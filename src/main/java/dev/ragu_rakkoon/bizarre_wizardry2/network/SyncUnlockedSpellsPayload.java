package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.client.ClientUnlockedSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record SyncUnlockedSpellsPayload(Set<Identifier> unlockedSpells) implements CustomPacketPayload {

    public static final Type<SyncUnlockedSpellsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "sync_unlocked_spells"));

    public static final StreamCodec<FriendlyByteBuf, SyncUnlockedSpellsPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        var list = payload.unlockedSpells().stream().toList();
                        buf.writeVarInt(list.size());
                        list.forEach(id -> buf.writeUtf(id.toString()));
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        Set<Identifier> set = new HashSet<>();
                        for (int i = 0; i < size; i++) {
                            set.add(UnlockSpellPayload.parseIdentifier(buf.readUtf()));
                        }
                        return new SyncUnlockedSpellsPayload(set);
                    }
            );

    @Override
    public Type<SyncUnlockedSpellsPayload> type() {
        return TYPE;
    }

    public static SyncUnlockedSpellsPayload from(UnlockedSpellsData data) {
        return new SyncUnlockedSpellsPayload(data.getUnlockedSpells());
    }

    public static void handle(SyncUnlockedSpellsPayload payload, IPayloadContext context) {
        ClientUnlockedSpells.set(payload.unlockedSpells());
    }
}
