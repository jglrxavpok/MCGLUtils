package org.jglrxavpok.glutils;

import java.nio.*;
import org.lwjgl.opengl.*;

/**
 *  Wraps properties for a material. 
 *
 *  To use:
 *      create a new material,
 *      set the material properties (setDiffuse(), setAmbient(), setSpecular(), setShininess()),
 *      call material.apply() in your render function to set the OpenGL current material properties.
 */
public class GLMaterial {
    // A sampling of color values
    public static final float colorClear[]          = {  0f,  0f,  0f,  0f}; // alpha is 0
    public static final float colorNone[]           = {  0f,  0f,  0f,  1f}; // no color = black
    public static final float colorRed[]            = {  1f,  0f,  0f,  1f};
    public static final float colorGreen[]          = {  0f,  1f,  0f,  1f};
    public static final float colorBlue[]           = {  0f,  0f,  1f,  1f};
    public static final float colorYellow[]         = {  1f,  1f,  0f,  1f};
    public static final float colorCyan[]           = {  0f,  1f,  1f,  1f};
    public static final float colorMagenta[]        = {  1f,  0f,  1f,  1f};
    public static final float colorGrayLight[]      = { .8f, .8f, .8f,  1f};
    public static final float colorGrayMedium[]     = { .5f, .5f, .5f,  1f};
    public static final float colorGrayDark[]       = { .2f, .2f, .2f,  1f};
    public static final float colorWhite[]          = {  1f,  1f,  1f,  1f};
    public static final float colorBlack[]          = {  0f,  0f,  0f,  1f};
    public static final float colorBeige[]          = { .7f, .7f, .4f,  1f};
    public static final float colorDefaultDiffuse[] = { .8f, .8f, .8f,  1f}; // OpenGL default diffuse color
    public static final float colorDefaultAmbient[] = { .2f, .2f, .2f,  1f}; // OpenGL default ambient color
    public static final float minShine   = 0.0f;
    public static final float maxShine   = 127.0f;

    // Default material values
    private static FloatBuffer defaultDiffuse = allocFloats(colorDefaultDiffuse);
    private static FloatBuffer defaultAmbient = allocFloats(colorDefaultAmbient);
    private static FloatBuffer defaultSpecular = allocFloats(colorNone);
    private static FloatBuffer defaultEmission = allocFloats(colorNone);
    private static FloatBuffer defaultShine = allocFloats(new float[] {minShine,0,0,0}); // LWJGL requires four values, so include three extra zeroes

    // The color values for this material
    public FloatBuffer diffuse;      // color of the lit surface
    public FloatBuffer ambient;      // color of the shadowed surface
    public FloatBuffer specular;     // reflection color (typically this is a shade of gray)
    public FloatBuffer emission;     // glow color
    public FloatBuffer shininess;    // size of the reflection highlight

    // hold name and texture values for this material
    public String mtlname = "noname";  // name of this material in the .mtl and .obj files
    public String textureFile = null;  // texture filename (null if no texture)
    public int textureHandle;          // opengl handle to the texture (0 if no texture)


    public GLMaterial() {
        setDefaults();
    }

    public GLMaterial(float[] color) {
        setDefaults();
        setColor(color);
    }

	/**
	 *  Set the material to OpenGL's default values (gray, with no reflection and no glow)
	 */
    public void setDefaults() {
        setDiffuse(colorDefaultDiffuse);
        setAmbient(colorDefaultAmbient);
        setSpecular(colorNone);
        setEmission(colorNone);
        setShininess(minShine);
    }

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 * Functions to set the material properties
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 *  Set the diffuse material color.  This is the color of the material
	 *  where it is directly lit.
	 */
    public void setDiffuse(float[] color) {
        diffuse = allocFloats(color);
    }

	/**
	 *  Set the ambient material color.  This is the color of the material
	 *  where it is lit by indirect light (light scattered off the environment).
	 *  Ie. the shadowed side of an object.
	 */
    public void setAmbient(float[] color) {
        ambient = allocFloats(color);
    }

	/**
	 *  Set the specular material color.  This controls how much light
	 *  is reflected off a glossy surface.  This color value describes
	 *  the brightness of the reflection and is typically a shade of gray.
	 *  Pure black means that no light is reflected (ie. a very rough matte
	 *  surface).  Pure white means that the surface is highly reflective,
	 *
	 *  see also:  setShininess()
	 */
    public void setSpecular(float[] color) {
        specular = allocFloats(color);
    }

