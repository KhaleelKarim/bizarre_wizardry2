package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.item.ZanpakutoItem;
import dev.ragu_rakkoon.bizarre_wizardry2.item.ZanpakutoSpellData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CycleSpellPayload() implements CustomPacketPayload {
    public static final Type<CycleSpellPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "cycle_spell"));

    public static final StreamCodec<ByteBuf, CycleSpellPayload> STREAM_CODEC = StreamCodec.unit(new CycleSpellPayload());

    @Override
    public Type<CycleSpellPayload> type() {
        return TYPE;
    }

    public static void handle(CycleSpellPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ZanpakutoItem)) {
            return;
        }

        ZanpakutoSpellData data = stack.get(ModDataComponents.ZANPAKUTO_SPELL_DATA.get());
        if (data == null || data.spells().isEmpty()) {
            return;
        }

        int newIndex = (data.selectedIndex() + 1) % data.spells().size();
        stack.set(ModDataComponents.ZANPAKUTO_SPELL_DATA.get(), data.withSelectedIndex(newIndex));

        String spellName = data.spells().get(newIndex).value().getTranslationKey();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSystemChatPacket(
                    Component.translatable("message.bizarre_wizardry2_jak.spell_switched", Component.translatable(spellName)), true));
        }
    }
}
