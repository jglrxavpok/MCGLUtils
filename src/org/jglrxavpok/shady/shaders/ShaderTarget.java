package org.jglrxavpok.shady.shaders;

public class ShaderTarget
{

    private String  id;
    private String  sampleName;
    private boolean bilinear;
    private boolean bilinearFlagPresent;
    private int     width;
    private int     height;
    private boolean renderInside;

    public ShaderTarget(String sampleName, String id)
    {
        this.sampleName = sampleName;
        this.id = id;
        this.width = -1;
        this.height = -1;
        this.renderInside = false;
    }

    public String getSampleName()
    {
        return sampleName;
    }

    public String getID()
    {
        return id;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public boolean isBilinear()
    {
        return bilinear;
    }

    public boolean hasBilinearFlag()
    {
        return bilinearFlagPresent;
    }

    public void setBilinearFlag(boolean flag)
    {
        this.bilinearFlagPresent = true;
        this.bilinear = flag;
    }

    public void setRenderInsideFlag(boolean flag)
    {
        this.renderInside = flag;
    }

    public boolean shouldRenderInside()
    {
        return renderInside;
    }

}
