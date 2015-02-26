package org.jglrxavpok.shady.shaders;

import java.util.Collection;

import org.jglrxavpok.shady.ShadyResManager;

import com.google.common.collect.Lists;

public abstract class ShaderPass
{

    public ShaderPass()
    {
    }

    public abstract String getName();

    public abstract String getProgram();

    public abstract void init();

    public abstract void registerVirtuals(ShadyResManager resManager);

    public Collection<ShaderUniform> generateUniforms()
    {
        return Lists.newArrayList();
    }

    public Collection<ShaderTarget> generateAuxiTargets()
    {
        return Lists.newArrayList();
    }
}
