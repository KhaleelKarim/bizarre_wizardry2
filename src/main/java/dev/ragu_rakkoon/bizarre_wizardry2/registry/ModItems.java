package dev.ragu_rakkoon.bizarre_wizardry2.registry;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.item.ZanpakutoItem;
import dev.ragu_rakkoon.bizarre_wizardry2.item.ZanpakutoSpellData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BizarreWizardry2.MOD_ID);

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", p -> p.food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredItem<ZanpakutoItem> ZANPAKUTO = ITEMS.registerItem(
            "zanpakuto",
            p -> new ZanpakutoItem(p
                    .attributes(ZanpakutoItem.createAttributes(5.0f, -2.4f))
                    .component(ModDataComponents.ZANPAKUTO_SPELL_DATA.get(), new ZanpakutoSpellData(List.of(), 0))));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
