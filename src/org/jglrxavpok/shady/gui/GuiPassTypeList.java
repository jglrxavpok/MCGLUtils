package org.jglrxavpok.shady.gui;

import java.util.ArrayList;
import java.util.Comparator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.resources.I18n;

import com.google.common.collect.Lists;

public class GuiPassTypeList extends GuiListExtended
{

    private ArrayList<PassTypeEntry> entries;
    private int                      selected;

    public GuiPassTypeList(Minecraft mc, int x, int y, int width, int height, int slotHeight)
    {
        super(mc, width, height, y, height - y - 40, slotHeight);
        entries = Lists.newArrayList();
        selected = -1;
    }

    public void addEntry(PassTypeEntry entry)
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

    public PassTypeEntry getSelected()
    {
        if(selected != -1)
            return (PassTypeEntry) getListEntry(selected);
        return null;
    }

    public void sort()
    {
        entries.sort(new Comparator<PassTypeEntry>()
        {
            @Override
            public int compare(PassTypeEntry o1, PassTypeEntry o2)
            {
                String a = I18n.format("shady.pass.type." + o1.getType());
                String b = I18n.format("shady.pass.type." + o2.getType());
                return a.compareTo(b);
            }
        });
    }

}
