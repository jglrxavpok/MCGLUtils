package org.jglrxavpok.shady.gui;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.shaders.ShaderBatch;

public class GuiLoadBatch extends GuiScreen
{

    private GuiShaderList      passes;
    private GuiUserBatch       parent;
    private GuiButton          loadButton;
    private GuiBatchesFileList batchesList;

    public static int          CANCEL_BUTTON = 0;
    public static int          LOAD_BUTTON   = 1;

    public GuiLoadBatch(GuiUserBatch parent, GuiShaderList passes)
    {
        this.parent = parent;
        this.passes = passes;
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        buttonList.clear();

        batchesList = new GuiBatchesFileList(mc, 20, 30, width, height, 30);

        File[] files = ShadyMod.instance.batchesFolder.listFiles();
        if(files != null)
        {
            for(File file : files)
            {
                batchesList.addEntry(new BatchFileEntry(fontRendererObj, file));
            }
        }

        buttonList.add(new GuiButton(CANCEL_BUTTON, width / 2 + 5, height - height / 8, I18n.format("shady.cancel")));
        loadButton = new GuiButton(LOAD_BUTTON, width / 2 - 200 - 5, height - height / 8, I18n.format("shady.confirm"));
        buttonList.add(loadButton);
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.batchesList.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if(mouseButton != 0 || !this.batchesList.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if(state != 0 || !this.batchesList.mouseReleased(mouseX, mouseY, state))
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        loadButton.enabled = batchesList.getSelectedIndex() != -1;
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == LOAD_BUTTON)
        {
            try
            {
                ShaderBatch batch = ShadyMod.instance.loadBatch(batchesList.getSelected().getFile());
                passes.clear();
                for(int i = 0; i < batch.getNames().size(); i++ )
                {
                    passes.addEntry(new ShaderPassEntry(fontRendererObj, batch.getPasses().get(i), batch.getNames().get(i)));
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            mc.displayGuiScreen(parent);
        }
        else if(button.id == CANCEL_BUTTON)
        {
            mc.displayGuiScreen(parent);
        }
        else
            batchesList.actionPerformed(button);
    }

    public void drawScreen(int mx, int my, float partialTicks)
    {
        batchesList.drawScreen(mx, my, partialTicks);
        super.drawScreen(mx, my, partialTicks);
        drawCenteredString(fontRendererObj, I18n.format("shady.loadBatch.title"), width / 2, 10, 0xFFFFFFFF);
    }
}
