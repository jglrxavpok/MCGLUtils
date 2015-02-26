package org.jglrxavpok.shady.gui;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.shaders.ShaderBatch;

import com.google.common.base.Predicate;

public class GuiSaveBatch extends GuiScreen implements GuiResponder
{

    private GuiUserBatch          parent;
    private ShaderBatch           batch;
    private ShadyTextField        shaderFileName;
    private Predicate<String>     predicate;
    private GuiButton             saveButton;
    private boolean               validText;
    private static final String[] disallowedFilenames = new String[]
                                                      {
            "CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
                                                      };
    public static int             SAVE_BUTTON         = 0;
    public static int             CANCEL_BUTTON       = 1;

    public GuiSaveBatch(GuiUserBatch parent, ShaderBatch batch)
    {
        this.parent = parent;
        this.batch = batch;
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == SAVE_BUTTON)
        {
            try
            {
                ShadyMod.instance.saveBatch(batch, new File(ShadyMod.instance.batchesFolder, shaderFileName.getText() + ".batch"));
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
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        validText = true;
        buttonList.clear();
        String text;
        if(shaderFileName != null) // Happens when resizing
            text = shaderFileName.getText();
        else
            text = "pass_" + ShadyMod.instance.batchesFolder.listFiles().length;
        this.shaderFileName = new ShadyTextField(0, fontRendererObj, width / 2 - 100, 50, 200, 20);
        this.predicate = createPredicate();
        shaderFileName.setPredicate(predicate);
        shaderFileName.setText(text);
        shaderFileName.func_175207_a(this); // Sets a GuiResponder

        saveButton = new GuiButton(SAVE_BUTTON, width / 2 - 200 - 5, height - 50, I18n.format("shady.confirm"));
        buttonList.add(saveButton);
        buttonList.add(new GuiButton(CANCEL_BUTTON, width / 2 + 5, height - 50, I18n.format("shady.cancel")));

        this.func_175319_a(0, text); // Updates the value of validText
    }

    private Predicate<String> createPredicate()
    {
        return new Predicate<String>()
        {
            @Override
            public boolean apply(String input)
            {
                if(input.isEmpty())
                    return false;
                if(!isValidFilename(input))
                    return false;
                File[] files = ShadyMod.instance.batchesFolder.listFiles();
                if(files != null)
                {
                    for(File file : files)
                    {
                        if(file.getName().equals(input + ".batch"))
                            return false;
                    }
                    return true;
                }
                return true;
            }
        };
    }

    public void drawScreen(int mx, int my, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("shady.saveBatch.title"), width / 2, 30, 0xFFFFFFFF);
        super.drawScreen(mx, my, partialTicks);
        shaderFileName.drawTextBox();

        if(validText)
        {
            drawCenteredString(fontRendererObj, I18n.format("shady.saveBatch.valid"), width / 2, shaderFileName.yPosition + shaderFileName.height + 10, 0xFFFFFFFF);
        }
        else
        {
            drawCenteredString(fontRendererObj, I18n.format("shady.saveBatch.invalid"), width / 2, shaderFileName.yPosition + shaderFileName.height + 10, 0xFFFFFFFF);
        }
    }

    private boolean isValidFilename(String input)
    {
        String newString = input.replaceAll("[/\"*?!<>|]", "_");
        if(!newString.equals(input))
            return false;
        for(int j = 0; j < disallowedFilenames.length; ++j)
        {
            String filename = disallowedFilenames[j];

            if(newString.equalsIgnoreCase(filename))
            {
                return false;
            }
        }

        return true;
    }

    public void updateScreen()
    {
        super.updateScreen();
        shaderFileName.updateCursorCounter();
    }

    public void mouseReleased(int mx, int my, int state)
    {
        super.mouseReleased(mx, my, state);
    }

    public void keyTyped(char ch, int code) throws IOException
    {
        super.keyTyped(ch, code);
        shaderFileName.textboxKeyTyped(ch, code);
    }

    public void mouseClicked(int mx, int my, int mouseButton) throws IOException
    {
        super.mouseClicked(mx, my, mouseButton);

        shaderFileName.mouseClicked(mx, my, mouseButton);
    }

    @Override
    public void func_175321_a(int p_175321_1_, boolean p_175321_2_)
    {
        ; // NOP
    }

    @Override
    public void func_175320_a(int p_175320_1_, float p_175320_2_)
    {
        ; // NOP
    }

    @Override
    public void func_175319_a(int id, String text)
    {
        validText = predicate.apply(text);
        saveButton.enabled = validText;
    }
}
