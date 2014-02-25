package org.jglrxavpok.glutils;

import java.nio.*;
import java.lang.reflect.Method;
import org.lwjgl.opengl.*;

/**
 * A simple shadow technique casts a transparent shadow onto a floor surface 
 * (or other planar surface). Use the stencil buffer to prevent the shadow  
 * from "doubling up" (drawing over itself). 
 * <P>
 * To make the shadow shape we create a matrix that transforms the object
 * as if it is being viewed from the light's point of view, and projected
 * onto the floor plane (see makeShadowMatrix()).
 * <P>
 * Multiply the modelview matrix by this "shadow matrix", then
 * draw the object.  Turn off lighting and texture to draw just the 
 * flat silhouette of the object.  Turn off depth_test to insure that
 * the shadow draws on top of the floor. (see drawShadow())
 * <P>
 * Set the alpha to 50% so the shadow appears as a dark area over the floor
 * texture.  This creates a problem though, as the "shadow" is actually an
 * object that has been flattened out onto a plane.  Where the object  
 * overlaps itself it appears darker, breaking the illusion of a shadow.  
 * <P>
 * Use the stencil buffer to draw a mask of the shadow shape, then 
 * use a glSencilOp() setting to insure that the object is drawn only once
 * into the shadow area (see drawShadow()).  This eliminates the  
 * "doubled up" areas and the shadow looks convincing.
 *
 */
public class GLShadowOnPlane {
    // shadow matrix (see makeShadowMatrix())
    FloatBuffer fShadowMatrix;

	// position of light
	float[] lightPos;

	// equation for the plane the shadow will fall on (the normal for the plane, 0,1,0)
    float[] shadowPlane = new float[] {0f,1f,0f,0f};
    
    // color of shadow, typically black, with alpha
	float[] shadowColor = new float[] {0f, 0f, 0f, .6f};
	
	// call back function to draw objects that will cast shadows
	Method drawObjectMethod;
	Object parentApp;
	
	
	public GLShadowOnPlane(float[] lightPosition, float[] plane, float[] shadowcolor, GLUtils gl_application, Method objectDrawFunction) {
		//shadowColor = shadowcolor;
		drawObjectMethod = objectDrawFunction;
		parentApp = gl_application;
		
		lightPos = lightPosition;
		shadowPlane = plane;
		
        float[] fShadowMatrixArray = new float[16];
        makeShadowMatrix(fShadowMatrixArray, lightPosition, plane);

        // convert the float array to a FloatBuffer
        fShadowMatrix = GLUtils.allocFloats(fShadowMatrixArray);
	}
	
    /**
     * draw the object that will be shadowed
     */
	public void drawObject() {
		GLUtils.invoke(parentApp, drawObjectMethod);
	}
	
	/**
	 * Draw the shadowed object from the lights point of view.  This will
	 * create the right shape for a shadow.  To draw the object as a flat shape
	 * we turn off lighting and texture, and set glColor() to gray.
	 */
    public void drawShadow() {
        float[] fShadowMatrixArray = new float[16];
        makeShadowMatrix(fShadowMatrixArray, lightPos, shadowPlane);

        // convert the float array to a FloatBuffer
        fShadowMatrix = GLUtils.allocFloats(fShadowMatrixArray);

        
        
        // preserve current settings
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        {
        	// no texture
        	GL11.glDisable(GL11.GL_TEXTURE_2D);
        	// no light
        	GL11.glDisable(GL11.GL_LIGHTING);
        	// no depth
        	GL11.glDisable(GL11.GL_DEPTH_TEST);
        	// turn blending on
        	GL11.glEnable(GL11.GL_BLEND);
        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        	// dark gray color
        	GL11.glColor4f(shadowColor[0],shadowColor[1],shadowColor[2],shadowColor[3]);
        	
        	// preserve current modelview matrix
        	GL11.glPushMatrix();
        	{
        		// multiply the current modelview matrix with the shadow matrix
        		GL11.glMultMatrix(fShadowMatrix);
        		
        		// make a mask of the shadow shape 
        		GLUtils.clearMask();      // clear the stencil buffer
        		GLUtils.beginMask(1);     // we'll fill the shadow shape with the number 1
        		drawObject();           // draw the object into stencil buffer 
        		GLUtils.endMask();        // done with mask
        		GLUtils.activateMask(1);  // now OpenGL will draw only where stencil buffer == 1
        		
        		// Make sure that we don't draw at any pixel position more than once. 
        		// GL_INCR tells the stencil buffer to increase the value to 2
        		// wherever we draw.  activateMask(1) set the stencil buffer to 
        		// only write to areas with a 1, so we'll only draw to an area once.
        		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
        		
        		// draw the object to be shadowed (as if viewed from light position)
        		drawObject();
        	}
        	GL11.glPopMatrix();
        	
        	// be sure to turn off stencil when done
        	GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
        GL11.glPopAttrib();
    }

    /** 
     *  Make a matrix that will transform the geometry as if it is 
     *  projected onto the given plane, from the viewpoint of the given light position.
     */
    void makeShadowMatrix(float[] fDestMat, float[] fLightPos, float[] fPlane)
    {
        float dot;

        // dot product of plane and light position
        dot = fPlane[0] * fLightPos[0] +
              fPlane[1] * fLightPos[1] +
              fPlane[2] * fLightPos[2] +
              fPlane[3] * fLightPos[3];

         // first column
        fDestMat[0]  = dot - fLightPos[0] * fPlane[0];
        fDestMat[4]  = 0.0f - fLightPos[0] * fPlane[1];
        fDestMat[8]  = 0.0f - fLightPos[0] * fPlane[2];
        fDestMat[12] = 0.0f - fLightPos[0] * fPlane[3];

        // second column
        fDestMat[1]  = 0.0f - fLightPos[1] * fPlane[0];
        fDestMat[5]  = dot - fLightPos[1] * fPlane[1];
        fDestMat[9]  = 0.0f - fLightPos[1] * fPlane[2];
        fDestMat[13] = 0.0f - fLightPos[1] * fPlane[3];

        // third column
        fDestMat[2]  = 0.0f - fLightPos[2] * fPlane[0];
        fDestMat[6]  = 0.0f - fLightPos[2] * fPlane[1];
        fDestMat[10] = dot - fLightPos[2] * fPlane[2];
        fDestMat[14] = 0.0f - fLightPos[2] * fPlane[3];

        // fourth column
        fDestMat[3]  = 0.0f - fLightPos[3] * fPlane[0];
        fDestMat[7]  = 0.0f - fLightPos[3] * fPlane[1];
        fDestMat[11] = 0.0f - fLightPos[3] * fPlane[2];
        fDestMat[15] = dot - fLightPos[3] * fPlane[3];
    }

}
