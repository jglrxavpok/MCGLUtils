package org.jglrxavpok.shady.shaders.passes;

import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.shaders.ShaderPass;

public class LowResPass extends ShaderPass
{

    public LowResPass()
    {
    }

    @Override
    public String getProgram()
    {
        return "lowres";
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

    @Override
    public String getName()
    {
        return "Low resolution";
    }

}
