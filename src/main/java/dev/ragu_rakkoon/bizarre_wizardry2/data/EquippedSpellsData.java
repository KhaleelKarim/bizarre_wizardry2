package dev.ragu_rakkoon.bizarre_wizardry2.data;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class EquippedSpellsData {

    public static final MapCodec<EquippedSpellsData> CODEC =
            Identifier.CODEC.listOf()
                    .fieldOf("spells")
                    .xmap(
                            list -> new EquippedSpellsData(new ArrayList<>(list)),
                            data -> new ArrayList<>(data.equippedSpells)
                    );

    private final ArrayList<Identifier> equippedSpells;

    public EquippedSpellsData(ArrayList<Identifier> equippedSpells) {
        this.equippedSpells = new ArrayList<>(equippedSpells);
    }

    public EquippedSpellsData() {
        this.equippedSpells = new ArrayList<>();
    }

    public boolean isEquipped(Identifier spellId) {
        return equippedSpells.contains(spellId);
    }

    public void equip(Identifier spellId) {
        equippedSpells.add(spellId);
    }

    public List<Identifier> getEquippedSpells() {
        return List.copyOf(equippedSpells);
    }

}
