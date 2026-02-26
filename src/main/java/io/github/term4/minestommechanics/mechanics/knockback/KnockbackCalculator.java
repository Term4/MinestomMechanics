package io.github.term4.minestommechanics.mechanics.knockback;

import io.github.term4.minestommechanics.Services;
import io.github.term4.minestommechanics.mechanics.Cause;
import io.github.term4.minestommechanics.util.SprintTracker;
import io.github.term4.minestommechanics.util.VelocityEstimator;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

// TODO: Add idle / active state handling (each can have an entirely custom "sub" config, which can be override selectively from the api snapshot)
public final class KnockbackCalculator {

    private final Services services;

    private static final double MIN_DIST = 1e-6; // distance at which position delta direction is degenerate
    private final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;

    public KnockbackCalculator(Services services) {
        this.services = services;
    }

    public @Nullable Vec compute(KnockbackSnapshot snap) {

        // Knockback Context
        DirContext ctx = dirCtx(snap);
        if (ctx == null) return null;

        KnockbackConfig cfg = snap.config();
        Cause cause = snap.cause();
        boolean hasExtra = extra(snap) > 0;

        Entity t = snap.target();
        Point oPt = ctx.oPt();
        Point tPt = t.getPosition();
        Vec dir3D = ctx.dir();

        // Direction
        Vec dDirH = deltaH(oPt, tPt);
        Vec yDirH = yawDir(dir3D);
        Vec dDirV = deltaV(oPt, tPt);
        Vec pDirV = pitchDir(dir3D);

        RawDirs raw = new RawDirs(dDirH, dDirV, yDirH, pDirV);
        DirAndStrength normKb = resolveDS(raw, cfg, false);
        DirAndStrength extraKb = hasExtra ? resolveDS(raw, cfg, true) :  null;

        // Strength
        Vec kb = normKb.direction().mul(normKb.h(), normKb.v(), normKb.h());
        Vec kbe = extraKb != null ? extraKb.direction().mul(extraKb.h(), extraKb.v(), extraKb.h()) : null;

        // Normal KB
        kb = applyRr(kb, oPt, tPt, cfg, false);         // Range Reduction
        kb = applyAMult(kb, t, cfg, false);             // Air Multiplier
        kb = applySweeping(kb, cause, cfg, false);      // Modifiers

        // Extra KB
        if (kbe != null) {
            kbe = applyRr(kbe, oPt, tPt, cfg, true);    // Range Reduction
            kbe = applyAMult(kbe, t, cfg, true);        // Air Multiplier
            kbe = applySweeping(kbe, cause, cfg, true); // Modifiers
        }

        Vec kbVec = kbe != null ? addVectors(kb, kbe, cfg) : kb;

        // Friction
        double fH = kbe != null ? cfg.frictionExtraH : cfg.frictionH;
        double fV = kbe != null ? cfg.frictionExtraV : cfg.frictionV;
        Vec mot = VelocityEstimator.getVelocity(t);
        double iFH = fH > 0 ? 1.0 / fH : 0;
        double iFV = fV > 0 ? 1.0 / fV : 0;
        kbVec = new Vec(mot.x() * iFH + kbVec.x(),
                mot.y() * iFV + kbVec.y(),
                mot.z() * iFH + kbVec.z());

        if (cfg.verticalLimit > 0) {
            double y = Math.max(-cfg.verticalLimit, Math.min(cfg.verticalLimit, kbVec.y())); // NOTE: could be weird with pitch / height delta
            kbVec = new Vec(kbVec.x(), y, kbVec.z());
        }

        kbVec = new Vec(kbVec.x() * tps, kbVec.y() * tps, kbVec.z() * tps);

        return kbVec;
    }

    /** Resolves "extra" knockback involved in knockback event. */
    private int extra(KnockbackSnapshot snap) {
        int extra = 0;

        // Only melee causes extra (for now) TODO: add handling for non-melee extra (e.g. punch bows)
        if (!snap.cause().isMelee()) return extra;

        Entity a = snap.source();
        if (a == null) return extra;

        if (SprintTracker.isSprinting(services.sprintTracker(), a, snap.config().sprintBuffer)) extra++;
        return extra;
    }

    /** Gets the direction context for this knockback. */
    private record DirContext(Point oPt, Vec dir) {}

