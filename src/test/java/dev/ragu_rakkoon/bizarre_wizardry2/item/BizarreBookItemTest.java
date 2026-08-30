package dev.ragu_rakkoon.bizarre_wizardry2.item;

import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Tag("item")
@ExtendWith(MockitoExtension.class)
class BizarreBookItemTest {

    @Mock
    private Level level;

    @Mock
    private Player player;

    @Test
    void use_onServerSide_returnsSuccess() {
        BizarreBookItem item = ModItems.BIZARRE_BOOK.get();
        when(level.isClientSide()).thenReturn(false);

        InteractionResult result = item.use(level, player, InteractionHand.MAIN_HAND);

        assertEquals(InteractionResult.SUCCESS, result);
    }
}
