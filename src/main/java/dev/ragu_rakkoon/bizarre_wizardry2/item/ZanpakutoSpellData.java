package dev.ragu_rakkoon.bizarre_wizardry2.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import net.minecraft.core.Holder;

import java.util.List;

public record ZanpakutoSpellData(List<Holder<Spell>> spells, int selectedIndex) {
    public static final Codec<ZanpakutoSpellData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModSpells.SPELL_REGISTRY.holderByNameCodec().listOf().fieldOf("spells").forGetter(ZanpakutoSpellData::spells),
            Codec.INT.fieldOf("selected_index").forGetter(ZanpakutoSpellData::selectedIndex)
    ).apply(instance, ZanpakutoSpellData::new));

    public Spell selectedSpell() {
        return spells.get(selectedIndex).value();
    }

    public ZanpakutoSpellData withSelectedIndex(int index) {
        return new ZanpakutoSpellData(spells, index);
    }
}
