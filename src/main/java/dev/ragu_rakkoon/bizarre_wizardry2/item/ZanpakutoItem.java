package dev.ragu_rakkoon.bizarre_wizardry2.item;

import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModAttachments;
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

public class ZanpakutoItem extends Item {
    public ZanpakutoItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);

        EquippedSpellsData equipData = player.getData(ModAttachments.EQUIPPED_SPELLS.get());
        Identifier spellId = equipData.getSelectedSpellId();

        if (EquippedSpellsData.EMPTY.equals(spellId)) return InteractionResult.PASS;

        Spell spell = ModSpells.SPELL_REGISTRY.get(spellId).map(Holder.Reference::value).orElse(null);
        if (spell == null) return InteractionResult.PASS;

        spell.cast(level, player, hand, stack);
        player.getCooldowns().addCooldown(stack, spell.getCooldown());
        return InteractionResult.SUCCESS;
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
