package dev.ragu_rakkoon.bizarre_wizardry2.client.screen;

import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.SpellUnlockCondition;
import net.minecraft.core.Holder;

import java.util.List;

public class SpellTreeNode {
    private final Holder<Spell> spell;
    private final int x;
    private final int y;
    private final List<SpellTreeNode> prerequisites;
    private final SpellUnlockCondition condition;

    public SpellTreeNode(Holder<Spell> spell, int x, int y, List<SpellTreeNode> prerequisites, SpellUnlockCondition condition) {
        this.spell = spell;
        this.x = x;
        this.y = y;
        this.prerequisites = prerequisites;
        this.condition = condition;
    }

    public Holder<Spell> getSpell() {
        return spell;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<SpellTreeNode> getPrerequisites() {
        return prerequisites;
    }

    public SpellUnlockCondition getCondition() {
        return condition;
    }
}
