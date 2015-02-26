package org.jglrxavpok.shady.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.jglrxavpok.shady.shaders.PassRegistry;

public class GuiSelectPassType extends GuiScreen
{

    private static final int BACK_BUTTON = 0;
    private static final int ADD_BUTTON  = 1;
    private GuiEditPass      parent;
    private GuiPassTypeList  typeList;
    private GuiButton        addButton;

    public GuiSelectPassType(GuiEditPass parent)
    {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        buttonList.clear();

        typeList = new GuiPassTypeList(mc, 20, 30, width, height, 15);

        for(String id : PassRegistry.getAllIDs())
        {
            typeList.addEntry(new PassTypeEntry(fontRendererObj, id));
        }

        typeList.sort();

        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 + 5, height - height / 8, I18n.format("gui.back")));
        addButton = new GuiButton(ADD_BUTTON, width / 2 - 200 - 5, height - height / 8, I18n.format("shady.select"));
        buttonList.add(addButton);
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.typeList.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if(mouseButton != 0 || !this.typeList.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if(state != 0 || !this.typeList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        addButton.enabled = typeList.getSelectedIndex() != -1;
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == ADD_BUTTON)
        {
            parent.confirmType(typeList.getSelected().getType());
            mc.displayGuiScreen(parent);
        }
        else if(button.id == BACK_BUTTON)
        {
            mc.displayGuiScreen(parent);
        }
        else
            typeList.actionPerformed(button);
    }

    public void drawScreen(int mx, int my, float partialTicks)
    {
        typeList.drawScreen(mx, my, partialTicks);
        super.drawScreen(mx, my, partialTicks);
        drawCenteredString(fontRendererObj, I18n.format("shady.select.passtype.title"), width / 2, 10, 0xFFFFFFFF);
    }

}
