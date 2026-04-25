package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModAttachments;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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

        EquippedSpellsData equipData = player.getData(ModAttachments.EQUIPPED_SPELLS.get());

        // Nothing to cycle if all slots are empty
        boolean anyOccupied = equipData.getEquippedSpells().stream()
                .anyMatch(id -> !EquippedSpellsData.EMPTY.equals(id));
        if (!anyOccupied) return;

        equipData.cycleSlot();
        player.setData(ModAttachments.EQUIPPED_SPELLS.get(), equipData);

        // Notify the client of the new selected slot
        context.reply(SyncEquippedSpellsPayload.from(equipData));

        // Show the newly selected spell name in the action bar
        Identifier newId = equipData.getSelectedSpellId();
        Spell spell = ModSpells.SPELL_REGISTRY.get(newId).map(Holder.Reference::value).orElse(null);
        if (spell != null && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSystemChatPacket(
                    Component.translatable("message.bizarre_wizardry2_jak.spell_switched",
                            Component.translatable(spell.getTranslationKey())), true));
        }
    }
}
