package io.github.term4.polyp.platform.compatibility;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** {@code off()} must decide every Boolean knob - a new CompatConfig field that skips it fails here. */
class CompatOffCoverageTest {

    @Test
    void offDecidesEveryBooleanKnob() throws IllegalAccessException {
        CompatConfig off = Compat18.off();
        for (Field field : CompatConfig.class.getFields()) {
            if (field.getType() == Boolean.class) {
                assertNotNull(field.get(off), "Compat18.off() skips " + field.getName());
            }
        }
    }
}
