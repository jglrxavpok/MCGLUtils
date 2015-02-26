package org.jglrxavpok.shady.shaders.passes;

import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.shaders.ShaderPass;

public class DummyPass extends ShaderPass
{

    public DummyPass()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "dummy";
    }

    @Override
    public String getProgram()
    {
        return null;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void registerVirtuals(ShadyResManager resManager)
    {

    }

}
