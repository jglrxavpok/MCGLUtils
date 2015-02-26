package org.jglrxavpok.shady.shaders;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.VirtualResource;
import org.jglrxavpok.shady.cpalette.ColorPalette;

public abstract class AutoShaderPass extends ShaderPass
{

    private String vertContent;
    private String fragContent;
    private String progContent;

    public AutoShaderPass()
    {
        super();
    }

    public abstract String createProgramContent();

    public abstract String createVertexContent();

    public abstract String createFragmentContent();

    @Override
    public void init()
    {
        fragContent = createFragmentContent();
        vertContent = createVertexContent();
        progContent = createProgramContent();
    }

    @Override
    public void registerVirtuals(ShadyResManager resManager)
    {
        VirtualResource vertexResource = new VirtualResource(new ResourceLocation(ShadyMod.MODID, "shaders/program/" + getProgram() + ".vsh"), vertContent.getBytes());
        resManager.register(vertexResource);
        VirtualResource fragResource = new VirtualResource(new ResourceLocation(ShadyMod.MODID, "shaders/program/" + getProgram() + ".fsh"), fragContent.getBytes());
        resManager.register(fragResource);
        VirtualResource progResource = new VirtualResource(new ResourceLocation(ShadyMod.MODID, "shaders/program/" + getProgram() + ".json"), progContent.getBytes());
        resManager.register(progResource);
    }

    public String read(ResourceLocation location)
    {
        return read("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
    }

    public String read(String path)
    {
        InputStream input = ColorPalette.class.getResourceAsStream(path);
        try
        {
            return IOUtils.toString(input, "UTF-8");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
