package dev.ragu_rakkoon.bizarre_wizardry2.item;

import dev.ragu_rakkoon.bizarre_wizardry2.data.UnlockedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModAttachments;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModDataComponents;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ZanpakutoItem extends Item {
    public ZanpakutoItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            syncSpellList(stack, player);
        }

        ZanpakutoSpellData data = stack.get(ModDataComponents.ZANPAKUTO_SPELL_DATA.get());

        if (data == null || data.spells().isEmpty()) {
            return InteractionResult.PASS;
        }

        Spell spell = data.selectedSpell();
        spell.cast(level, player, hand, stack);
        player.getCooldowns().addCooldown(stack, spell.getCooldown());
        return InteractionResult.SUCCESS;
    }

    /**
     * Rebuilds the zanpakuto's spell list from the player's unlocked spells, using
     * registry iteration order for a consistent cycle. Preserves the currently
     * selected spell if it is still present.
     */
    public static void syncSpellList(ItemStack stack, Player player) {
        UnlockedSpellsData unlockedData = player.getData(ModAttachments.UNLOCKED_SPELLS.get());

        // getEntries() preserves registration order, giving a stable cycle sequence
        List<Holder<Spell>> registryOrdered = ModSpells.SPELLS.getEntries().stream()
                .filter(holder -> {
                    Identifier id = ModSpells.SPELL_REGISTRY.getKey(holder.value());
                    return id != null && unlockedData.isUnlocked(id);
                })
                .map(h -> (Holder<Spell>) h)
                .toList();

        if (registryOrdered.isEmpty()) return;

        ZanpakutoSpellData current = stack.get(ModDataComponents.ZANPAKUTO_SPELL_DATA.get());
        int newIndex = 0;

        if (current != null && !current.spells().isEmpty()) {
            Identifier selectedId = ModSpells.SPELL_REGISTRY.getKey(
                    current.spells().get(current.selectedIndex()).value());
            for (int i = 0; i < registryOrdered.size(); i++) {
                Identifier id = ModSpells.SPELL_REGISTRY.getKey(registryOrdered.get(i).value());
                if (selectedId != null && selectedId.equals(id)) {
                    newIndex = i;
                    break;
                }
            }
        }

        stack.set(ModDataComponents.ZANPAKUTO_SPELL_DATA.get(),
                new ZanpakutoSpellData(registryOrdered, newIndex));
    }

    public static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                attackDamage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_ID,
                                attackSpeed,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
