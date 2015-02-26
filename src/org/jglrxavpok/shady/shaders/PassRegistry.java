package org.jglrxavpok.shady.shaders;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

public class PassRegistry
{

    private static Map<String, ShaderPass> providers = Maps.newHashMap();

    public static void register(String id, ShaderPass provider)
    {
        providers.put(id, provider);
    }

    public static Collection<String> getAllIDs()
    {
        return providers.keySet();
    }

    public static ShaderPass getFromID(String id)
    {
        return providers.get(id);
    }

    public static String getID(ShaderPass pass)
    {
        for(String id : getAllIDs())
        {
            if(getFromID(id) == pass)
                return id;
        }
        return null;
    }
}
