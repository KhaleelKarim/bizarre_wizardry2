package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class StompSpell extends Spell{

    public StompSpell() {
        super(10, 20);
    }

    @Override
    public void cast(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (!level.isClientSide()) {
            AABB area = player.getBoundingBox().inflate(5.0);
            for (Entity entity : level.getEntities(player, area)) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 1.5, 0));
                entity.hurtMarked = true;
            }
        }
    }

}