    private @Nullable DirContext dirCtx(KnockbackSnapshot snap) {
        Entity t = snap.target();
        Point tPt = t.getPosition();
        Entity s = snap.source();
        Point oPt = snap.origin();
        Vec dir = snap.direction();

        if (s != null) return new DirContext(s.getPosition(), s.getPosition().direction());
        if (oPt != null && dir == null) return new DirContext(oPt, new Pos(oPt).withLookAt(tPt).direction());
        if (dir != null) {
            Point pt = oPt != null ? oPt : tPt;
            return new DirContext(pt, new Pos(pt).withDirection(dir).direction());
        } return null;
    }


    /** Direction + horizontal/vertical strengths */
    private record DirAndStrength(Vec direction, double h, double v) {}

    /** Raw position and yaw/pitch directions */
    private record RawDirs(Vec posH, Vec posV, Vec yaw, Vec pitch) {}

    /**
     * Combines raw directions into one direction and strength.
     * extra=true uses "extra" knockback values (sprint, enchantment)
     */
    private DirAndStrength resolveDS(RawDirs raw, KnockbackConfig cfg, boolean extra) {
        double h = extra ? cfg.extraHorizontal :  cfg.horizontal;
        double v = extra ? cfg.extraVertical : cfg.vertical;
        double yw = extra ? cfg.extraYawWeight : cfg.yawWeight;
        double pw =  extra ? cfg.extraPitchWeight : cfg.pitchWeight;
        double hw = extra ? cfg.extraHeightDelta : cfg.heightDelta;

        Vec dirH; Vec dirV; double magH = h; double magV = v;

        // Horizontal
        if (cfg.horizontalCombine == KnockbackConfig.DirectionMode.VECTOR_ADDITION) {
            double posMag = h * (1 - yw);
            double lookMag = h * yw;
            double cx = raw.posH().x() * posMag + raw.yaw().x() * lookMag;
            double cz = raw.posH().z() * posMag + raw.yaw().z() * lookMag;
            double len = Math.sqrt(cx * cx + cz * cz);
            dirH = len < MIN_DIST ? raw.yaw() : new Vec(cx / len, 0, cz / len);
            magH = len < MIN_DIST ? h : len;
        } else {
            dirH = blend(raw.posH(), raw.yaw(), 1-yw, yw, KnockbackCalculator::ranDirH);
        }

        // Vertical
        if (cfg.verticalCombine == KnockbackConfig.DirectionMode.VECTOR_ADDITION) {
            double heightMag = v * hw;
            double pitchMag = v * pw;
            double cy = raw.pitch().y() * pitchMag + raw.posV().y() * heightMag;
            double len = Math.abs(cy);
            dirV = len < MIN_DIST ? UP : new Vec(0, Math.signum(cy), 0);
            magV = len < MIN_DIST ? v : len;
        } else {
            dirV = blend(raw.pitch(), raw.posV(), pw, hw, () -> UP);
        }

        Vec dir3D = new Vec(dirH.x(), dirV.y(), dirH.z());

        return new DirAndStrength(dir3D, magH, magV);
    }

    /** Combines normal and extra knockback vectors. */
    private Vec addVectors(Vec a, Vec b, KnockbackConfig cfg) {
        boolean hAdd = cfg.horizontalCombine == KnockbackConfig.DirectionMode.VECTOR_ADDITION;
        boolean vAdd = cfg.verticalCombine == KnockbackConfig.DirectionMode.VECTOR_ADDITION;

        double resX, resZ, resY;

        if (hAdd) {
            resX = a.x() + b.x();
            resZ = a.z() + b.z();
        } else {
            double magA = Math.sqrt(a.x() * a.x() + a.z() * a.z());
            double magB = Math.sqrt(b.x() * b.x() + b.z() * b.z());
            double hNet = magA + magB;

            if (hNet < MIN_DIST) {
                resX = resZ = 0;
            } else {
                double sumX = a.x() + b.x();
                double sumZ = a.z() + b.z();
                double len = Math.sqrt(sumX * sumX + sumZ * sumZ);
                if (len < MIN_DIST) {
                    resX = resZ = 0;
                } else {
                    double s = hNet / len;
                    resX = sumX * s;
                    resZ = sumZ * s;
                }
            }
        }

        if (vAdd) {
            resY = a.y() + b.y();
        } else {
            double vNet = Math.abs(a.y()) + Math.abs(b.y());
            double blendY = a.y() + b.y();
            resY = Math.max(-vNet, Math.min(vNet, blendY));
        }

        return new Vec(resX, resY, resZ);
    }

    private static final Vec UP = new Vec(0, 1, 0);

