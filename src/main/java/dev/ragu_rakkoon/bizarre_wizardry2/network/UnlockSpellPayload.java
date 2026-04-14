package dev.ragu_rakkoon.bizarre_wizardry2.network;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModAttachments;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.SpellUnlockCondition;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.SpellUnlockConditions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record UnlockSpellPayload(Identifier spellId) implements CustomPacketPayload {

    public static final Type<UnlockSpellPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "unlock_spell"));

    public static final StreamCodec<FriendlyByteBuf, UnlockSpellPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeUtf(payload.spellId().toString()),
                    buf -> new UnlockSpellPayload(parseIdentifier(buf.readUtf()))
            );

    @Override
    public Type<UnlockSpellPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockSpellPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

        Identifier spellId = payload.spellId();

        // Verify spell exists in registry
        boolean exists = ModSpells.SPELL_REGISTRY.stream()
                .anyMatch(spell -> spellId.equals(ModSpells.SPELL_REGISTRY.getKey(spell)));
        if (!exists) return;

        UnlockedSpellsData data = serverPlayer.getData(ModAttachments.UNLOCKED_SPELLS.get());

        // Already unlocked
        if (data.isUnlocked(spellId)) return;

        // Check unlock condition server-side
        SpellUnlockCondition cond = SpellUnlockConditions.get(spellId);
        if (!cond.isAlways()) {
            int statValue = serverPlayer.getStats().getValue(cond.getStat());
            if (!cond.isMet(statValue)) return;
        }

        // Check prerequisite: spell at previous registry position must be unlocked
        List<Spell> allSpells = ModSpells.SPELL_REGISTRY.stream().toList();
        int targetIndex = -1;
        for (int i = 0; i < allSpells.size(); i++) {
            Identifier key = ModSpells.SPELL_REGISTRY.getKey(allSpells.get(i));
            if (spellId.equals(key)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex > 0) {
            Identifier prereqId = ModSpells.SPELL_REGISTRY.getKey(allSpells.get(targetIndex - 1));
            if (prereqId == null || !data.isUnlocked(prereqId)) return;
        }

        data.unlock(spellId);
        serverPlayer.setData(ModAttachments.UNLOCKED_SPELLS.get(), data);

        // Sync updated data back to client
        context.reply(SyncUnlockedSpellsPayload.from(data));
    }

    static Identifier parseIdentifier(String str) {
        int colon = str.indexOf(':');
        if (colon < 0) return Identifier.fromNamespaceAndPath("minecraft", str);
        return Identifier.fromNamespaceAndPath(str.substring(0, colon), str.substring(colon + 1));
    }
}
