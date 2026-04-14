package dev.ragu_rakkoon.bizarre_wizardry2.client.screen;

import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;

import java.util.List;

public class SpellTreeData {
    private final List<SpellTreeNode> nodes;

    public SpellTreeData() {
        SpellTreeNode fireball = new SpellTreeNode(ModSpells.FIREBALL, 0, 0, List.of(), false);
        SpellTreeNode stomp = new SpellTreeNode(ModSpells.STOMP, 0, 80, List.of(fireball), false);
        SpellTreeNode dash = new SpellTreeNode(ModSpells.DASH, 0, 160, List.of(stomp), false);

        this.nodes = List.of(fireball, stomp, dash);
    }

    public List<SpellTreeNode> getNodes() {
        return nodes;
    }
}
