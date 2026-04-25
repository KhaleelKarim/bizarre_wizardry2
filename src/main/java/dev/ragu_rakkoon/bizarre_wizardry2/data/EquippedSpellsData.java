package dev.ragu_rakkoon.bizarre_wizardry2.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EquippedSpellsData {

    /** Sentinel value representing an empty spell slot. */
    public static final Identifier EMPTY = Identifier.fromNamespaceAndPath("bizarre_wizardry2_jak", "empty");

    private static final int DEFAULT_MAX_SLOTS = 3;

    public static final MapCodec<EquippedSpellsData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("max_slots").forGetter(d -> d.maxSlots),
                    Identifier.CODEC.listOf().fieldOf("spells").forGetter(d -> d.equippedSpells)
            ).apply(instance, EquippedSpellsData::new)
    );

    private int maxSlots;
    private final ArrayList<Identifier> equippedSpells;

    public EquippedSpellsData(int maxSlots, List<Identifier> spells) {
        this.maxSlots = maxSlots;
        this.equippedSpells = new ArrayList<>(spells);
        // Ensure the list is exactly maxSlots size
        while (this.equippedSpells.size() < maxSlots) this.equippedSpells.add(EMPTY);
        while (this.equippedSpells.size() > maxSlots) this.equippedSpells.remove(this.equippedSpells.size() - 1);
    }

    public EquippedSpellsData() {
        this.maxSlots = DEFAULT_MAX_SLOTS;
        this.equippedSpells = new ArrayList<>(Collections.nCopies(DEFAULT_MAX_SLOTS, EMPTY));
    }

    public boolean isEquipped(Identifier spellId) {
        if (EMPTY.equals(spellId)) return false;
        return equippedSpells.contains(spellId);
    }

    /** Returns the slot index of the spell, or -1 if not equipped. */
    public int getSlotOf(Identifier spellId) {
        if (EMPTY.equals(spellId)) return -1;
        return equippedSpells.indexOf(spellId);
    }

    /** Returns the spell at the given slot index, or EMPTY if the slot is empty or out of bounds. */
    public Identifier getSpellAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= maxSlots) return EMPTY;
        return equippedSpells.get(slotIndex);
    }

    /**
     * Places a spell into a slot. Automatically removes it from any other slot it occupied.
     * Does nothing if the slot index is out of bounds.
     */
    public void equip(Identifier spellId, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= maxSlots) return;
        if (EMPTY.equals(spellId)) return;
        // Clear the spell from any other slot it occupies
        for (int i = 0; i < maxSlots; i++) {
            if (spellId.equals(equippedSpells.get(i))) equippedSpells.set(i, EMPTY);
        }
        equippedSpells.set(slotIndex, spellId);
    }

    /** Clears the spell from a specific slot. */
    public void unequip(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= maxSlots) return;
        equippedSpells.set(slotIndex, EMPTY);
    }

    /** Removes a spell from whichever slot it occupies. */
    public void unequipSpell(Identifier spellId) {
        for (int i = 0; i < equippedSpells.size(); i++) {
            if (spellId.equals(equippedSpells.get(i))) {
                equippedSpells.set(i, EMPTY);
                return;
            }
        }
    }

    /** Returns true if all slots are occupied. */
    public boolean isFull() {
        return equippedSpells.stream().noneMatch(EMPTY::equals);
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    /**
     * Expands or shrinks the loadout capacity. When shrinking, spells beyond the
     * new limit are lost.
     */
    public void setMaxSlots(int newMax) {
        if (newMax <= 0) return;
        while (equippedSpells.size() < newMax) equippedSpells.add(EMPTY);
        while (equippedSpells.size() > newMax) equippedSpells.remove(equippedSpells.size() - 1);
        this.maxSlots = newMax;
    }

    public List<Identifier> getEquippedSpells() {
        return List.copyOf(equippedSpells);
    }
}
