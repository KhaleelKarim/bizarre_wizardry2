package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DashSpell extends Spell {

    public DashSpell() {
        super(10, 10);
    }

    @Override
    public void cast(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            double dashStrength = 1.5;
            player.push(look.scale(dashStrength));
            player.hurtMarked = true;
        }
    }
}
