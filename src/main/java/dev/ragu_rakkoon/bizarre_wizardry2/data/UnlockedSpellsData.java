package dev.ragu_rakkoon.bizarre_wizardry2.data;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UnlockedSpellsData {

    public static final MapCodec<UnlockedSpellsData> CODEC =
            Identifier.CODEC.listOf()
                    .fieldOf("spells")
                    .xmap(
                            list -> new UnlockedSpellsData(new HashSet<>(list)),
                            data -> new ArrayList<>(data.unlockedSpells)
                    );

    private final Set<Identifier> unlockedSpells;

    public UnlockedSpellsData(Set<Identifier> unlockedSpells) {
        this.unlockedSpells = new HashSet<>(unlockedSpells);
    }

    public UnlockedSpellsData() {
        this.unlockedSpells = new HashSet<>();
    }

    public boolean isUnlocked(Identifier spellId) {
        return unlockedSpells.contains(spellId);
    }

    public void unlock(Identifier spellId) {
        unlockedSpells.add(spellId);
    }

    public Set<Identifier> getUnlockedSpells() {
        return Set.copyOf(unlockedSpells);
    }
}
