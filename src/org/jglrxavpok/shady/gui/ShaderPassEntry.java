package org.jglrxavpok.shady.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.shaders.PassRegistry;
import org.jglrxavpok.shady.shaders.ShaderPass;

public class ShaderPassEntry implements IGuiListEntry
{

    private ShaderPass                    pass;
    private FontRenderer                  font;
    public GuiShaderList                  list;
    private String                        name;
    private static final ResourceLocation resourcepackTextures = new ResourceLocation("textures/gui/resource_packs.png");
    private static final ResourceLocation deleteTexture        = new ResourceLocation(ShadyMod.MODID, "textures/gui/delete.png");

    public ShaderPassEntry(FontRenderer font, ShaderPass pass)
    {
        this(font, pass, pass.getName());
    }

    public ShaderPassEntry(FontRenderer font, ShaderPass pass, String name)
    {
        this.font = font;
        this.pass = pass;
        this.name = name;
    }

    protected boolean canGoUp()
    {
        int i = list.indexOf(this);
        return i > 0;
    }

    protected boolean canGoDown()
    {
        int i = list.indexOf(this);
        return i >= 0 && i < list.getSize() - 1;
    }

    public ShaderPass getPass()
    {
        return pass;
    }

    @Override
    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
        ;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        font.drawString(name, x + 16, y, 0xFFFFFFFF);
        font.drawString(I18n.format("shady.pass.type." + PassRegistry.getID(pass)), x + 10 + 16, y + 10, 0xFF707070);

        if(isMouseOver(slotIndex, mouseX, mouseY))
        {
            int relX = mouseX - (x - 16);
            int relY = mouseY - y;
            Minecraft.getMinecraft().renderEngine.bindTexture(resourcepackTextures);
            if(this.canGoUp())
            {
                if(relX < 32 && relX > 16 && relY < 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x - 16, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x - 16, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if(this.canGoDown())
            {
                if(relX < 32 && relX > 16 && relY > 16)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x - 16, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x - 16, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

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
                if(canGoUp())
                {
                    if(relativeX <= 16 && relativeY < 16)
                    {
                        list.remove(slotIndex);
                        list.addEntry(this, slotIndex - 1);
                        list.setSelected(slotIndex - 1);
                        selectable = false;
                    }
                }
                if(canGoDown())
                {
                    if(relativeX <= 16 && relativeY >= 16)
                    {
                        list.remove(slotIndex);
                        list.addEntry(this, slotIndex + 1);
                        list.setSelected(slotIndex + 1);
                        selectable = false;
                    }
                }
                if(relativeX >= list.getListWidth() - 25)
                {
                    list.remove(slotIndex);
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPass(ShaderPass pass)
    {
        this.pass = pass;
    }

}
