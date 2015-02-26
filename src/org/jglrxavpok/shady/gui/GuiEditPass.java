package org.jglrxavpok.shady.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import org.jglrxavpok.shady.shaders.PassRegistry;

import com.mojang.realmsclient.gui.ChatFormatting;

public class GuiEditPass extends GuiScreen
{

    private static final int BACK_BUTTON      = 0;
    private static final int ADD_BUTTON       = 1;
    private static final int PASS_TYPE_BUTTON = 2;
    private static final int PASS_NAME_FIELD  = 3;
    private ShaderPassEntry  entry;
    private GuiUserBatch     parent;
    private String           passName;
    private String           passType;
    private GuiTextField     passNameField;
    private GuiButton        addButton;

    public GuiEditPass(GuiUserBatch parent, ShaderPassEntry entry)
    {
        this.parent = parent;
        this.entry = entry;
        passName = entry.getName();
        passType = PassRegistry.getID(entry.getPass());
        if(passType == null)
        {
            passType = PassRegistry.getAllIDs().iterator().next(); // First entry in pass types
        }
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        buttonList.clear();

        passNameField = new GuiTextField(PASS_NAME_FIELD, fontRendererObj, width / 2 - 100, 45, 200, 20);
        passNameField.setText(passName);

        buttonList.add(new GuiButton(PASS_TYPE_BUTTON, width / 2 - 200 - 5, 70, I18n.format("shady.select.passtype")));

        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 + 5, height - height / 8, I18n.format("gui.back")));

        addButton = new GuiButton(ADD_BUTTON, width / 2 - 200 - 5, height - height / 8, I18n.format("shady.confirm"));
        buttonList.add(addButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("shady.editPass.title"), width / 2, 10, 0xFFFFFFFF);

        drawCenteredString(fontRendererObj, ChatFormatting.UNDERLINE + I18n.format("shady.pass.name"), width / 2, 30, 0xFFFFFFFF);
        passNameField.drawTextBox();

        fontRendererObj.drawStringWithShadow(I18n.format("shady.currentPass") + ": " + I18n.format("shady.pass.type." + passType), width / 2 + 5, 75, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void updateScreen()
    {
        super.updateScreen();
        addButton.enabled = !passNameField.getText().isEmpty();
    }

    public void actionPerformed(GuiButton button)
    {
        if(button.id == BACK_BUTTON)
        {
            mc.displayGuiScreen(parent);
        }
        else if(button.id == ADD_BUTTON)
        {
            entry.setName(passNameField.getText());
            entry.setPass(PassRegistry.getFromID(passType));
            if(!parent.hasEntry(entry))
                parent.addEntry(entry);
            mc.displayGuiScreen(parent);
        }
        else if(button.id == PASS_TYPE_BUTTON)
        {
            mc.displayGuiScreen(new GuiSelectPassType(this));
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.passNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        passNameField.textboxKeyTyped(typedChar, keyCode);
    }

    public void confirmType(String type)
    {
        this.passType = type;
    }
}
