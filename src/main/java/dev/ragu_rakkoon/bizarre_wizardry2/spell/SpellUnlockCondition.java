package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import net.minecraft.stats.Stat;

/**
 * Holds the unlock requirement for a spell. Both server and client provide their own
 * stat value (via ServerPlayer.getStats() or LocalPlayer.getStats()); this class
 * contains only the threshold and display logic, keeping it side-agnostic.
 */
public class SpellUnlockCondition {

    public static final SpellUnlockCondition ALWAYS = new SpellUnlockCondition(null, 0, null, "Always available");

    private final Stat<?> stat;
    private final int threshold;
    private final String unit;          // e.g. "blocks"
    private final String description;  // e.g. "Fall 100 blocks"

    public SpellUnlockCondition(Stat<?> stat, int threshold, String unit, String description) {
        this.stat = stat;
        this.threshold = threshold;
        this.unit = unit;
        this.description = description;
    }

    public boolean isAlways() {
        return stat == null;
    }

    public Stat<?> getStat() {
        return stat;
    }

    public boolean isMet(int currentStatValue) {
        if (stat == null) return true;
        return currentStatValue >= threshold;
    }

    /**
     * @param currentStatValue the raw stat value (same units as threshold)
     * @param displayDivisor   divide stat value by this to get a human-readable number
     */
    public String getProgressText(int currentStatValue, int displayDivisor) {
        if (stat == null) return description;
        int current = currentStatValue / displayDivisor;
        int max = threshold / displayDivisor;
        return description + " (" + Math.min(current, max) + "/" + max + ")";
    }

    public int getThreshold() {
        return threshold;
    }

    public String getDescription() {
        return description;
    }
}
