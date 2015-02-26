package org.jglrxavpok.shady.shaders;

public class ShaderUniform
{
    private String   name;
    private double[] values;

    public ShaderUniform(String name, double[] values)
    {
        this.name = name;
        this.values = values;
    }

    public String getName()
    {
        return name;
    }

    public double[] getValues()
    {
        return values;
    }
}
