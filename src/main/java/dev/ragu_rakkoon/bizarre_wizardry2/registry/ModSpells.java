package dev.ragu_rakkoon.bizarre_wizardry2.registry;

import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModSpells {
    public static final ResourceKey<Registry<Spell>> SPELL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "spells"));

    public static final Registry<Spell> SPELL_REGISTRY = new RegistryBuilder<>(SPELL_REGISTRY_KEY)
            .sync(true)
            .create();

    public static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(SPELL_REGISTRY, BizarreWizardry2.MOD_ID);

    // Register spells here, e.g.:
    // public static final DeferredHolder<Spell, FireballSpell> FIREBALL = SPELLS.register("fireball", FireballSpell::new);

    public static void register(IEventBus modEventBus) {
        SPELLS.register(modEventBus);
    }
}
