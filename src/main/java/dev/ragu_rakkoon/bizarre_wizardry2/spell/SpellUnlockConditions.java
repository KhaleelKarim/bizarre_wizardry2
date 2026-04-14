package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;

import java.util.HashMap;
import java.util.Map;

public class SpellUnlockConditions {

    // 100 blocks of total fall distance tracked in centimeters by MC.
    private static final int STOMP_FALL_CM = 10_000;
    private static final int STOMP_DISPLAY_DIVISOR = 100; // cm -> blocks

    private static Map<Identifier, SpellUnlockCondition> CONDITIONS = null;

    private static Map<Identifier, SpellUnlockCondition> conditions() {
        if (CONDITIONS != null) return CONDITIONS;
        CONDITIONS = new HashMap<>();

        CONDITIONS.put(
                ModSpells.SPELL_REGISTRY.getKey(ModSpells.FIREBALL.value()),
                SpellUnlockCondition.ALWAYS
        );
        CONDITIONS.put(
                ModSpells.SPELL_REGISTRY.getKey(ModSpells.STOMP.value()),
                new SpellUnlockCondition(
                        Stats.CUSTOM.get(Stats.FALL_ONE_CM),
                        STOMP_FALL_CM,
                        "blocks",
                        "Fall 100 blocks total"
                )
        );
        // Dash has no defined condition yet — defaults to locked
        return CONDITIONS;
    }

    public static SpellUnlockCondition get(Identifier spellId) {
        return conditions().getOrDefault(spellId, new SpellUnlockCondition(null, 1, null, "Locked"));
    }

    /** Convenience: display divisor for a condition (cm -> blocks for fall-based conditions). */
    public static int getDisplayDivisor(Identifier spellId) {
        if (spellId.equals(ModSpells.SPELL_REGISTRY.getKey(ModSpells.STOMP.value()))) {
            return STOMP_DISPLAY_DIVISOR;
        }
        return 1;
    }
}
