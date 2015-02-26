package org.jglrxavpok.shady.gui;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.shaders.ShaderBatch;
import org.jglrxavpok.shady.shaders.ShaderPass;
import org.jglrxavpok.shady.shaders.passes.DummyPass;

public class GuiUserBatch extends GuiScreen
{

    public static final int BACK_BUTTON   = 0;
    public static final int ADD_BUTTON    = 1;
    public static final int EDIT_BUTTON   = 2;
    public static final int REMOVE_BUTTON = 3;
    public static final int LOAD_BUTTON   = 4;
    public static final int SAVE_BUTTON   = 5;
    private GuiScreen       parent;
    private GuiShaderList   passesList;
    private GuiButton       addButton;
    private GuiButton       editButton;
    private GuiButton       removeButton;
    private GuiButton       loadButton;
    private GuiButton       saveButton;

    public GuiUserBatch(GuiScreen parent)
    {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        buttonList.clear();
        if(passesList == null)
        {
            passesList = new GuiShaderList(mc, 20, 30, width, height, 30);
            ShaderBatch batch = ShadyMod.instance.getBatch();
            if(batch != null)
            {
                List<String> names = batch.getNames();
                for(int i = 0; i < names.size(); i++ )
                {
                    ShaderPass pass = batch.getPasses().get(i);
                    passesList.addEntry(new ShaderPassEntry(fontRendererObj, pass, names.get(i)));
                }
            }
        }

        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 100, height - 20, I18n.format("gui.back")));

        addButton = new GuiButton(ADD_BUTTON, 10, height - 20 - 50, I18n.format("shady.addPass"));
        editButton = new GuiButton(EDIT_BUTTON, width - 10 - 200, height - 20 - 50, I18n.format("shady.editPass"));
        removeButton = new GuiButton(REMOVE_BUTTON, 10, height - 20 - 25, I18n.format("shady.removePass"));

        loadButton = new GuiButton(LOAD_BUTTON, width - 10 - 200, height - 20 - 25, 98, 20, I18n.format("shady.loadBatch"));
        saveButton = new GuiButton(SAVE_BUTTON, width - 10 - 200 + 102, height - 20 - 25, 98, 20, I18n.format("shady.saveBatch"));

        buttonList.add(addButton);
        buttonList.add(editButton);
        buttonList.add(removeButton);
        buttonList.add(loadButton);
        buttonList.add(saveButton);
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.passesList.handleMouseInput();
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == BACK_BUTTON)
        {
            if(passesList.getSize() == 0)
            {
                ShadyMod.instance.setBatch(null);
                try
                {
                    ShadyMod.instance.saveBatch();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                ShadyMod.instance.disactiveBatch();
            }
            else
            {
                ShaderBatch batch = new ShaderBatch();
                for(int i = 0; i < passesList.getSize(); i++ )
                {
                    ShaderPassEntry entry = ((ShaderPassEntry) passesList.getListEntry(i));
                    batch.addPass(entry.getName(), entry.getPass());
                }
                batch.init();
                ShadyMod.instance.setBatch(batch);
                try
                {
                    ShadyMod.instance.saveBatch();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                ShadyMod.instance.activateBatch();
            }
            mc.displayGuiScreen(parent);
        }
        else if(button.id == ADD_BUTTON)
        {
            openEditGui(new ShaderPassEntry(fontRendererObj, new DummyPass(), "Pass " + (passesList.getSize() + 1)));
        }
        else if(button.id == EDIT_BUTTON)
        {
            openEditGui(passesList.getSelected());
        }
        else if(button.id == REMOVE_BUTTON)
        {
            int index = passesList.getSelectedIndex();
            passesList.remove(index);
        }
        else if(button.id == LOAD_BUTTON)
        {
            mc.displayGuiScreen(new GuiLoadBatch(this, passesList));
        }
        else if(button.id == SAVE_BUTTON)
        {
            ShaderBatch batch = new ShaderBatch();
            for(int i = 0; i < passesList.getSize(); i++ )
            {
                ShaderPassEntry entry = ((ShaderPassEntry) passesList.getListEntry(i));
                batch.addPass(entry.getName(), entry.getPass());
            }
            mc.displayGuiScreen(new GuiSaveBatch(this, batch));
        }
        else
        {
            passesList.actionPerformed(button);
        }
    }

    private void openEditGui(ShaderPassEntry entry)
    {
        mc.displayGuiScreen(new GuiEditPass(this, entry));
    }

    public void addEntry(ShaderPassEntry entry)
    {
        passesList.addEntry(entry);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        passesList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, I18n.format("shady.userbatch"), width / 2, 10, 0xFFFFFFFF);
    }

    public void updateScreen()
    {
        super.updateScreen();
        boolean slotSelected = passesList.getSelectedIndex() != -1 && passesList.getSize() > 0;
        editButton.enabled = slotSelected;
        removeButton.enabled = slotSelected;
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if(mouseButton != 0 || !this.passesList.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if(state != 0 || !this.passesList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    public boolean hasEntry(ShaderPassEntry entry)
    {
        return passesList.contains(entry);
    }

    public GuiShaderList getList()
    {
        return passesList;
    }
}
