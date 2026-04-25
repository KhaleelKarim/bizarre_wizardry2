package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModAttachments;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EquipSpellPayload(Identifier spellId, int index) implements CustomPacketPayload {

    public static final Type<EquipSpellPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "equip_spell"));

    public static final StreamCodec<FriendlyByteBuf, EquipSpellPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUtf(payload.spellId().toString());
                        buf.writeInt(payload.index());
                    },
                    buf -> new EquipSpellPayload(parseIdentifier(buf.readUtf()), buf.readInt())
            );

    @Override
    public Type<EquipSpellPayload> type() {
        return TYPE;
    }

    public static void handle(EquipSpellPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

        Identifier spellId = payload.spellId();
        int index = payload.index();

        // Verify spell exists in registry
        boolean exists = ModSpells.SPELL_REGISTRY.stream()
                .anyMatch(spell -> spellId.equals(ModSpells.SPELL_REGISTRY.getKey(spell)));
        if (!exists) return;

        EquippedSpellsData equipData = serverPlayer.getData(ModAttachments.EQUIPPED_SPELLS.get());
        UnlockedSpellsData unlockData = serverPlayer.getData(ModAttachments.UNLOCKED_SPELLS.get());

        // Spell must be unlocked before it can be equipped
        if (!unlockData.isUnlocked(spellId)) return;

        // Slot index must be within the player's current loadout capacity
        if (index < 0 || index >= equipData.getMaxSlots()) return;

        equipData.equip(spellId, index);
        serverPlayer.setData(ModAttachments.EQUIPPED_SPELLS.get(), equipData);

        // Sync updated loadout back to client
        context.reply(SyncEquippedSpellsPayload.from(equipData));
    }

    static Identifier parseIdentifier(String str) {
        int colon = str.indexOf(':');
        if (colon < 0) return Identifier.fromNamespaceAndPath("minecraft", str);
        return Identifier.fromNamespaceAndPath(str.substring(0, colon), str.substring(colon + 1));
    }
}
