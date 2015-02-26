package org.jglrxavpok.shady.shaders.passes;

import java.util.Collection;
import java.util.List;

import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.shaders.ShaderPass;
import org.jglrxavpok.shady.shaders.ShaderTarget;
import org.jglrxavpok.shady.shaders.ShaderUniform;

import com.google.common.collect.Lists;

public class PhosphorPass extends ShaderPass
{

    @Override
    public String getName()
    {
        return "Phosphor pass";
    }

    @Override
    public String getProgram()
    {
        return "phosphor";
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
    public Collection<ShaderTarget> generateAuxiTargets()
    {
        List<ShaderTarget> targets = Lists.newArrayList();
        targets.add(new ShaderTarget("PrevSampler", "previous"));
        return targets;
    }

    @Override
    public Collection<ShaderUniform> generateUniforms()
    {
        List<ShaderUniform> uniforms = Lists.newArrayList();
        uniforms.add(new ShaderUniform("Phosphor", new double[]
        {
                0.95, 0.95, 0.95
        }));
        return uniforms;
    }
}
