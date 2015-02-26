package org.jglrxavpok.shady;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

public class VirtualResource implements IResource
{

    private ResourceLocation location;
    private byte[] content;
    private ByteArrayInputStream input;

    public VirtualResource(ResourceLocation location, byte[] content)
    {
        this.location = location;
        this.input = new ByteArrayInputStream(content);
        this.content = content;
    }
    
    public VirtualResource copy()
    {
        return new VirtualResource(location, content);
    }
    
    @Override
    public ResourceLocation getResourceLocation()
    {
        return location;
    }

    @Override
    public InputStream getInputStream()
    {
        return input;
    }

    @Override
    public boolean hasMetadata()
    {
        return false;
    }

    @Override
    public IMetadataSection getMetadata(String p_110526_1_)
    {
        return null;
    }

    @Override
    public String getResourcePackName()
    {
        return "virtual";
    }

}