	/**
	 *  Set the emission material color.  This controls the "glow" of the material,
	 *  and can be used to make a material that seems to be lit from inside.
	 */
    public void setEmission(float[] color) {
        emission = allocFloats(color);
    }

    /**
     *  Set size of the reflection highlight.  Must also set the specular color for
     *  shininess to have any effect:
     *           setSpecular(GLMaterial.colorWhite);
     *
     * @param howShiny  How sharp reflection is: 0 - 127 (127=very sharp pinpoint)
     */
    public void setShininess(float howShiny) {
        if (howShiny >= minShine && howShiny <= maxShine) {
            float[] tmp = {howShiny,0,0,0};
            shininess = allocFloats(tmp);
        }
    }

    /**
     *  Call glMaterial() to activate these material properties in the OpenGL environment.
     *  These properties will stay in effect until you change them or disable lighting.
     */
    public void apply() {
    	// GL_FRONT: affect only front facing triangles
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, emission);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, shininess);
    }

    /**
     *  Reset all material settings to the default values.
     */
    public static void clear() {
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, defaultDiffuse);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, defaultAmbient);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, defaultSpecular);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, defaultEmission);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, defaultShine);
    }

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 * The following functions provide a simpler way to use materials
	 * that hides some of the complexity of the OpenGL functions.
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 *  Sets the material color to approximate a "real" surface color.
	 *
	 *  Use the same color for diffuse and ambient.  To create a
	 *  shadowed effect you should lower the ambient value for the
	 *  light sources and lower the overall ambient light.
	 */
    public void setColor(float[] color) {
        setDiffuse(color);   // surface directly lit
        setAmbient(color);   // surface in shadow
    }

    /**
     *  Set the reflection properties.  Typically the reflection (specular color)
     *  describes the brightness of the reflection, and is a shade of gray.
     *  This function takes two params that describe the intensity
     *  of the reflection, and the size of the highlight.
     *
     *  intensity - a float from 0-1 (0=no reflectivity, 1=maximum reflectivity)
     *  highlight - a float from 0-1 (0=soft highlight, 1=sharpest highlight)
     *
     *  example: setReflection(1,1)  creates a bright, sharp reflection
     *           setReflection(.5f,.5f)  creates a softer, wider reflection
     */
    public void setReflection(float intensity, float highlight) {
		float[] color = {intensity,intensity,intensity,1}; // create a shade of gray
        setSpecular(color);
        setShininess((int)(highlight*127f)); // convert 0-1 to 0-127
    }

    /**
     *  Make material appear to emit light
     */
    public void setGlowColor(float[] color) {
        emission = allocFloats(color);
    }

    /**
     * alpha value is set in the diffuse material color.  Other material
     * colors (ambient, specular) are not affected by alpha value.
     * @param alphaVal  0 - 1
     */
    public void setAlpha(float alphaVal) {
        diffuse.put(3, alphaVal);
    }

    /**
     * alpha value is stored in the diffuse material color alpha.  Other material
     * colors (ambient, specular) are not affected by alpha value.
     * @return alphaVal  0 - 1
     */
    public float getAlpha() {
        return diffuse.get(3);
    }

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 * functions to add a texture to this material
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Store a texture filename with the material.
     */
    public void setTextureFile(String s) {
        textureFile = s;
    }

    /**
     * Assign a texture handle to this material.
     * @param txtrHandle
     */
    public void setTexture(int txtrHandle) {
        textureHandle = txtrHandle;
    }

    public String getTextureFile() {
        return textureFile;
    }

    public int getTexture() {
        return textureHandle;
    }

    /**
     * set the material name.  This is the name assigned to this texture in the .mtl file.
     * It is NOT a filename.
     * @param s
     */
    public void setName(String s) {
        mtlname = s;
    }

    public String getName() {
        return mtlname;
    }

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 * Native IO buffer functions
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public static final int SIZE_FLOAT = 4;  // four bytes in a float

    public static FloatBuffer allocFloats(int howmany) {
        return ByteBuffer.allocateDirect(howmany * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static FloatBuffer allocFloats(float[] floatarray) {
        FloatBuffer fb = ByteBuffer.allocateDirect(floatarray.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fb.put(floatarray).flip();
        return fb;
    }

}