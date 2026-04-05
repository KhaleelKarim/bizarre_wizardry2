package dev.ragu_rakkoon.bizarre_wizardry2.registry;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.item.ZanpakutoSpellData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BizarreWizardry2.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ZanpakutoSpellData>> ZANPAKUTO_SPELL_DATA =
            DATA_COMPONENTS.register("zanpakuto_spell_data",
                    () -> DataComponentType.<ZanpakutoSpellData>builder()
                            .persistent(ZanpakutoSpellData.CODEC)
                            .build());

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
