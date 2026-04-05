package dev.ragu_rakkoon.bizarre_wizardry2.registry;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BizarreWizardry2.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BIZARRE_WIZARDRY_TAB = CREATIVE_MODE_TABS.register("bizarre_wizardry_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bizarre_wizardry2_jak.bizarre_wizardry2"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.ZANPAKUTO.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ZANPAKUTO.get());
            }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
