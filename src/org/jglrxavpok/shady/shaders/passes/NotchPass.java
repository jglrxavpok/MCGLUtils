package org.jglrxavpok.shady.shaders.passes;

import java.util.Collection;
import java.util.List;

import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.shaders.ShaderPass;
import org.jglrxavpok.shady.shaders.ShaderTarget;

import com.google.common.collect.Lists;

public class NotchPass extends ShaderPass
{

    @Override
    public String getName()
    {
        return "notch";
    }

    @Override
    public String getProgram()
    {
        return "notch";
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

    public Collection<ShaderTarget> generateAuxiTargets()
    {
        List<ShaderTarget> list = Lists.newArrayList();
        ShaderTarget target = new ShaderTarget("DitherSampler", "dither");
        target.setBilinearFlag(false);
        target.setWidth(4);
        target.setHeight(4);
        target.setRenderInsideFlag(false);
        list.add(target);
        return list;
    }

}
