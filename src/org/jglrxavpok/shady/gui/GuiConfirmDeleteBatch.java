package org.jglrxavpok.shady.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiConfirmDeleteBatch extends GuiScreen
{

    public static int          CONFIRM_BUTTON = 0;
    public static int          CANCEL_BUTTON  = 1;
    private BatchFileEntry     entry;
    private GuiScreen          parent;
    private int                index;
    private GuiBatchesFileList list;

    public GuiConfirmDeleteBatch(BatchFileEntry entry, GuiScreen parent, int index, GuiBatchesFileList list)
    {
        this.entry = entry;
        this.parent = parent;
        this.index = index;
        this.list = list;
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        buttonList.clear();

        buttonList.add(new GuiButton(CANCEL_BUTTON, width / 2 + 5, height - height / 8, I18n.format("shady.cancel")));
        buttonList.add(new GuiButton(CONFIRM_BUTTON, width / 2 - 200 - 5, height - height / 8, I18n.format("shady.confirm")));
    }

    public void drawScreen(int mx, int my, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, ChatFormatting.UNDERLINE + I18n.format("shady.delBatch.title"), width / 2, 30, 0xFFFFFFFF);
        fontRendererObj.drawSplitString(I18n.format("shady.delBatch.explain", entry.getFile().getName().replace(".batch", "")), 40, 50, width - 40 - 40, 0xFFFFFFFF);
        super.drawScreen(mx, my, partialTicks);
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == CONFIRM_BUTTON)
        {
            entry.getFile().delete();
            list.remove(index);
            list.setSelected(-1);
            mc.displayGuiScreen(parent);
        }
        else if(button.id == CANCEL_BUTTON)
        {
            mc.displayGuiScreen(parent);
        }
    }

}
