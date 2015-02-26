package org.jglrxavpok.shady.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import com.google.common.base.Predicate;

public class ShadyTextField extends GuiTextField
{

    private Predicate<String> predicate;

    public ShadyTextField(int id, FontRenderer font, int x, int y, int width, int height)
    {
        super(id, font, x, y, width, height);
    }

    public void drawTextBox()
    {
        if(getVisible())
        {
            int borderColor = -6250336;
            if(predicate != null)
            {
                boolean result = predicate.apply(getText());
                if(result)
                    borderColor &= 0xFF0FFF0F; // We remove a lil' bit of red and blue: accentuates green
                else
                    borderColor &= 0xFFFF0F0F; // We remove a lil' bit of green and blue: accentuates red
            }
            drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, borderColor);
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
            super.drawTextBox();
        }
    }

    // We do not use the built in predicate from Minecraft because we want to still be able to write
    public void setPredicate(Predicate<String> predicate)
    {
        this.predicate = predicate;
    }

    public boolean getEnableBackgroundDrawing()
    {
        return false;
    }
}
