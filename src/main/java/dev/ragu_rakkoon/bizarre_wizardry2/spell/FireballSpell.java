package dev.ragu_rakkoon.bizarre_wizardry2.spell;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireballSpell extends Spell {

    public FireballSpell() {
        super(10, 20);
    }

    @Override
    public void cast(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            SmallFireball fireball = new SmallFireball(level, player, look);
            fireball.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            level.addFreshEntity(fireball);
        }
    }
}