    /** Horizontal (xz) direction from attacker to victim. Null if degenerate. */
    private static Vec deltaH(Point sPt, Point tPt) {
        double dx = tPt.x() - sPt.x();
        double dz = tPt.z() - sPt.z();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < MIN_DIST) return ranDirH();
        return new Vec(dx / dist, 0, dz / dist);
    }

    /** Vertical unit direction from height delta (is the victim above or below the attacker. */
    private static Vec deltaV(Point sPt, Point tPt) {
        double dy = tPt.y() - sPt.y();
        if (Math.abs(dy) < MIN_DIST) return UP; // defaults to up
        return new Vec(0, Math.signum(dy), 0);
    }

    /** Vertical unit direction from 3D unit vector (determines up / down). */
    private static Vec pitchDir(Vec dir) {
        if (Math.abs(dir.y()) < MIN_DIST) return UP;
        return new Vec(0, Math.signum(dir.y()), 0);
    }

    /** Horizontal direction from 3D unit vector. */
    private static Vec yawDir(Vec dir) {
        double len = Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
        if (len < MIN_DIST) return ranDirH();
        return new Vec(dir.x() / len, 0, dir.z() / len);
    }

    /** Returns a random horizontal vector. */
    private static Vec ranDirH() {
        Vec v;
        do {
            double x = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            double z = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            v = new Vec(x, 0, z);
        } while (v.length() < MIN_DIST);
        return v.normalize();
    }

    /** Weighted blend of two vectors. Uses fallback when sum is degenerate. */
    private static Vec blend(Vec a, Vec b, double wA, double wB, Supplier<Vec> ranDir) {
        if (wA <= 0 && wB <= 0) return ranDir.get();
        Vec sum = a.mul(wA).add(b.mul(wB));
        return sum.lengthSquared() < MIN_DIST *  MIN_DIST ? ranDir.get() : sum.normalize();
    }

    /** Applies range reduction (reduces knockback based on distance between attacker & victim).*/
    private Vec applyRr(Vec kb, Point aPt, Point vPt, KnockbackConfig cfg, boolean hasExtra) {
        double dh = Math.sqrt(Math.pow(vPt.x() - aPt.x(), 2) + Math.pow(vPt.z() - aPt.z(), 2));
        double dv = Math.abs(vPt.y() - aPt.y());

        double rsh = hasExtra ? cfg.rangeStartExtraH : cfg.rangeStartH;
        double rfh = hasExtra ? cfg.rangeFactorExtraH : cfg.rangeFactorH;
        double rsv = hasExtra ? cfg.rangeStartExtraV : cfg.rangeStartV;
        double rfv = hasExtra ? cfg.rangeFactorExtraV : cfg.rangeFactorV;

        double sh = dh <= rsh ? 1.0 : 1.0 - rfh * (dh - rsh);
        double sv = dv <= rsv ? 1.0 : 1.0 - rfv * (dv - rsv);

        if (cfg.rangeMaxH > 0) sh = Math.max(sh, cfg.rangeMaxH);
        if (cfg.rangeMaxV > 0) sv = Math.max(sv, cfg.rangeMaxV);

        sh = Math.max(0, Math.min(1, sh));
        sv = Math.max(0, Math.min(1, sv));

        return new Vec(kb.x() * sh, kb.y() * sv, kb.z() * sh);
    }

    /** Applies air multipliers (how knockback is effected when the victim is in the air) */
    private Vec applyAMult(Vec kb, Entity e, KnockbackConfig cfg, boolean hasExtra) {
        boolean inAir = e instanceof LivingEntity le && !le.isOnGround();
        if (!inAir) return kb;

        double mH = hasExtra ? cfg.aMultExtraH : cfg.aMultH;
        double mV = hasExtra ? cfg.aMultExtraV : cfg.aMultV;

        Vec result = new Vec(kb.x() * mH, kb.y() * mV, kb.z() * mH);

        if (cfg.aMultVLimit > 0) {
            double y = Math.min(cfg.aMultVLimit, result.y()); // NOTE: could get weird with pitch / height delta
            result = new Vec(result.x(), y, result.z());
        }
        return result;
    }

    /** Reduces knockback for sweeping attacks */
    private Vec applySweeping(Vec kb, Cause cause, KnockbackConfig cfg, boolean hasExtra) {
        if (cause != Cause.SWEEPING) return kb;

        double sfh = hasExtra ? cfg.sweepFactorExtraH : cfg.sweepFactorH;
        double sfv = hasExtra ? cfg.sweepFactorExtraV : cfg.sweepFactorV;

        double sh = 1.0 - sfh;
        double sv = 1.0 - sfv;

        return new Vec(kb.x() * sh, kb.y() * sv, kb.z() * sh);
    }
}
