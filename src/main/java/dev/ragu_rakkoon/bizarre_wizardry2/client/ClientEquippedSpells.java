package dev.ragu_rakkoon.bizarre_wizardry2.client;

import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientEquippedSpells {

    private static int maxSlots = 3;
    private static ArrayList<Identifier> equippedSpells =
            new ArrayList<>(Collections.nCopies(maxSlots, EquippedSpellsData.EMPTY));
    private static int selectedSlot = 0;

    /** Called when the server syncs the player's full equipped spells state. */
    public static void set(List<Identifier> spells, int maxSlots, int selectedSlot) {
        ClientEquippedSpells.maxSlots = maxSlots;
        ClientEquippedSpells.equippedSpells = new ArrayList<>(spells);
        ClientEquippedSpells.selectedSlot = selectedSlot;
    }

    public static boolean isEquipped(Identifier spellId) {
        if (EquippedSpellsData.EMPTY.equals(spellId)) return false;
        return equippedSpells.contains(spellId);
    }

    /** Returns the slot index of the spell, or -1 if not equipped. */
    public static int equipPos(Identifier spellId) {
        if (EquippedSpellsData.EMPTY.equals(spellId)) return -1;
        return equippedSpells.indexOf(spellId);
    }

    /** Returns the spell at the given slot index, or EMPTY if out of bounds or empty. */
    public static Identifier getSpellAt(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= equippedSpells.size()) return EquippedSpellsData.EMPTY;
        return equippedSpells.get(slotIndex);
    }

    /** Returns true if all slots are occupied. */
    public static boolean isFull() {
        return equippedSpells.stream().noneMatch(EquippedSpellsData.EMPTY::equals);
    }

    public static int getMaxSlots() {
        return maxSlots;
    }

    public static int getSelectedSlot() {
        return selectedSlot;
    }

    /** Returns the ID of the currently selected spell, or EMPTY if the slot is empty. */
    public static Identifier getSelectedSpellId() {
        return getSpellAt(selectedSlot);
    }

    public static List<Identifier> getAll() {
        return Collections.unmodifiableList(equippedSpells);
    }
}
