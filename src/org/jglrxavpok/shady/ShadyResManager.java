package org.jglrxavpok.shady;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class ShadyResManager implements IResourceManager
{

    private Set<String>           domains;
    private IResourceManager      parent;
    private List<VirtualResource> virtuals;

    public ShadyResManager(IResourceManager parent)
    {
        this.parent = parent;
        domains = new HashSet<String>();
        virtuals = Lists.newArrayList();
        domains.add(ShadyMod.MODID);
        domains.add("minecraft");
    }

    @Override
    public Set getResourceDomains()
    {
        return domains;
    }

    @Override
    public IResource getResource(ResourceLocation var1) throws IOException
    {
        IResource result = null;
        result = load(var1);
        if(result == null)
        {
            if(var1.getResourceDomain().equals("minecraft"))
                return getResource(new ResourceLocation(ShadyMod.MODID, var1.getResourcePath()));
            throw new FileNotFoundException("File " + var1.getResourceDomain() + ":" + var1.getResourcePath() + " not found");
        }
        return result;
    }

    private IResource load(ResourceLocation var1)
    {
        for(VirtualResource virtual : virtuals)
        {
            if(virtual.getResourceLocation().getResourceDomain().equals(var1.getResourceDomain()) && virtual.getResourceLocation().getResourcePath().equals(var1.getResourcePath()))
            {
                System.out.println("fetched virtual " + var1.getResourceDomain() + ":" + var1.getResourcePath());
                return virtual.copy();
            }
        }
        for(String domain : domains)
        {
            InputStream in = getInputStream(domain, var1);
            if(in == null)
            {
                try
                {
                    IResource res = parent.getResource(var1);
                    if(res != null)
                        return res;
                }
                catch(IOException e)
                {
                    ; // File doesn't exist
                }
                continue;
            }
            return new SimpleResource(domain, var1, in, null, new IMetadataSerializer());
        }
        return null;
    }

    private InputStream getInputStream(String domain, ResourceLocation var1)
    {
        return ShadyResManager.class.getResourceAsStream("/assets/" + domain + "/" + var1.getResourcePath());
    }

    @Override
    public List getAllResources(ResourceLocation var1) throws IOException
    {
        ArrayList<IResource> list = new ArrayList<IResource>();
        list.add(getResource(var1));
        return list;
    }

    public void register(VirtualResource res)
    {
        virtuals.add(res);
        System.out.println("Registred virtual resource at: " + res.getResourceLocation().getResourceDomain() + ":" + res.getResourceLocation().getResourcePath());
    }

}
