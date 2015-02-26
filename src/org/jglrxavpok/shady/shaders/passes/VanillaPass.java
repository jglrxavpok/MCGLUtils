package org.jglrxavpok.shady.shaders.passes;

import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.shaders.ShaderPass;

public class VanillaPass extends ShaderPass
{

    private String id;

    public VanillaPass(String id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return id;
    }

    @Override
    public String getProgram()
    {
        return id;
    }

    @Override
    public void init()
    {
        ;
    }

    @Override
    public void registerVirtuals(ShadyResManager resManager)
    {
        ;
    }

}
