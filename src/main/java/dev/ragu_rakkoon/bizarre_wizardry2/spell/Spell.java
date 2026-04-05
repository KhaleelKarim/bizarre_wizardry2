package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Spell {
    private final int manaCost;
    private final int cooldown;

    protected Spell(int manaCost, int cooldown) {
        this.manaCost = manaCost;
        this.cooldown = cooldown;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public String getTranslationKey() {
        Identifier id = ModSpells.SPELL_REGISTRY.getKey(this);
        return "spell." + id.getNamespace() + "." + id.getPath();
    }

    public abstract void cast(Level level, Player player, InteractionHand hand, ItemStack stack);
}
