package org.jglrxavpok.shady.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class GuiIconButton extends GuiButton
{

    private ResourceLocation icon;

    public GuiIconButton(int buttonId, int x, int y, ResourceLocation icon)
    {
        super(buttonId, x, y, 20, 20, "");
        this.icon = icon;
    }
    
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        super.drawButton(mc, mouseX, mouseY);
        if(visible)
        {
            mc.renderEngine.bindTexture(icon);
            Gui.drawModalRectWithCustomSizedTexture(xPosition+1, yPosition+1, 0, 0, 16, 16, 16, 16);
        }
    }

}
