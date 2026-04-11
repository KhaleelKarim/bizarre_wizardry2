package dev.ragu_rakkoon.bizarre_wizardry2;

import dev.ragu_rakkoon.bizarre_wizardry2.client.ModKeybinds;
import dev.ragu_rakkoon.bizarre_wizardry2.network.CycleSpellPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value = BizarreWizardry2.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BizarreWizardry2.MOD_ID, value = Dist.CLIENT)
public class BizarreWizardry2Client {
    public BizarreWizardry2Client(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerKeybinds);
        NeoForge.EVENT_BUS.addListener(BizarreWizardry2Client::onClientTick);
    }

    private void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.registerCategory(ModKeybinds.CATEGORY);
        event.register(ModKeybinds.CYCLE_SPELL_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        while (ModKeybinds.CYCLE_SPELL_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new CycleSpellPayload());
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        BizarreWizardry2.LOGGER.info("HELLO FROM CLIENT SETUP");
        BizarreWizardry2.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
