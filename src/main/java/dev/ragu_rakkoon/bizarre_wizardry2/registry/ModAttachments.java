package dev.ragu_rakkoon.bizarre_wizardry2.registry;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BizarreWizardry2.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UnlockedSpellsData>> UNLOCKED_SPELLS =
            ATTACHMENT_TYPES.register("unlocked_spells", () ->
                    AttachmentType.builder(() -> new UnlockedSpellsData())
                            .serialize(UnlockedSpellsData.CODEC)
                            .copyOnDeath()
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EquippedSpellsData>> EQUIPPED_SPELLS =
            ATTACHMENT_TYPES.register("equipped_spells", () ->
                    AttachmentType.builder(() -> new EquippedSpellsData())
                            .serialize(EquippedSpellsData.CODEC)
                            .copyOnDeath()
                            .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
