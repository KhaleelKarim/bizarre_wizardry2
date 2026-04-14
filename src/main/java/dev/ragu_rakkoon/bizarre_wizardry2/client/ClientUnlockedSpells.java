package dev.ragu_rakkoon.bizarre_wizardry2.client;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientUnlockedSpells {

    private static Set<Identifier> unlockedSpells = new HashSet<>();

    public static void set(Set<Identifier> spells) {
        unlockedSpells = new HashSet<>(spells);
    }

    public static boolean isUnlocked(Identifier spellId) {
        return unlockedSpells.contains(spellId);
    }

    public static Set<Identifier> getAll() {
        return Collections.unmodifiableSet(unlockedSpells);
    }
}
