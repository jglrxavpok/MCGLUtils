package org.jglrxavpok.shady.cpalette;

import java.io.IOException;

import net.minecraft.util.ResourceLocation;

import org.jglrxavpok.shady.ShadyMod;

public class DefaultColorPalettes
{

    public static ColorPalette NES;
    public static ColorPalette GB;
    public static ColorPalette GB_GRAY;
    public static ColorPalette DOOM;
    public static ColorPalette CGA;
    public static ColorPalette COMMODORE64;

    public static void init()
    {
        try
        {
            NES = ColorPalette.create("nes", new ResourceLocation(ShadyMod.MODID, "textures/palettes/nes.png"));
            GB = ColorPalette.create("gb", new ResourceLocation(ShadyMod.MODID, "textures/palettes/gameboy.png"));
            GB_GRAY = ColorPalette.create("gb_gray", new ResourceLocation(ShadyMod.MODID, "textures/palettes/gameboy_gray.png"));
            DOOM = ColorPalette.create("doom", new ResourceLocation(ShadyMod.MODID, "textures/palettes/doom.png"));
            CGA = ColorPalette.create("cga", new ResourceLocation(ShadyMod.MODID, "textures/palettes/cga.png"));
            COMMODORE64 = ColorPalette.create("commodore64", new ResourceLocation(ShadyMod.MODID, "textures/palettes/c64.png"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
