package dev.ragu_rakkoon.bizarre_wizardry2.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class BizarreBookScreen extends Screen {
    private static final int NODE_WIDTH = 100;
    private static final int NODE_HEIGHT = 30;
    private static final int EDGE_THICKNESS = 2;
    private static final int MARGIN = 20;

    private static final int COLOR_NODE_LOCKED = 0xFF555555;
    private static final int COLOR_NODE_UNLOCKED = 0xFF336633;
    private static final int COLOR_NODE_BORDER_LOCKED = 0xFF888888;
    private static final int COLOR_NODE_BORDER_UNLOCKED = 0xFF55CC55;
    private static final int COLOR_EDGE = 0xFFAAAAAA;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final SpellTreeData treeData;
    private double panX;
    private double panY;
    private boolean isDragging;

    public BizarreBookScreen() {
        super(Component.translatable("screen.bizarre_wizardry2_jak.spell_tree"));
        this.treeData = new SpellTreeData();
    }

    @Override
    protected void init() {
        super.init();
        panX = (width / 2.0) - (NODE_WIDTH / 2.0);
        panY = (height / 2.0) - 40;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        // Title centered at the top
        Component titleText = this.getTitle();
        FormattedCharSequence titleOrdered = titleText.getVisualOrderText();
        int titleWidth = font.width(titleOrdered);
        graphics.text(font, titleOrdered, width / 2 - titleWidth / 2, 6, COLOR_TEXT);

        // Scissor clip the tree area
        graphics.enableScissor(MARGIN, MARGIN, width - MARGIN, height - MARGIN);

        // Draw edges first (behind nodes)
        for (SpellTreeNode node : treeData.getNodes()) {
            int nodeScreenX = (int) panX + node.getX() + NODE_WIDTH / 2;
            int nodeScreenY = (int) panY + node.getY();

            for (SpellTreeNode prereq : node.getPrerequisites()) {
                int prereqScreenX = (int) panX + prereq.getX() + NODE_WIDTH / 2;
                int prereqScreenY = (int) panY + prereq.getY() + NODE_HEIGHT;

                // Vertical line from bottom of prereq to top of node
                graphics.fill(
                        prereqScreenX - EDGE_THICKNESS / 2,
                        prereqScreenY,
                        prereqScreenX + EDGE_THICKNESS / 2,
                        nodeScreenY,
                        COLOR_EDGE
                );
            }
        }

        // Draw nodes
        for (SpellTreeNode node : treeData.getNodes()) {
            int screenX = (int) panX + node.getX();
            int screenY = (int) panY + node.getY();

            int bgColor = node.isUnlocked() ? COLOR_NODE_UNLOCKED : COLOR_NODE_LOCKED;
            int borderColor = node.isUnlocked() ? COLOR_NODE_BORDER_UNLOCKED : COLOR_NODE_BORDER_LOCKED;

            // Border (slightly larger rect behind the node)
            graphics.fill(screenX - 1, screenY - 1, screenX + NODE_WIDTH + 1, screenY + NODE_HEIGHT + 1, borderColor);
            // Node background
            graphics.fill(screenX, screenY, screenX + NODE_WIDTH, screenY + NODE_HEIGHT, bgColor);

            // Spell name centered in node
            String spellName = Component.translatable(node.getSpell().value().getTranslationKey()).getString();
            int textWidth = font.width(spellName);
            graphics.text(font, spellName, screenX + NODE_WIDTH / 2 - textWidth / 2, screenY + (NODE_HEIGHT - 8) / 2, COLOR_TEXT);
        }

        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            isDragging = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            isDragging = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (isDragging && event.button() == 0) {
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
}
