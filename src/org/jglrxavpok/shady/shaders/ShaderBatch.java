package org.jglrxavpok.shady.shaders;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;

import org.jglrxavpok.shady.ShadyMod;
import org.jglrxavpok.shady.ShadyResManager;
import org.jglrxavpok.shady.VirtualResource;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonWriter;

public class ShaderBatch
{

    private static int       NUM = 0;
    private List<ShaderPass> passes;
    private int              id;
    private ResourceLocation location;
    private String[]         targets;
    private List<String>     names;

    // TODO: Per batch uniforms/targets
    public ShaderBatch()
    {
        this.id = NUM++ ;
        this.location = new ResourceLocation(ShadyMod.MODID, "shaders/post/virtual" + id + ".json");
        passes = Lists.newArrayList();
        names = Lists.newArrayList();
    }

    public List<ShaderPass> getPasses()
    {
        return passes;
    }

    public List<String> getNames()
    {
        return names;
    }

    public void addPass(String name, ShaderPass pass)
    {
        names.add(name);
        passes.add(pass);
    }

    public void init()
    {
        ShadyResManager resManager = ShadyMod.instance.getResourceManager();
        List<String> auxTargetsList = Lists.newArrayList();
        for(ShaderPass pass : getPasses())
        {
            pass.init();
            pass.registerVirtuals(resManager);
            Collection<ShaderTarget> auxtargets = pass.generateAuxiTargets();
            for(ShaderTarget entry : auxtargets)
            {
                if(entry.shouldRenderInside())
                    auxTargetsList.add(entry.getID());
            }
        }
        targets = new String[(passes.size() == 1 ? 1 : 2) + auxTargetsList.size()];
        int swapTargets = targets.length - auxTargetsList.size(); // Number of targets needed without auxilary targets
        for(int i = 0; i < swapTargets; i++ )
            targets[i] = "swap:target" + i;
        for(int i = swapTargets; i < targets.length; i++ )
        {
            targets[i] = auxTargetsList.get(i - swapTargets);
        }
        auxTargetsList.clear();
        try
        {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.setIndent("    ");
            writer.beginObject();
            {
                writer.name("targets");
                writer.beginArray();
                {
                    for(String target : targets)
                        writer.value(target);
                }
                writer.endArray();

                writer.name("passes");
                writer.beginArray();
                {
                    int passIndex = 0;
                    for(ShaderPass pass : passes)
                    {
                        writer.beginObject();
                        writer.name("name");
                        writer.value(pass.getProgram());
                        writer.name("intarget");
                        writer.value(getInTarget(passIndex));

                        writer.name("outtarget");
                        writer.value(getOutTarget(passIndex));

                        Collection<ShaderUniform> uniforms = pass.generateUniforms();
                        if(!uniforms.isEmpty())
                        {
                            writer.name("uniforms");
                            writer.beginArray();
                            for(ShaderUniform uniform : uniforms)
                            {
                                writer.beginObject();
                                writer.name("name");
                                writer.value(uniform.getName());

                                writer.name("values");
                                writer.beginArray();
                                for(double value : uniform.getValues())
                                    writer.value(value);
                                writer.endArray();
                                writer.endObject();
                            }
                            writer.endArray();
                        }

                        Collection<ShaderTarget> auxtargets = pass.generateAuxiTargets();
                        if(!auxtargets.isEmpty())
                        {
                            writer.name("auxtargets");
                            writer.beginArray();
                            for(ShaderTarget entry : auxtargets)
                            {
                                writer.beginObject();
                                writer.name("name");
                                writer.value(entry.getSampleName());

                                writer.name("id");
                                writer.value(entry.getID());

                                if(entry.getWidth() != -1)
                                {
                                    writer.name("width");
                                    writer.value(entry.getWidth());
                                }

                                if(entry.getHeight() != -1)
                                {
                                    writer.name("height");
                                    writer.value(entry.getHeight());
                                }

                                if(entry.hasBilinearFlag())
                                {
                                    writer.name("bilinear");
                                    writer.value(entry.isBilinear());
                                }

                                if(entry.shouldRenderInside())
                                {
                                    System.out.println("dqzdkqzdokzqdùmo<ikqzdùop<k");
                                    auxTargetsList.add(entry.getID());
                                }
                                writer.endObject();
                            }
                            writer.endArray();
                        }

                        writer.endObject();

                        passIndex++ ;
                    }

                    writer.beginObject();
                    writer.name("name");
                    writer.value("blit");
                    writer.name("intarget");
                    writer.value(getInTarget(passIndex));

                    writer.name("outtarget");
                    writer.value("minecraft:main");
                    writer.endObject();

                    for(String target : auxTargetsList)
                    {
                        writer.beginObject();
                        writer.name("name");
                        writer.value("blit");
                        writer.name("intarget");
                        writer.value(getInTarget(passIndex));

                        writer.name("outtarget");
                        writer.value(target);
                        writer.endObject();
                    }
                }
                writer.endArray();
            }
            writer.endObject();
            writer.flush();
            writer.close();
            stringWriter.flush();
            stringWriter.close();
            String string = stringWriter.getBuffer().toString();
            System.out.println(string);
            resManager.register(new VirtualResource(getResourceLocation(), string.getBytes()));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getOutTarget(int passIndex)
    {
        return getInTarget(passIndex + 1);
    }

    private String getInTarget(int target)
    {
        if(target == 0)
        {
            return "minecraft:main";
        }
        return targets[(target - 1) % targets.length];
    }

    public ResourceLocation getResourceLocation()
    {
        return location;
    }

    public ShaderGroup toShaderGroup(ShadyResManager resManager) throws JsonException
    {
        return new ShaderGroup(Minecraft.getMinecraft().renderEngine, resManager, Minecraft.getMinecraft().getFramebuffer(), getResourceLocation());
    }
}
