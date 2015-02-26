package org.jglrxavpok.shady.gui;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import com.google.common.collect.Lists;

public class GuiBatchesFileList extends GuiListExtended
{

    private ArrayList<BatchFileEntry> entries;
    private int                       selected;

    public GuiBatchesFileList(Minecraft mc, int x, int y, int width, int height, int slotHeight)
    {
        super(mc, width, height, y, height - y - 40, slotHeight);
        entries = Lists.newArrayList();
        selected = -1;
    }

    public int getListWidth()
    {
        return 350;
    }

    protected int getScrollBarX()
    {
        return getListWidth() + left + 40;
    }

    public void addEntry(BatchFileEntry entry)
    {
        entries.add(entry);
        entry.list = this;
    }

    public void setSelected(int slotIndex)
    {
        selected = slotIndex;
    }

    public boolean isSelected(int slotIndex)
    {
        return selected == slotIndex;
    }

    @Override
    public IGuiListEntry getListEntry(int index)
    {
        return entries.get(index);
    }

    @Override
    protected int getSize()
    {
        return entries.size();
    }

    public int getSelectedIndex()
    {
        return selected;
    }

    public BatchFileEntry getSelected()
    {
        if(selected != -1)
            return (BatchFileEntry) getListEntry(selected);
        return null;
    }

    public void remove(int slotIndex)
    {
        entries.remove(slotIndex);
    }
}
