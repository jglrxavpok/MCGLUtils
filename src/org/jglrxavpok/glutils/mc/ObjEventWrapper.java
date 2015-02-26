package org.jglrxavpok.glutils.mc;

import net.minecraftforge.fml.common.eventhandler.Event;

import org.jglrxavpok.glutils.ObjEvent;

public class ObjEventWrapper extends Event
{

    public ObjEvent objEvent;

    public ObjEventWrapper(ObjEvent e)
    {
        this.objEvent = e;
    }

    public boolean isCancelable()
    {
        return objEvent.canBeCancelled();
    }
}
