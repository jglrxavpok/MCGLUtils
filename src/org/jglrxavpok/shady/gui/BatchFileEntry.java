package org.jglrxavpok.shady.gui;

import java.io.File;

import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.util.ResourceLocation;

import org.jglrxavpok.shady.ShadyMod;

public class BatchFileEntry implements IGuiListEntry
{

    private FontRenderer                  font;
    public GuiBatchesFileList             list;
    private File                          file;
    private String                        filepath;
    private static final ResourceLocation deleteTexture = new ResourceLocation(ShadyMod.MODID, "textures/gui/delete.png");

    public BatchFileEntry(FontRenderer font, File file)
    {
        this.font = font;
        this.file = file;
        String[] hierarchy = file.getAbsolutePath().split("\\\\");
        int maxDepth = 5;
        maxDepth = Math.min(maxDepth, hierarchy.length);

        String[] pieces = new String[maxDepth];
        for(int i = hierarchy.length - maxDepth; i < hierarchy.length; i++ )
        {
            pieces[i - (hierarchy.length - maxDepth)] = hierarchy[i];
        }
        this.filepath = Strings.join(pieces, "/");
    }

    public File getFile()
    {
        return file;
    }

    @Override
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
        ;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        font.drawString(file.getName().replace(".batch", ""), x, y, 0xFFFFFFFF);
        font.drawString(filepath, x + 10, y + 11, 0xFF707070);

        if(isMouseOver(slotIndex, mouseX, mouseY))
        {
            int relX = mouseX - (x - 16);
            Minecraft.getMinecraft().renderEngine.bindTexture(deleteTexture);

            if(relX >= listWidth - 10)
            {
                Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 20, y + 4, 16.0F, 0.0F, 16, 16, 32.0F, 16.0F);
            }
            else
                Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 20, y + 4, 0.0F, 0.0F, 16, 16, 32.0F, 16.0F);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        if(isMouseOver(slotIndex, x, y))
        {
            boolean selectable = true;
            if(list.getSelectedIndex() == slotIndex)
            {
                if(relativeX >= list.getListWidth() - 25)
                {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiConfirmDeleteBatch(this, Minecraft.getMinecraft().currentScreen, slotIndex, list));
                }
            }
            if(selectable)
                list.setSelected(slotIndex);
        }
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
