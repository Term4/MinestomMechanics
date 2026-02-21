package io.github.term4.minestommechanics.mechanics.knockback;

import io.github.term4.minestommechanics.api.event.knockback.KnockbackEvent;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;
import io.github.term4.minestommechanics.util.VelocityEstimator;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class KnockbackCalculator {

    private static final double MIN_DIST = 1e-6; // distance at which position delta direction is degenerate
    private final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;

    public Vec compute(AttackSnapshot snap, KnockbackConfig cfg, KnockbackEvent.Cause cause) {

        boolean hasExtra = snap.sprint(); // Also true if snap.item has a knockback attribute (not implemented yet)

        // Add return or something for kbCause!!!

        // References
        Entity a = snap.attacker();
        Entity t = snap.target();
        if (t == null) return Vec.ZERO;
        Point aPt = snap.attackerPos() != null ? snap.attackerPos() : (a != null ? a.getPosition() : t.getPosition());
        Point vPt = snap.targetPos() != null ? snap.targetPos() : t.getPosition();
        Pos aPos = a != null ? a.getPosition() : (aPt instanceof Pos p ? p : new Pos(aPt.x(), aPt.y(), aPt.z()));

        // Direction
        Vec dDirH = deltaH(aPt, vPt);
        Vec yDirH = yawDir(aPos);
        Vec dDirV = deltaV(aPt, vPt);
        Vec pDirV = pitchDir(aPos);

        RawDirs raw = new RawDirs(dDirH, dDirV, yDirH, pDirV);
        DirAndStrength norm = resolveDS(raw, cfg, false);
        DirAndStrength extra = hasExtra ? resolveDS(raw, cfg, true) :  null;

        // Strength
        Vec kb = norm.direction().mul(norm.h(), norm.v(), norm.h());
        Vec kbe = extra != null ? extra.direction().mul(extra.h(), extra.v(), extra.h()) : null;


        // Normal KB
        kb = applyRr(kb, aPt, vPt, cfg, false);         // Range Reduction
        kb = applyAMult(kb, t, cfg, false);             // Air Multiplier
        kb = applySweeping(kb, cause, cfg, false);      // Modifiers

        // Extra KB
        if (kbe != null) {
            kbe = applyRr(kbe, aPt, vPt, cfg, true);    // Range Reduction
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

    /** Horizontal (xz) direction from attacker to victim. Null if degenerate. */
    private static Vec deltaH(Point aPt, Point vPt) {
        double dx = vPt.x() - aPt.x();
        double dz = vPt.z() - aPt.z();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < MIN_DIST) return ranDirH();
        return new Vec(dx / dist, 0, dz / dist);
    }

    /** Vertical unit direction from height delta (is the victim above or below the attacker */
    private static Vec deltaV(Point aPt, Point vPt) {
        double dy = vPt.y() - aPt.y();
        if (Math.abs(dy) < MIN_DIST) return new Vec(0,1,0); // defaults to up
        return new Vec(0, Math.signum(dy), 0);
    }

    /** Horizontal direction from attacker yaw */
    private static Vec yawDir(Pos aPos) {
        Vec yaw = aPos.direction();
        double len = Math.sqrt(yaw.x() * yaw.x() + yaw.z() * yaw.z());
        if (len < MIN_DIST) return ranDirH();
        return new Vec(yaw.x() / len, 0, yaw.z() / len);
    }

    /** Vertical unit direction from pitch (determines up / down) */
    private static Vec pitchDir(Pos aPos) {
        double y = aPos.direction().y();
        if (Math.abs(y) < MIN_DIST) return new Vec (0,1,0);
        return new Vec(0, Math.signum(y), 0);
    }

    /** Returns a random horizontal (xz) vector. */
    private static Vec ranDirH() {
        Vec v;
        do {
            double x = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            double z = ThreadLocalRandom.current().nextDouble() * 2 - 1;
            v = new Vec(x, 0, z);
        } while (v.length() < MIN_DIST);
        return v.normalize();
    }

    private static final Vec UP = new Vec(0, 1, 0);

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
    private Vec applySweeping(Vec kb, KnockbackEvent.Cause cause, KnockbackConfig cfg, boolean hasExtra) {
        if (cause != KnockbackEvent.Cause.SWEEPING) return kb;

        double sfh = hasExtra ? cfg.sweepFactorExtraH : cfg.sweepFactorH;
        double sfv = hasExtra ? cfg.sweepFactorExtraV : cfg.sweepFactorV;

        double sh = 1.0 - sfh;
        double sv = 1.0 - sfv;

        return new Vec(kb.x() * sh, kb.y() * sv, kb.z() * sh);
    }
}
