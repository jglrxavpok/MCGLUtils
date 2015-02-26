package org.jglrxavpok.shady.cpalette;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.shaders.AutoShaderPass;
import org.jglrxavpok.shady.shaders.PassRegistry;

import com.google.common.collect.Maps;

public class ColorPalette extends AutoShaderPass
{

    private String                                 id;
    private int[]                                  colors;
    private ResourceLocation                       location;
    private static final Map<String, ColorPalette> palettes = Maps.newHashMap();

    public ColorPalette(String id, int[] colors)
    {
        super();
        palettes.put(id, this);
        this.id = id;
        this.colors = colors;
        this.location = new ResourceLocation(ShadyMod.MODID, "shaders/post/" + id + ".json");
    }

    private String createHSBs()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("hsbcolors[" + colors.length + "] = vec3[](");
        int index = 0;
        for(int color : colors)
        {
            if(index++ != 0)
                buffer.append(",");
            String vec3 = "vec3(";
            double r = ((double) ((color >> 16) & 0xFF) / 255.0);
            double g = ((double) ((color >> 8) & 0xFF) / 255.0);
            double b = ((double) ((color >> 0) & 0xFF) / 255.0);
            float[] vals = Color.RGBtoHSB((int) (r * 255), (int) (g * 255), (int) (b * 255), null);
            vec3 += vals[0];
            vec3 += "," + vals[1];
            vec3 += "," + vals[2];
            buffer.append(vec3).append(")");
        }
        buffer.append(")");
        String result = buffer.toString();
        return result;
    }

    private String createPalette()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("palette[" + colors.length + "] = vec3[](");
        int index = 0;
        for(int color : colors)
        {
            if(index++ != 0)
                buffer.append(",");
            String vec3 = "vec3(";
            vec3 += ((double) ((color >> 16) & 0xFF) / 255.0);
            vec3 += "," + ((double) ((color >> 8) & 0xFF) / 255.0);
            vec3 += "," + ((double) ((color >> 0) & 0xFF) / 255.0);
            buffer.append(vec3).append(")");
        }
        buffer.append(")");
        String result = buffer.toString();
        return result;
    }

    public String getID()
    {
        return id;
    }

    public static ColorPalette create(String id, ResourceLocation paletteLoc) throws IOException
    {
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(paletteLoc);
        BufferedImage image = ImageIO.read(res.getInputStream());
        int[] colors = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ColorPalette palette = new ColorPalette(id, colors);
        PassRegistry.register(id, palette);
        return palette;
    }

    public static ColorPalette fromID(String id)
    {
        return palettes.get(id);
    }

    public ResourceLocation getResourceLocation()
    {
        return location;
    }

    @Override
    public String getProgram()
    {
        return id;
    }

    @Override
    public String createProgramContent()
    {
        return read(new ResourceLocation(ShadyMod.MODID, "shaders/program/paletteBase.json")).replace("#PALETTE_SHADER#", id);
    }

    @Override
    public String createVertexContent()
    {
        return read(new ResourceLocation(ShadyMod.MODID, "shaders/program/paletteBase.vsh"));
    }

    @Override
    public String createFragmentContent()
    {
        String palette = createPalette();
        String hsbcolors = createHSBs();
        return read(new ResourceLocation(ShadyMod.MODID, "shaders/program/paletteBase.fsh")).replace("#palette#", palette).replace("#hsbcolors#", hsbcolors);
    }

    @Override
    public String getName()
    {
        return I18n.format("shady.pass.type." + id);
    }

    public static Map<String, ColorPalette> getPalettes()
    {
        return palettes;
    }

}
