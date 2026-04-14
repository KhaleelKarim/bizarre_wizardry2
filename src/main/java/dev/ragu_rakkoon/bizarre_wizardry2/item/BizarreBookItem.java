package dev.ragu_rakkoon.bizarre_wizardry2.item;

import dev.ragu_rakkoon.bizarre_wizardry2.client.screen.BizarreBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class BizarreBookItem extends Item {

    public BizarreBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new BizarreBookScreen());
        }
        return InteractionResult.SUCCESS;
    }
}
