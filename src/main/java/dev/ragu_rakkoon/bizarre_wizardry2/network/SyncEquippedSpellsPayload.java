package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.client.ClientEquippedSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncEquippedSpellsPayload(List<Identifier> equippedSpells, int maxSlots, int selectedSlot)
        implements CustomPacketPayload {

    public static final Type<SyncEquippedSpellsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "sync_equipped_spells"));

    public static final StreamCodec<FriendlyByteBuf, SyncEquippedSpellsPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.maxSlots());
                        buf.writeVarInt(payload.equippedSpells().size());
                        payload.equippedSpells().forEach(id -> buf.writeUtf(id.toString()));
                        buf.writeVarInt(payload.selectedSlot());
                    },
                    buf -> {
                        int maxSlots = buf.readVarInt();
                        int size = buf.readVarInt();
                        List<Identifier> list = new ArrayList<>();
                        for (int i = 0; i < size; i++) list.add(EquipSpellPayload.parseIdentifier(buf.readUtf()));
                        int selectedSlot = buf.readVarInt();
                        return new SyncEquippedSpellsPayload(list, maxSlots, selectedSlot);
                    }
            );

    @Override
    public Type<SyncEquippedSpellsPayload> type() {
        return TYPE;
    }

    public static SyncEquippedSpellsPayload from(EquippedSpellsData data) {
        return new SyncEquippedSpellsPayload(data.getEquippedSpells(), data.getMaxSlots(), data.getSelectedSlot());
    }

    public static void handle(SyncEquippedSpellsPayload payload, IPayloadContext context) {
        ClientEquippedSpells.set(payload.equippedSpells(), payload.maxSlots(), payload.selectedSlot());
    }
}
