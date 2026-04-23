package dev.ragu_rakkoon.bizarre_wizardry2.client.screen;

import dev.ragu_rakkoon.bizarre_wizardry2.client.ClientUnlockedSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.network.UnlockSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.SpellUnlockCondition;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.SpellUnlockConditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BizarreBookScreen extends Screen {
    private static final int NODE_WIDTH = 100;
    private static final int NODE_HEIGHT = 30;
    private static final int EDGE_THICKNESS = 2;
    private static final int MARGIN = 20;
    private static final int SIDEBAR_WIDTH = 80;
    private static final int SIDEBAR_BUTTON_HEIGHT = 20;
    private static final int SIDEBAR_PADDING = 4;

    // Unlocked (green)
    private static final int COLOR_NODE_UNLOCKED = 0xFF336633;
    private static final int COLOR_NODE_BORDER_UNLOCKED = 0xFF55CC55;
    // Available to unlock (gold/amber)
    private static final int COLOR_NODE_AVAILABLE = 0xFF664400;
    private static final int COLOR_NODE_BORDER_AVAILABLE = 0xFFFFAA00;
    // Locked (gray)
    private static final int COLOR_NODE_LOCKED = 0xFF555555;
    private static final int COLOR_NODE_BORDER_LOCKED = 0xFF888888;

    private static final int COLOR_EDGE = 0xFFAAAAAA;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TOOLTIP_BG = 0xCC000000;
    private static final int COLOR_TOOLTIP_TEXT = 0xFFCCCCCC;

    private final SpellTreeData treeData;
    private double panX;
    private double panY;
    private boolean isDragging;
    private double mouseDownX;
    private double mouseDownY;
    private Tab activeTab = Tab.SPELL_TREE;
    private Button spellTreeButton;
    private Button spellLoadoutButton;

    public BizarreBookScreen() {
        super(Component.translatable("screen.bizarre_wizardry2_jak.spell_tree"));
        this.treeData = new SpellTreeData();
    }

    @Override
    protected void init() {
        super.init();
        panX = SIDEBAR_WIDTH + ((width - SIDEBAR_WIDTH) / 2.0) - (NODE_WIDTH / 2.0);
        panY = (height / 2.0) - 40;

        spellTreeButton = addRenderableWidget(Button.builder(
                Component.literal("Spell Tree"), btn -> setActiveTab(Tab.SPELL_TREE))
                .bounds(SIDEBAR_PADDING, 20, SIDEBAR_WIDTH - SIDEBAR_PADDING * 2, SIDEBAR_BUTTON_HEIGHT)
                .build());

        spellLoadoutButton = addRenderableWidget(Button.builder(
                Component.literal("Spell Loadout"), btn -> setActiveTab(Tab.SPELL_LOADOUT))
                .bounds(SIDEBAR_PADDING, 20 + SIDEBAR_BUTTON_HEIGHT + SIDEBAR_PADDING, SIDEBAR_WIDTH - SIDEBAR_PADDING * 2, SIDEBAR_BUTTON_HEIGHT)
                .build());

        updateButtonStates();
    }

    private void setActiveTab(Tab tab) {
        this.activeTab = tab;
        updateButtonStates();
    }

    private void updateButtonStates() {
        spellTreeButton.active = activeTab != Tab.SPELL_TREE;
        spellLoadoutButton.active = activeTab != Tab.SPELL_LOADOUT;
    }

    private Identifier spellId(SpellTreeNode node) {
        return ModSpells.SPELL_REGISTRY.getKey(node.getSpell().value());
    }

    private NodeState getNodeState(SpellTreeNode node) {
        Identifier id = spellId(node);
        if (ClientUnlockedSpells.isUnlocked(id)) return NodeState.UNLOCKED;

        SpellUnlockCondition cond = node.getCondition();
        var player = Minecraft.getInstance().player;
        if (player == null) return NodeState.LOCKED;

        boolean condMet;
        if (cond.isAlways()) {
            condMet = true;
        } else {
            int statValue = player.getStats().getValue(cond.getStat());
            condMet = cond.isMet(statValue);
        }
        if (!condMet) return NodeState.LOCKED;

        boolean prereqsMet = node.getPrerequisites().stream()
                .allMatch(prereq -> ClientUnlockedSpells.isUnlocked(spellId(prereq)));
        return prereqsMet ? NodeState.AVAILABLE : NodeState.LOCKED;
    }

    private String getTooltip(SpellTreeNode node, NodeState state) {
        return switch (state) {
            case UNLOCKED -> "Unlocked";
            case AVAILABLE -> "Click to unlock!";
            case LOCKED -> {
                SpellUnlockCondition cond = node.getCondition();
                if (cond.isAlways()) yield "Locked";
                var player = Minecraft.getInstance().player;
                if (player == null) yield "Locked";
                int statValue = player.getStats().getValue(cond.getStat());
                int divisor = SpellUnlockConditions.getDisplayDivisor(spellId(node));
                yield cond.getProgressText(statValue, divisor);
            }
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Title
        FormattedCharSequence titleOrdered = this.getTitle().getVisualOrderText();
        int titleWidth = font.width(titleOrdered);
        graphics.text(font, titleOrdered, SIDEBAR_WIDTH + (width - SIDEBAR_WIDTH) / 2 - titleWidth / 2, 6, COLOR_TEXT);

        int contentLeft = SIDEBAR_WIDTH + MARGIN;

        if (activeTab == Tab.SPELL_TREE) {
            graphics.enableScissor(contentLeft, MARGIN, width - MARGIN, height - MARGIN);

            // Edges
            for (SpellTreeNode node : treeData.getNodes()) {
                int nodeScreenX = (int) panX + node.getX() + NODE_WIDTH / 2;
                int nodeScreenY = (int) panY + node.getY();
                for (SpellTreeNode prereq : node.getPrerequisites()) {
                    int prereqScreenX = (int) panX + prereq.getX() + NODE_WIDTH / 2;
                    int prereqScreenY = (int) panY + prereq.getY() + NODE_HEIGHT;
                    graphics.fill(
                            prereqScreenX - EDGE_THICKNESS / 2, prereqScreenY,
                            prereqScreenX + EDGE_THICKNESS / 2, nodeScreenY,
                            COLOR_EDGE);
                }
            }

            // Nodes
            SpellTreeNode hoveredNode = null;
            NodeState hoveredState = NodeState.LOCKED;
            for (SpellTreeNode node : treeData.getNodes()) {
                int screenX = (int) panX + node.getX();
                int screenY = (int) panY + node.getY();
                NodeState state = getNodeState(node);

                int bgColor = switch (state) {
                    case UNLOCKED -> COLOR_NODE_UNLOCKED;
                    case AVAILABLE -> COLOR_NODE_AVAILABLE;
                    case LOCKED -> COLOR_NODE_LOCKED;
                };
                int borderColor = switch (state) {
                    case UNLOCKED -> COLOR_NODE_BORDER_UNLOCKED;
                    case AVAILABLE -> COLOR_NODE_BORDER_AVAILABLE;
                    case LOCKED -> COLOR_NODE_BORDER_LOCKED;
                };

                graphics.fill(screenX - 1, screenY - 1, screenX + NODE_WIDTH + 1, screenY + NODE_HEIGHT + 1, borderColor);
                graphics.fill(screenX, screenY, screenX + NODE_WIDTH, screenY + NODE_HEIGHT, bgColor);

                String spellName = Component.translatable(node.getSpell().value().getTranslationKey()).getString();
                int textWidth = font.width(spellName);
                graphics.text(font, spellName,
                        screenX + NODE_WIDTH / 2 - textWidth / 2,
                        screenY + (NODE_HEIGHT - 8) / 2,
                        COLOR_TEXT);

                if (mouseX >= screenX && mouseX < screenX + NODE_WIDTH
                        && mouseY >= screenY && mouseY < screenY + NODE_HEIGHT) {
                    hoveredNode = node;
                    hoveredState = state;
                }
            }

            graphics.disableScissor();

            // Tooltip
            if (hoveredNode != null) {
                String tooltip = getTooltip(hoveredNode, hoveredState);
                if (!tooltip.isEmpty()) {
                    int tw = font.width(tooltip);
                    int tx = Math.min(mouseX + 6, width - tw - 4);
                    int ty = mouseY - 14;
                    graphics.fill(tx - 2, ty - 2, tx + tw + 2, ty + 10, COLOR_TOOLTIP_BG);
                    graphics.text(font, tooltip, tx, ty, COLOR_TOOLTIP_TEXT);
                }
            }
        } else if (activeTab == Tab.SPELL_LOADOUT) {
            String placeholder = "Spell Loadout - Coming Soon";
            int tw = font.width(placeholder);
            graphics.text(font, placeholder,
                    contentLeft + (width - contentLeft - MARGIN) / 2 - tw / 2,
                    height / 2,
                    COLOR_TEXT);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (activeTab == Tab.SPELL_TREE && event.button() == 0) {
            mouseDownX = event.x();
            mouseDownY = event.y();
            isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activeTab == Tab.SPELL_TREE && event.button() == 0) {
            isDragging = false;
            double dx = event.x() - mouseDownX;
            double dy = event.y() - mouseDownY;
            if (Math.abs(dx) < 4 && Math.abs(dy) < 4) {
                handleNodeClick((int) event.x(), (int) event.y());
            }
        }
        return super.mouseReleased(event);
    }

    private void handleNodeClick(int mouseX, int mouseY) {
        for (SpellTreeNode node : treeData.getNodes()) {
            int screenX = (int) panX + node.getX();
            int screenY = (int) panY + node.getY();
            if (mouseX >= screenX && mouseX < screenX + NODE_WIDTH
                    && mouseY >= screenY && mouseY < screenY + NODE_HEIGHT) {
                if (getNodeState(node) == NodeState.AVAILABLE) {
                    Identifier id = spellId(node);
                    ClientPacketDistributor.sendToServer(new UnlockSpellPayload(id));
                    // Optimistic update so the UI reacts immediately
                    ClientUnlockedSpells.set(
                            Stream.concat(ClientUnlockedSpells.getAll().stream(), Stream.of(id))
                                    .collect(Collectors.toSet()));
                }
                return;
            }
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (activeTab == Tab.SPELL_TREE && isDragging && event.button() == 0) {
            panX += dx;
            panY += dy;
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum NodeState {
        UNLOCKED, AVAILABLE, LOCKED
    }

    private enum Tab {
        SPELL_TREE, SPELL_LOADOUT
    }
}
