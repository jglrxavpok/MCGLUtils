package org.jglrxavpok.shady.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;

public class PassTypeEntry implements IGuiListEntry
{

    private FontRenderer   font;
    public GuiPassTypeList list;
    private String         type;

    public PassTypeEntry(FontRenderer font, String type)
    {
        this.font = font;
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
        ;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        font.drawString(I18n.format("shady.pass.type." + type), x, y, 0xFFFFFFFF);
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        if(isMouseOver(slotIndex, x, y))
            list.setSelected(slotIndex);
        return false;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    private boolean isMouseOver(int slotIndex, int x, int y)
    {
        return list.getSlotIndexFromScreenCoords(x, y) == slotIndex && list.isMouseYWithinSlotBounds(y);
    }

}
