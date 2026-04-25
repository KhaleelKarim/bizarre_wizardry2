package dev.ragu_rakkoon.bizarre_wizardry2.client.screen;

import dev.ragu_rakkoon.bizarre_wizardry2.client.ClientEquippedSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.client.ClientUnlockedSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.data.EquippedSpellsData;
import dev.ragu_rakkoon.bizarre_wizardry2.network.EquipSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.network.UnlockSpellPayload;
import dev.ragu_rakkoon.bizarre_wizardry2.registry.ModSpells;
import dev.ragu_rakkoon.bizarre_wizardry2.spell.Spell;
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

import java.util.ArrayList;
import java.util.List;
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

    private static final int COLOR_PANEL_BG = 0xFF1A1A2E;
    private static final int COLOR_PANEL_BORDER = 0xFF4A4A6A;
    private static final int PANEL_GAP = 4;

    // Loadout panel constants
    private static final int SPELL_SLOT_SIZE = 30;
    private static final int SPELL_SLOT_GAP = 8;
    private static final int SPELL_LIST_ITEM_HEIGHT = 24;
    private static final int PANEL_INNER_PADDING = 6;
    private static final int COLOR_SLOT_EMPTY = 0xFF2A2A3E;
    private static final int COLOR_SLOT_BORDER = 0xFF6A6A8A;
    private static final int COLOR_SLOT_HIGHLIGHT = 0xFF7A7AFF;
    private static final int COLOR_LIST_ITEM_BG = 0xFF2A2A3E;
    private static final int COLOR_LIST_ITEM_HOVER = 0xFF3A3A5E;
    private static final int COLOR_SELECTED = 0xFF4A4A8E;

    private final SpellTreeData treeData;
    private double panX;
    private double panY;
    private boolean isDragging;
    private double mouseDownX;
    private double mouseDownY;
    private Tab activeTab = Tab.SPELL_TREE;
    private Button spellTreeButton;
    private Button spellLoadoutButton;

    // Loadout state
    private Identifier[] equippedSlots;
    private Identifier selectedSpell = null;
    private double spellListScrollOffset = 0;
    private Identifier draggedSpell = null;
    private double dragX, dragY;
    private double loadoutMouseDownX, loadoutMouseDownY;

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
        initEquippedSlots();
    }

    private void setActiveTab(Tab tab) {
        this.activeTab = tab;
        updateButtonStates();
    }

    private void updateButtonStates() {
        spellTreeButton.active = activeTab != Tab.SPELL_TREE;
        spellLoadoutButton.active = activeTab != Tab.SPELL_LOADOUT;
    }

    private void initEquippedSlots() {
        List<Identifier> current = ClientEquippedSpells.getAll();
        int maxSlots = ClientEquippedSpells.getMaxSlots();
        equippedSlots = new Identifier[maxSlots];
        for (int i = 0; i < maxSlots; i++) {
            Identifier id = current.get(i);
            equippedSlots[i] = EquippedSpellsData.EMPTY.equals(id) ? null : id;
        }
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
            int contentRight = width - MARGIN;
            int contentTop = MARGIN;
            int contentBottom = height - MARGIN;
            int midX = contentLeft + (contentRight - contentLeft) / 2;
            int midY = contentTop + (contentBottom - contentTop) / 2;

            int topLeftX1 = contentLeft, topLeftY1 = contentTop;
            int topLeftX2 = midX - PANEL_GAP / 2, topLeftY2 = midY - PANEL_GAP / 2;
            int botLeftX1 = contentLeft, botLeftY1 = midY + PANEL_GAP / 2;
            int botLeftX2 = midX - PANEL_GAP / 2, botLeftY2 = contentBottom;
            int rightX1 = midX + PANEL_GAP / 2, rightY1 = contentTop;
            int rightX2 = contentRight, rightY2 = contentBottom;

            // Top-left panel (equipped slots)
            drawPanel(graphics, topLeftX1, topLeftY1, topLeftX2, topLeftY2);
            drawLoadoutEquipPanel(graphics, topLeftX1, topLeftY1, topLeftX2, topLeftY2, mouseX, mouseY);

            // Bottom-left panel (description)
            drawPanel(graphics, botLeftX1, botLeftY1, botLeftX2, botLeftY2);
            drawLoadoutDescPanel(graphics, botLeftX1, botLeftY1, botLeftX2, botLeftY2);

            // Right panel (spell list)
            drawPanel(graphics, rightX1, rightY1, rightX2, rightY2);
            drawLoadoutSpellList(graphics, rightX1, rightY1, rightX2, rightY2, mouseX, mouseY);

            // Draw dragged spell at cursor
            if (draggedSpell != null) {
                Spell spell = ModSpells.SPELL_REGISTRY.getValue(draggedSpell);
                if (spell != null) {
                    String name = Component.translatable(spell.getTranslationKey()).getString();
                    int tw = font.width(name);
                    graphics.fill((int) dragX - tw / 2 - 2, (int) dragY - 6, (int) dragX + tw / 2 + 2, (int) dragY + 10, 0xAA000000);
                    graphics.text(font, name, (int) dragX - tw / 2, (int) dragY - 4, 0xCCFFFFFF);
                }
            }
        }
    }

    private void drawPanel(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2) {
        graphics.fill(x1, y1, x2, y2, COLOR_PANEL_BORDER);
        graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, COLOR_PANEL_BG);
    }

    private void drawLoadoutEquipPanel(GuiGraphicsExtractor graphics, int px1, int py1, int px2, int py2, int mouseX, int mouseY) {
        int totalWidth = SPELL_SLOT_SIZE * equippedSlots.length + SPELL_SLOT_GAP * (equippedSlots.length - 1);
        int startX = px1 + (px2 - px1) / 2 - totalWidth / 2;
        int startY = py1 + (py2 - py1) / 2 - SPELL_SLOT_SIZE / 2;

        for (int i = 0; i < equippedSlots.length; i++) {
            int sx = startX + i * (SPELL_SLOT_SIZE + SPELL_SLOT_GAP);
            int sy = startY;

            boolean hovering = mouseX >= sx && mouseX < sx + SPELL_SLOT_SIZE
                    && mouseY >= sy && mouseY < sy + SPELL_SLOT_SIZE;
            boolean dropTarget = hovering && draggedSpell != null;

            int borderColor = dropTarget ? COLOR_SLOT_HIGHLIGHT : COLOR_SLOT_BORDER;
            graphics.fill(sx - 1, sy - 1, sx + SPELL_SLOT_SIZE + 1, sy + SPELL_SLOT_SIZE + 1, borderColor);
            graphics.fill(sx, sy, sx + SPELL_SLOT_SIZE, sy + SPELL_SLOT_SIZE, COLOR_SLOT_EMPTY);

            if (equippedSlots[i] != null) {
                Spell spell = ModSpells.SPELL_REGISTRY.getValue(equippedSlots[i]);
                if (spell != null) {
                    String name = Component.translatable(spell.getTranslationKey()).getString();
                    // Truncate if needed to fit in the slot
                    String display = font.width(name) > SPELL_SLOT_SIZE - 4
                            ? font.plainSubstrByWidth(name, SPELL_SLOT_SIZE - 8) + ".."
                            : name;
                    int tw = font.width(display);
                    graphics.text(font, display, sx + SPELL_SLOT_SIZE / 2 - tw / 2,
                            sy + SPELL_SLOT_SIZE / 2 - 4, COLOR_TEXT);
                }
            } else {
                String empty = "-";
                int tw = font.width(empty);
                graphics.text(font, empty, sx + SPELL_SLOT_SIZE / 2 - tw / 2,
                        sy + SPELL_SLOT_SIZE / 2 - 4, COLOR_TOOLTIP_TEXT);
            }
        }
    }

    private void drawLoadoutDescPanel(GuiGraphicsExtractor graphics, int px1, int py1, int px2, int py2) {
        int textX = px1 + PANEL_INNER_PADDING + 1;
        int textY = py1 + PANEL_INNER_PADDING + 1;

        if (selectedSpell == null) {
            graphics.text(font, "Select a spell to view details", textX, textY, COLOR_TOOLTIP_TEXT);
            return;
        }

        Spell spell = ModSpells.SPELL_REGISTRY.getValue(selectedSpell);
        if (spell == null) return;

        String name = Component.translatable(spell.getTranslationKey()).getString();
        graphics.text(font, name, textX, textY, COLOR_TEXT);
        textY += 14;
        graphics.text(font, "Mana Cost: " + spell.getManaCost(), textX, textY, COLOR_TOOLTIP_TEXT);
        textY += 12;
        String cooldown = String.format("Cooldown: %.1fs", spell.getCooldown() / 20.0);
        graphics.text(font, cooldown, textX, textY, COLOR_TOOLTIP_TEXT);
    }

    private void drawLoadoutSpellList(GuiGraphicsExtractor graphics, int px1, int py1, int px2, int py2, int mouseX, int mouseY) {
        List<Identifier> unlocked = getSortedUnlockedSpells();
        int innerX1 = px1 + 1;
        int innerY1 = py1 + 1;
        int innerX2 = px2 - 1;
        int innerY2 = py2 - 1;
        int panelHeight = innerY2 - innerY1;

        // Clamp scroll
        int totalHeight = unlocked.size() * SPELL_LIST_ITEM_HEIGHT;
        int maxScroll = Math.max(0, totalHeight - panelHeight);
        spellListScrollOffset = Math.max(0, Math.min(spellListScrollOffset, maxScroll));

        graphics.enableScissor(innerX1, innerY1, innerX2, innerY2);

        for (int i = 0; i < unlocked.size(); i++) {
            Identifier id = unlocked.get(i);
            int itemY = innerY1 + i * SPELL_LIST_ITEM_HEIGHT - (int) spellListScrollOffset;

            if (itemY + SPELL_LIST_ITEM_HEIGHT < innerY1 || itemY > innerY2) continue;

            boolean hovered = mouseX >= innerX1 && mouseX < innerX2
                    && mouseY >= itemY && mouseY < itemY + SPELL_LIST_ITEM_HEIGHT;
            boolean selected = id.equals(selectedSpell);

            int bg = selected ? COLOR_SELECTED : (hovered ? COLOR_LIST_ITEM_HOVER : COLOR_LIST_ITEM_BG);
            graphics.fill(innerX1, itemY, innerX2, itemY + SPELL_LIST_ITEM_HEIGHT, bg);

            Spell spell = ModSpells.SPELL_REGISTRY.getValue(id);
            if (spell != null) {
                String name = Component.translatable(spell.getTranslationKey()).getString();
                graphics.text(font, name, innerX1 + PANEL_INNER_PADDING,
                        itemY + SPELL_LIST_ITEM_HEIGHT / 2 - 4, COLOR_TEXT);
            }
        }

        graphics.disableScissor();
    }

    private List<Identifier> getSortedUnlockedSpells() {
        return new ArrayList<>(ClientUnlockedSpells.getAll());
    }

    private int getHoveredSlot(int mouseX, int mouseY, int px1, int py1, int px2, int py2) {
        int totalWidth = SPELL_SLOT_SIZE * equippedSlots.length + SPELL_SLOT_GAP * (equippedSlots.length - 1);
        int startX = px1 + (px2 - px1) / 2 - totalWidth / 2;
        int startY = py1 + (py2 - py1) / 2 - SPELL_SLOT_SIZE / 2;

        for (int i = 0; i < equippedSlots.length; i++) {
            int sx = startX + i * (SPELL_SLOT_SIZE + SPELL_SLOT_GAP);
            if (mouseX >= sx && mouseX < sx + SPELL_SLOT_SIZE
                    && mouseY >= startY && mouseY < startY + SPELL_SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private Identifier getHoveredListSpell(int mouseX, int mouseY, int px1, int py1, int px2, int py2) {
        int innerX1 = px1 + 1;
        int innerY1 = py1 + 1;
        int innerX2 = px2 - 1;
        int innerY2 = py2 - 1;

        if (mouseX < innerX1 || mouseX >= innerX2 || mouseY < innerY1 || mouseY >= innerY2) return null;

        List<Identifier> unlocked = getSortedUnlockedSpells();
        int relY = mouseY - innerY1 + (int) spellListScrollOffset;
        int index = relY / SPELL_LIST_ITEM_HEIGHT;
        if (index >= 0 && index < unlocked.size()) {
            return unlocked.get(index);
        }
        return null;
    }

    private boolean isInPanel(int mouseX, int mouseY, int px1, int py1, int px2, int py2) {
        return mouseX >= px1 && mouseX < px2 && mouseY >= py1 && mouseY < py2;
    }

    // Loadout panel bounds — computed from screen dimensions
    private int loadoutContentLeft() { return SIDEBAR_WIDTH + MARGIN; }
    private int loadoutContentRight() { return width - MARGIN; }
    private int loadoutMidX() { return loadoutContentLeft() + (loadoutContentRight() - loadoutContentLeft()) / 2; }
    private int loadoutMidY() { return MARGIN + (height - MARGIN - MARGIN) / 2; }

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
        if (activeTab == Tab.SPELL_LOADOUT && event.button() == 0) {
            int mx = (int) event.x(), my = (int) event.y();
            loadoutMouseDownX = event.x();
            loadoutMouseDownY = event.y();

            // Check right panel for drag start
            int rightX1 = loadoutMidX() + PANEL_GAP / 2, rightY1 = MARGIN;
            int rightX2 = loadoutContentRight(), rightY2 = height - MARGIN;
            Identifier hovered = getHoveredListSpell(mx, my, rightX1, rightY1, rightX2, rightY2);
            if (hovered != null) {
                draggedSpell = hovered;
                dragX = event.x();
                dragY = event.y();
                return true;
            }

            // Check equipped slots for selection
            int topLeftX1 = loadoutContentLeft(), topLeftY1 = MARGIN;
            int topLeftX2 = loadoutMidX() - PANEL_GAP / 2, topLeftY2 = loadoutMidY() - PANEL_GAP / 2;
            int slot = getHoveredSlot(mx, my, topLeftX1, topLeftY1, topLeftX2, topLeftY2);
            if (slot >= 0 && equippedSlots[slot] != null) {
                selectedSpell = equippedSlots[slot];
                return true;
            }

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
        if (activeTab == Tab.SPELL_LOADOUT && event.button() == 0 && draggedSpell != null) {
            int mx = (int) event.x(), my = (int) event.y();
            double dx = event.x() - loadoutMouseDownX;
            double dy = event.y() - loadoutMouseDownY;

            if (Math.abs(dx) < 4 && Math.abs(dy) < 4) {
                // Treat as a click — select the spell
                selectedSpell = draggedSpell;
            } else {
                // Check if dropped on a slot
                int topLeftX1 = loadoutContentLeft(), topLeftY1 = MARGIN;
                int topLeftX2 = loadoutMidX() - PANEL_GAP / 2, topLeftY2 = loadoutMidY() - PANEL_GAP / 2;
                int slot = getHoveredSlot(mx, my, topLeftX1, topLeftY1, topLeftX2, topLeftY2);
                if (slot >= 0) {
                    // Clear the spell from any other slot it may occupy
                    for (int i = 0; i < equippedSlots.length; i++) {
                        if (draggedSpell.equals(equippedSlots[i])) {
                            equippedSlots[i] = null;
                        }
                    }
                    equippedSlots[slot] = draggedSpell;
                    selectedSpell = draggedSpell;
                    ClientPacketDistributor.sendToServer(new EquipSpellPayload(draggedSpell, slot));
                }
            }
            draggedSpell = null;
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
        if (activeTab == Tab.SPELL_LOADOUT && draggedSpell != null && event.button() == 0) {
            dragX = event.x();
            dragY = event.y();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (activeTab == Tab.SPELL_LOADOUT) {
            int rightX1 = loadoutMidX() + PANEL_GAP / 2, rightY1 = MARGIN;
            int rightX2 = loadoutContentRight(), rightY2 = height - MARGIN;
            if (isInPanel((int) mouseX, (int) mouseY, rightX1, rightY1, rightX2, rightY2)) {
                spellListScrollOffset -= scrollY * SPELL_LIST_ITEM_HEIGHT;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
