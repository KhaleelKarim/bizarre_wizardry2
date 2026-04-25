package dev.ragu_rakkoon.bizarre_wizardry2;

import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.network.CycleSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.network.EquipSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.network.SyncEquippedSpellsPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.network.SyncUnlockedSpellsPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.network.UnlockSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.*;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(BizarreWizardry2.MOD_ID)
public class BizarreWizardry2 {
    public static final String MOD_ID = "bizarre_wizardry2_jak";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BizarreWizardry2(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSpells.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModAttachments.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.EXAMPLE_BLOCK_ITEM);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CycleSpellPayload.TYPE, CycleSpellPayload.STREAM_CODEC, CycleSpellPayload::handle);
        registrar.playToServer(UnlockSpellPayload.TYPE, UnlockSpellPayload.STREAM_CODEC, UnlockSpellPayload::handle);
        registrar.playToServer(EquipSpellPayload.TYPE, EquipSpellPayload.STREAM_CODEC, EquipSpellPayload::handle);
        registrar.playToClient(SyncUnlockedSpellsPayload.TYPE, SyncUnlockedSpellsPayload.STREAM_CODEC, SyncUnlockedSpellsPayload::handle);
        registrar.playToClient(SyncEquippedSpellsPayload.TYPE, SyncEquippedSpellsPayload.STREAM_CODEC, SyncEquippedSpellsPayload::handle);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // Ensure fireball is always unlocked by default
            UnlockedSpellsData data = serverPlayer.getData(ModAttachments.UNLOCKED_SPELLS.get());
            net.minecraft.resources.Identifier fireballId =
                    ModSpells.SPELL_REGISTRY.getKey(ModSpells.FIREBALL.value());
            if (!data.isUnlocked(fireballId)) {
                data.unlock(fireballId);
                serverPlayer.setData(ModAttachments.UNLOCKED_SPELLS.get(), data);
            }
            // Sync to client
            PacketDistributor.sendToPlayer(serverPlayer, SyncUnlockedSpellsPayload.from(data));
            EquippedSpellsData equipData = serverPlayer.getData(ModAttachments.EQUIPPED_SPELLS.get());
            PacketDistributor.sendToPlayer(serverPlayer, SyncEquippedSpellsPayload.from(equipData));
        }
    }
}
