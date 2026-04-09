package dev.ragu_rakkoon.bizarre_wizardry2.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ragu_rakkoon.bizarre_wizardry2.BizarreWizardry2;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(BizarreWizardry2.MOD_ID, "bizarre_wizardry2")
    );

    public static final KeyMapping CYCLE_SPELL_KEY = new KeyMapping(
            "key.bizarre_wizardry2_jak.cycle_spell",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
}
