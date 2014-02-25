package org.jglrxavpok.glutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

/**
 * Collection of functions to init and run an OpenGL app using LWJGL.
 * <P>
 * Includes functions to handle: <BR>
 * Setup display mode, keyboard, mouse, handle events<BR>
 * Run main loop of application <BR>
 * Buffer allocation -- manage IntBuffer, ByteBuffer calls. <BR>
 * OpenGL functions -- convert screen/world coords, set modes, lights, etc. <BR>
 * Utility functions -- load images, convert pixels, getTimeInMillis, etc. <BR>
 * <P>
 * Has a main() function to run as an application, though this class has only
 * minimal placeholder functionality. It is meant to be subclassed, and the
 * subclass will define setup() draw() mouseMove() functions, etc.
 * <P>
 * GLUtils initializes the LWJGL environment for OpenGL rendering, ie. creates a
 * window, sets the display mode, inits mouse and keyboard, then runs a loop.
 * <P>
 * Uses GLImage to load and hold image pixels.
 * <P>
 * napier -at- potatoland -dot- org
 * 
 * @see GLImage
 */
public class GLUtils
{
	// Just for reference
	public static final String GLAPP_VERSION = ".5";

	// Byte size of data types: Used when allocating native buffers
	public static final int SIZE_DOUBLE = 8;
	public static final int SIZE_FLOAT = 4;
	public static final int SIZE_INT = 4;
	public static final int SIZE_BYTE = 1;

	// Application settings
	// These can be tweaked in main() before calling app.run()
	// to customize app behavior.

	// NIO Buffers to retrieve OpenGL settings.
	// For memory efficiency and performance, instantiate these once, and reuse.
	// see getSetingInt(), getModelviewMatrix(), project(), unProject()
	public static IntBuffer bufferViewport = allocInts(16);
	public static FloatBuffer bufferModelviewMatrix = allocFloats(16);
	public static FloatBuffer bufferProjectionMatrix = allocFloats(16);
	public static FloatBuffer tmpResult = allocFloats(16); // temp var to hold
															// project/unproject
															// results
	public static FloatBuffer tmpFloats = allocFloats(4); // temp var used by
															// setLightPos(),
															// setFog()
	public static ByteBuffer tmpFloat = allocBytes(SIZE_FLOAT); // temp var used
																// by
																// getZDepth()
	public static IntBuffer tmpInts = allocInts(16); // temp var used by
														// getSettingInt()
	public static ByteBuffer tmpByte = allocBytes(SIZE_BYTE); // temp var used
																// by
																// getStencilValue()
	public static ByteBuffer tmpInt = allocBytes(GLUtils.SIZE_INT); // temp var
																	// used by
																	// getPixelColor()

	// Material colors (see setMaterial())
	public static FloatBuffer mtldiffuse = allocFloats(4); // color of the lit
															// surface
	public static FloatBuffer mtlambient = allocFloats(4); // color of the
															// shadowed surface
	public static FloatBuffer mtlspecular = allocFloats(4); // reflection color
															// (typically this
															// is a shade of
															// gray)
	public static FloatBuffer mtlemissive = allocFloats(4); // glow color
	public static FloatBuffer mtlshininess = allocFloats(4); // size of the
																// reflection
																// highlight

	// Misc.
	public static float rotation = 0f; // to rotate cubes (just to put something
										// on screen)
	public static final float PIOVER180 = 0.0174532925f; // A constant used in
															// navigation:
															// PI/180
	public static final float PIUNDER180 = 57.2957795130f; // A constant used in
															// navigation:
															// 180/PI;
	static Hashtable OpenGLextensions; // will be populated by extensionExists()
	static double avgSecsPerFrame = .01; // to smooth out motion, keep a moving

	private static int screenTextureSize;
											// average of frame render times

	private static double viewportW;

	private static double viewportH;

	private static float aspectRatio;


	public static FloatBuffer getModelviewMatrix()
	{
		bufferModelviewMatrix.clear();
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bufferModelviewMatrix);
		return bufferModelviewMatrix;
	}

	public static FloatBuffer getProjectionMatrix()
	{
		bufferProjectionMatrix.clear();
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, bufferProjectionMatrix);
		return bufferProjectionMatrix;
	}

	public static IntBuffer getViewport()
	{
		bufferViewport.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, bufferViewport);
		return bufferViewport;
	}

	/**
	 * Convert a FloatBuffer matrix to a 4x4 float array.
	 * 
	 * @param fb
	 *            FloatBuffer containing 16 values of 4x4 matrix
	 * @return 2 dimensional float array
	 */
	public static float[][] getMatrixAsArray(FloatBuffer fb)
	{
		float[][] fa = new float[4][4];
		fa[0][0] = fb.get();
		fa[0][1] = fb.get();
		fa[0][2] = fb.get();
		fa[0][3] = fb.get();
		fa[1][0] = fb.get();
		fa[1][1] = fb.get();
		fa[1][2] = fb.get();
		fa[1][3] = fb.get();
		fa[2][0] = fb.get();
		fa[2][1] = fb.get();
		fa[2][2] = fb.get();
		fa[2][3] = fb.get();
		fa[3][0] = fb.get();
		fa[3][1] = fb.get();
		fa[3][2] = fb.get();
		fa[3][3] = fb.get();
		return fa;
	}

	/**
	 * Return the Z depth of the single pixel at the given screen position.
	 */
	public static float getZDepth(int x, int y)
	{
		tmpFloat.clear();
		GL11.glReadPixels(x, y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT,
				tmpFloat);
		return ((float) (tmpFloat.getFloat(0)));
	}

	/**
	 * Find the Z depth of the origin in the projected world view. Used by
	 * getWorldCoordsAtScreen() Projection matrix must be active for this to
	 * return correct results (GL.glMatrixMode(GL.GL_PROJECTION)). For some
	 * reason I have to chop this to four decimals or I get bizarre results when
	 * I use the value in project().
	 */
	public static float getZDepthAtOrigin()
	{
		float[] resultf = new float[3];
		project(0, 0, 0, resultf);
		return ((int) (resultf[2] * 10000F)) / 10000f; // truncate to 4 decimals
	}

	/**
	 * Return screen coordinates for a given point in world space. The world
	 * point xyz is 'projected' into screen coordinates using the current model
	 * and projection matrices, and the current viewport settings.
	 * 
	 * @param x
	 *            world coordinates
	 * @param y
	 * @param z
	 * @param resultf
	 *            the screen coordinate as an array of 3 floats
	 */
	public static void project(float x, float y, float z, float[] resultf)
	{
		// lwjgl 2.0 altered params for GLU funcs
		GLU.gluProject(x, y, z, getModelviewMatrix(), getProjectionMatrix(),
				getViewport(), tmpResult);
		resultf[0] = tmpResult.get(0);
		resultf[1] = tmpResult.get(1);
		resultf[2] = tmpResult.get(2);
	}

	/**
	 * Return world coordinates for a given point on the screen. The screen
	 * point xyz is 'un-projected' back into world coordinates using the current
	 * model and projection matrices, and the current viewport settings.
	 * 
	 * @param x
	 *            screen x position
	 * @param y
	 *            screen y position
	 * @param z
	 *            z-buffer depth position
	 * @param resultf
	 *            the world coordinate as an array of 3 floats
	 * @see getWorldCoordsAtScreen()
	 */
	public static void unProject(float x, float y, float z, float[] resultf)
	{
		GLU.gluUnProject(x, y, z, getModelviewMatrix(), getProjectionMatrix(),
				getViewport(), tmpResult);
		resultf[0] = tmpResult.get(0);
		resultf[1] = tmpResult.get(1);
		resultf[2] = tmpResult.get(2);
	}

	/**
	 * For given screen xy, return the world xyz coords in a float array. Assume
	 * Z position is 0.
	 */
	public static float[] getWorldCoordsAtScreen(int x, int y)
	{
		float z = getZDepthAtOrigin();
		float[] resultf = new float[3];
		unProject((float) x, (float) y, (float) z, resultf);
		return resultf;
	}

	/**
	 * For given screen xy and z depth, return the world xyz coords in a float
	 * array.
	 */
	public static float[] getWorldCoordsAtScreen(int x, int y, float z)
	{
		float[] resultf = new float[3];
		unProject((float) x, (float) y, (float) z, resultf);
		return resultf;
	}

	// ========================================================================
	// Texture functions
	// ========================================================================

	/**
	 * Allocate a texture (glGenTextures) and return the handle to it.
	 */
	public static int allocateTexture()
	{
		IntBuffer textureHandle = allocInts(1);
		GL11.glGenTextures(textureHandle);
		return textureHandle.get(0);
	}

	/**
	 * "Select" the given texture for further texture operations.
	 */
	public static void activateTexture(int textureHandle)
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
	}

	/**
	 * Create a texture and mipmap from the given image file.
	 */
	public static int makeTexture(String textureImagePath)
	{
		int textureHandle = 0;
		GLImage textureImg = loadImage(textureImagePath);
		if (textureImg != null)
		{
			textureHandle = makeTexture(textureImg);
			makeTextureMipMap(textureHandle, textureImg);
		}
		return textureHandle;
	}

	/**
	 * Create a texture and optional mipmap with the given parameters.
	 * 
	 * @param mipmap
	 *            : if true, create mipmaps for the texture
	 * @param anisotropic
	 *            : if true, enable anisotropic filtering
	 */
	public static int makeTexture(String textureImagePath, boolean mipmap,
			boolean anisotropic)
	{
		int textureHandle = 0;
		GLImage textureImg = loadImage(textureImagePath);
		if (textureImg != null)
		{
			textureHandle = makeTexture(textureImg.pixelBuffer, textureImg.w,
					textureImg.h, anisotropic);
			if (mipmap)
			{
				makeTextureMipMap(textureHandle, textureImg);
			}
		}
		return textureHandle;
	}

	/**
	 * Create a texture from the given image.
	 */
	public static int makeTexture(GLImage textureImg)
	{
		if (textureImg != null)
		{
			if (isPowerOf2(textureImg.w) && isPowerOf2(textureImg.h))
			{
				return makeTexture(textureImg.pixelBuffer, textureImg.w,
						textureImg.h, false);
			} else
			{
				msg("GLUtils.makeTexture(GLImage) Warning: not a power of two: "
						+ textureImg.w + "," + textureImg.h);
				textureImg.convertToPowerOf2();
				return makeTexture(textureImg.pixelBuffer, textureImg.w,
						textureImg.h, false);
			}
		}
		return 0;
	}

	/**
	 * De-allocate the given texture (glDeleteTextures()).
	 */
	public static void deleteTexture(int textureHandle)
	{
		IntBuffer bufferTxtr = allocInts(1).put(textureHandle);
		;
		GL11.glDeleteTextures(bufferTxtr);
	}

	/**
	 * Returns true if n is a power of 2. If n is 0 return zero.
	 */
	public static boolean isPowerOf2(int n)
	{
		if (n == 0)
		{
			return false;
		}
		return (n & (n - 1)) == 0;
	}

	/**
	 * Create a blank square texture with the given size.
	 * 
	 * @return the texture handle
	 */
	public static int makeTexture(int w)
	{
		ByteBuffer pixels = allocBytes(w * w * SIZE_INT); // allocate 4 bytes
															// per pixel
		return makeTexture(pixels, w, w, false);
	}

	/**
	 * Create a texture from the given pixels in the default Java ARGB int
	 * format.<BR>
	 * Configure the texture to repeat in both directions and use LINEAR for
	 * magnification.
	 * <P>
	 * 
	 * @return the texture handle
	 */
	public static int makeTexture(int[] pixelsARGB, int w, int h,
			boolean anisotropic)
	{
		if (pixelsARGB != null)
		{
			ByteBuffer pixelsRGBA = GLImage.convertImagePixelsRGBA(pixelsARGB,
					w, h, true);
			return makeTexture(pixelsRGBA, w, h, anisotropic);
		}
		return 0;
	}

	/**
	 * Create a texture from the given pixels in the default OpenGL RGBA pixel
	 * format. Configure the texture to repeat in both directions and use LINEAR
	 * for magnification.
	 * <P>
	 * 
	 * @return the texture handle
	 */
	public static int makeTexture(ByteBuffer pixels, int w, int h,
			boolean anisotropic)
	{
		// get a new empty texture
		int textureHandle = allocateTexture();
		// preserve currently bound texture, so glBindTexture() below won't
		// affect anything)
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
		// 'select' the new texture by it's handle
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		// set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR); // GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR); // GL11.GL_NEAREST);

		// make texture "anisotropic" so it will minify more gracefully
		if (anisotropic && extensionExists("GL_EXT_texture_filter_anisotropic"))
		{
			// Due to LWJGL buffer check, you can't use smaller sized buffers
			// (min_size = 16 for glGetFloat()).
			FloatBuffer max_a = allocFloats(16);
			// Grab the maximum anisotropic filter.
			GL11.glGetFloat(
					EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a);
			// Set up the anisotropic filter.
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D,
					EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
					max_a.get(0));
		}

		// Create the texture from pixels
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, // level of detail
				GL11.GL_RGBA8, // internal format for texture is RGB with Alpha
				w, h, // size of texture image
				0, // no border
				GL11.GL_RGBA, // incoming pixel format: 4 bytes in RGBA order
				GL11.GL_UNSIGNED_BYTE, // incoming pixel data type: unsigned
										// bytes
				pixels); // incoming pixels

		// restore previous texture settings
		GL11.glPopAttrib();

		return textureHandle;
	}

	/**
	 * Create a texture from the given pixels in ARGB format. The pixels buffer
	 * contains 4 bytes per pixel, in ARGB order. ByteBuffers are created with
	 * native hardware byte orders, so the pixels can be in big-endian (ARGB)
	 * order, or little-endian (BGRA) order. Set the pixel_byte_order
	 * accordingly when creating the texture.
	 * <P>
	 * Configure the texture to repeat in both directions and use LINEAR for
	 * magnification.
	 * <P>
	 * NOTE: I'm having problems creating mipmaps when image pixel data is in
	 * GL_BGRA format. Looks like GLU type param doesn't recognize
	 * GL_UNSIGNED_INT_8_8_8_8 and GL_UNSIGNED_INT_8_8_8_8_REV so I can't
	 * specify endian byte order. Mipmaps look right on PC but colors are
	 * reversed on Mac. Have to stick with GL_RGBA byte order for now.
	 * <P>
	 * 
	 * @return the texture handle
	 */
	public static int makeTextureARGB(ByteBuffer pixels, int w, int h)
	{
		// byte buffer has ARGB ints in little endian or big endian byte order
		int pixel_byte_order = (pixels.order() == ByteOrder.BIG_ENDIAN) ? GL12.GL_UNSIGNED_INT_8_8_8_8
				: GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
		// get a new empty texture
		int textureHandle = allocateTexture();
		// 'select' the new texture by it's handle
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		// set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR); // GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR); // GL11.GL_NEAREST);
		// Create the texture from pixels
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, // level of detail
				GL11.GL_RGBA8, // internal format for texture is RGB with Alpha
				w, h, // size of texture image
				0, // no border
				GL12.GL_BGRA, // incoming pixel format: 4 bytes in ARGB order
				pixel_byte_order, // incoming pixel data type: little or big
									// endian ints
				pixels); // incoming pixels
		return textureHandle;
	}

	/**
	 * Build Mipmaps for currently bound texture (builds a set of textures at
	 * various levels of detail so that texture will scale up and down
	 * gracefully)
	 * 
	 * @param textureImg
	 *            the texture image
	 * @return error code of buildMipMap call
	 */
	public static int makeTextureMipMap(int textureHandle, GLImage textureImg)
	{
		int ret = 0;
		if (textureImg != null && textureImg.isLoaded())
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
			ret = GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8,
					textureImg.w, textureImg.h, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, textureImg.getPixelBytes());
			if (ret != 0)
			{
				err("GLUtils.makeTextureMipMap(): Error occured while building mip map, ret="
						+ ret + " error=" + GLU.gluErrorString(ret));
			}
			// Assign the mip map levels and texture info
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
					GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
					GL11.GL_MODULATE);
		}
		return ret;
	}

	/**
	 * Create a texture large enough to hold the screen image. Use RGBA8 format
	 * to insure colors are copied exactly. Use GL_NEAREST for magnification to
	 * prevent slight blurring of image when screen is drawn back.
	 * 
	 * @see frameCopy()
	 * @see frameDraw()
	 */
	public static int makeTextureForScreen(int screenSize)
	{
		// get a texture size big enough to hold screen (512, 1024, 2048 etc.)
		screenTextureSize = getPowerOfTwoBiggerThan(screenSize);
		msg("GLUtils.makeTextureForScreen(): made texture for screen with size "
				+ screenTextureSize);
		// get a new empty texture
		int textureHandle = allocateTexture();
		ByteBuffer pixels = allocBytes(screenTextureSize * screenTextureSize
				* SIZE_INT);
		// 'select' the new texture by it's handle
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		// set texture parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		// use GL_NEAREST to prevent blurring during frequent screen restores
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		// Create the texture from pixels: use GL_RGBA8 to insure exact color
		// copy
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
				screenTextureSize, screenTextureSize, 0, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, pixels);
		return textureHandle;
	}

	/**
	 * Find a power of two equal to or greater than the given value. Ie.
	 * getPowerOfTwoBiggerThan(800) will return 1024.
	 * <P>
	 * 
	 * @see makeTextureForScreen()
	 * @param dimension
	 * @return a power of two equal to or bigger than the given dimension
	 */
	public static int getPowerOfTwoBiggerThan(int n)
	{
		if (n < 0)
			return 0;
		--n;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return n + 1;
	}

	/**
	 * Copy pixels from a ByteBuffer to a texture. The buffer pixels are
	 * integers in ARGB format (this is the Java default format you get from a
	 * BufferedImage) or BGRA format (this is the native order of Intel systems.
	 * 
	 * The glTexSubImage2D() call treats the incoming pixels as integers in
	 * either big-endian (ARGB) or little-endian (BGRA) formats based on the
	 * setting of the bytebuffer (pixel_byte_order).
	 * 
	 * @param bb
	 *            ByteBuffer of pixels stored as ARGB or BGRA integers
	 * @param w
	 *            width of source image
	 * @param h
	 *            height of source image
	 * @param textureHandle
	 *            texture to copy pixels into
	 */
	public static void copyPixelsToTexture(ByteBuffer bb, int w, int h,
			int textureHandle)
	{
		int pixel_byte_order = (bb.order() == ByteOrder.BIG_ENDIAN) ? GL12.GL_UNSIGNED_INT_8_8_8_8
				: GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

		// "select" the texture that we'll write into
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);

		// Copy pixels to texture
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, // always GL_TEXTURE_2D
				0, // texture detail level: always 0
				0, 0, // x,y offset into texture
				w, h, // dimensions of image pixel data
				GL12.GL_BGRA, // format of pixels in texture (little_endian -
								// native for PC)
				pixel_byte_order, // format of pixels in bytebuffer (big or
									// little endian ARGB integers)
				bb // image pixel data
		);
	}

	/**
	 * Calls glTexSubImage2D() to copy pixels from an image into a texture.
	 */
	public static void copyImageToTexture(GLImage img, int textureHandle)
	{
		copyPixelsToTexture(img.pixelBuffer, img.w, img.h, textureHandle);
	}


	// ========================================================================
	// Functions to push/pop OpenGL settings
	// ========================================================================

	/**
	 * preserve all OpenGL settings that can be preserved. Use this function to
	 * isolate settings changes. Call pushAttrib() before calling glEnable(),
	 * glDisable(), glMatrixMode() etc. After your code executes, call
	 * popAttrib() to return to the previous settings.
	 * 
	 * For better performance, call pushAttrib() with specific settings flags to
	 * preserve only specific settings.
	 * 
	 * @see popAttrib()
	 */
	public static void pushAttrib()
	{
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
	}

	/**
	 * preserve the specified OpenGL setting. Call popAttrib() to return to the
	 * preserved state.
	 * 
	 * @see popAttrib()
	 */
	public static void pushAttrib(int attribute_bits)
	{
		GL11.glPushAttrib(attribute_bits);
	}

	/**
	 * preserve the OpenGL settings that will be affected when we draw in ortho
	 * mode over the scene. For example if we're drawing an interface layer,
	 * buttons, popup menus, cursor, text, etc. we need to turn off lighting,
	 * turn on blending, set color to white and turn off depth test.
	 * <P>
	 * call pushAttribOverlay(), enable settings that you need, when done call
	 * popAttrib()
	 * 
	 * @see popAttrib()
	 */
	public static void pushAttribOrtho()
	{
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT
				| GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * preserve the OpenGL viewport settings.
	 * 
	 * <pre>
	 *       pushAttribViewport();
	 *           setViewport(0,0,displaymode.getWidth(),displaymode.getHeight());
	 *           ... do some drawing outside of previous viewport area
	 *       popAttrib();
	 * </pre>
	 * 
	 * @see popAttrib()
	 */
	public static void pushAttribViewport()
	{
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
	}

	/**
	 * return to the OpenGL settings that were preserved by the previous
	 * pushAttrib() call.
	 * 
	 * @see pushAttrib()
	 */
	public static void popAttrib()
	{
		GL11.glPopAttrib();
	}

	// ========================================================================
	// Lighting functions
	// ========================================================================

	/**
	 * Set the color of a 'positional' light (a light that has a specific
	 * position within the scene). <BR>
	 * 
	 * Pass in an OpenGL light number (GL11.GL_LIGHT1), the 'Diffuse' and
	 * 'Ambient' colors (direct light and reflected light), and the position.<BR>
	 * 
	 * @param GLLightHandle
	 * @param diffuseLightColor
	 * @param ambientLightColor
	 * @param position
	 */
	public static void setLight(int GLLightHandle, float[] diffuseLightColor,
			float[] ambientLightColor, float[] specularLightColor,
			float[] position)
	{
		FloatBuffer ltDiffuse = allocFloats(diffuseLightColor);
		FloatBuffer ltAmbient = allocFloats(ambientLightColor);
		FloatBuffer ltSpecular = allocFloats(specularLightColor);
		FloatBuffer ltPosition = allocFloats(position);
		GL11.glLight(GLLightHandle, GL11.GL_DIFFUSE, ltDiffuse); // color of the
																	// direct
																	// illumination
		GL11.glLight(GLLightHandle, GL11.GL_SPECULAR, ltSpecular); // color of
																	// the
																	// highlight
		GL11.glLight(GLLightHandle, GL11.GL_AMBIENT, ltAmbient); // color of the
																	// reflected
																	// light
		GL11.glLight(GLLightHandle, GL11.GL_POSITION, ltPosition);
		GL11.glEnable(GLLightHandle); // Enable the light (GL_LIGHT1 - 7)
		// GL11.glLightf(GLLightHandle, GL11.GL_QUADRATIC_ATTENUATION, .005F);
		// // how light beam drops off
	}

	public static void setSpotLight(int GLLightHandle,
			float[] diffuseLightColor, float[] ambientLightColor,
			float[] position, float[] direction, float cutoffAngle)
	{
		FloatBuffer ltDirection = allocFloats(direction);
		setLight(GLLightHandle, diffuseLightColor, ambientLightColor,
				diffuseLightColor, position);
		GL11.glLightf(GLLightHandle, GL11.GL_SPOT_CUTOFF, cutoffAngle); // width
																		// of
																		// the
																		// beam
		GL11.glLight(GLLightHandle, GL11.GL_SPOT_DIRECTION, ltDirection); // which
																			// way
																			// it
																			// points
		GL11.glLightf(GLLightHandle, GL11.GL_CONSTANT_ATTENUATION, 2F); // how
																		// light
																		// beam
																		// drops
																		// off
		// GL11.glLightf(GLLightHandle, GL11.GL_LINEAR_ATTENUATION, .5F); // how
		// light beam drops off
		// GL11.glLightf(GLLightHandle, GL11.GL_QUADRATIC_ATTENUATION, .5F); //
		// how light beam drops off
	}

	/**
	 * Set the color of the Global Ambient Light. Affects all objects in scene
	 * regardless of their placement.
	 */
	public static void setAmbientLight(float[] ambientLightColor)
	{
		put(tmpFloats, ambientLightColor);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, tmpFloats);
	}

	/**
	 * Set the position of a light to the given xyz. NOTE: Positional light
	 * only, not directional.
	 */
	public static void setLightPosition(int GLLightHandle, float x, float y,
			float z)
	{
		put(tmpFloats, new float[]
		{ x, y, z, 1 });
		GL11.glLight(GLLightHandle, GL11.GL_POSITION, tmpFloats);
	}

	/**
	 * Set the position (or direction) of a light to the given xyz.
	 */
	public static void setLightPosition(int GLLightHandle, float[] position)
	{
		put(tmpFloats, position);
		GL11.glLight(GLLightHandle, GL11.GL_POSITION, tmpFloats);
	}

	/**
	 * enable/disable the given light. The light handle parameter is one of the
	 * predefined OpenGL light handle numbers (GL_LIGHT1, GL_LIGHT2 ...
	 * GL_LIGHT7).
	 */
	public static void setLight(int GLLightHandle, boolean on)
	{
		if (on)
		{
			GL11.glEnable(GLLightHandle);
		} else
		{
			GL11.glDisable(GLLightHandle);
		}
	}

	/**
	 * Enable/disable lighting. If parameter value is false, this will turn off
	 * all lights and ambient lighting.
	 * 
	 * NOTE: When lighting is disabled, material colors are disabled as well.
	 * Use glColor() to set color properties when ligthing is off.
	 */
	public static void setLighting(boolean on)
	{
		if (on)
		{
			GL11.glEnable(GL11.GL_LIGHTING);
		} else
		{
			GL11.glDisable(GL11.GL_LIGHTING);
		}
	}

	// ========================================================================
	// Material functions
	// ========================================================================
	public static final float[] colorClear =
	{ 0f, 0f, 0f, 0f }; // alpha is 0
	public static final float[] colorBlack =
	{ 0f, 0f, 0f, 1f };
	public static final float[] colorWhite =
	{ 1f, 1f, 1f, 1f };
	public static final float[] colorGray =
	{ .5f, .5f, .5f, 1f };
	public static final float[] colorRed =
	{ 1f, 0f, 0f, 1f };
	public static final float[] colorGreen =
	{ 0f, 1f, 0f, 1f };
	public static final float[] colorBlue =
	{ 0f, 0f, 1f, 1f };

	/**
	 * A simple way to set the current material properties to approximate a
	 * "real" surface. Provide the surface color (float[4]]) and shininess value
	 * (range 0-1).
	 * <P>
	 * Sets diffuse material color to the surfaceColor and ambient material
	 * color to surfaceColor/2. Based on the shiny value (0-1), sets the
	 * specular property to a color between black (0) and white (1), and sets
	 * the shininess property to a value between 0 and 127.
	 * <P>
	 * Lighting must be enabled for material colors to take effect.
	 * <P>
	 * 
	 * @param surfaceColor
	 *            - must be float[4] {R,G,B,A}
	 * @param reflection
	 *            - a float from 0-1 (0=very matte, 1=very shiny)
	 */
	public static void setMaterial(float[] surfaceColor, float shiny)
	{
		float[] reflect =
		{ shiny, shiny, shiny, 1 }; // make a shade of gray
		float[] ambient =
		{ surfaceColor[0] * .5f, surfaceColor[1] * .5f, surfaceColor[2] * .5f,
				1 }; // darker surface color
		mtldiffuse.put(surfaceColor).flip(); // surface directly lit
		mtlambient.put(ambient).flip(); // surface in shadow
		mtlspecular.put(reflect).flip(); // reflected light
		mtlemissive.put(colorBlack).flip(); // no emissive light
		// size of reflection
		int openglShininess = ((int) (shiny * 127f)); // convert 0-1 to 0-127
		if (openglShininess >= 0 && openglShininess <= 127)
		{
			mtlshininess.put(new float[]
			{ openglShininess, 0, 0, 0 }).flip();
		}
		applyMaterial();
	}

	/**
	 * Set the four material colors and calls glMaterial() to change the current
	 * material color in OpenGL. Lighting must be enabled for material colors to
	 * take effect.
	 * 
	 * @param shininess
	 *            : size of reflection (0 is matte, 127 is pinpoint reflection)
	 */
	public static void setMaterial(float[] diffuseColor, float[] ambientColor,
			float[] specularColor, float[] emissiveColor, float shininess)
	{
		mtldiffuse.put(diffuseColor).flip(); // surface directly lit
		mtlambient.put(ambientColor).flip(); // surface in shadow
		mtlspecular.put(specularColor).flip(); // reflection color
		mtlemissive.put(emissiveColor).flip(); // glow color
		if (shininess >= 0 && shininess <= 127)
		{
			mtlshininess.put(new float[]
			{ shininess, 0, 0, 0 }).flip(); // size of reflection 0=broad
											// 127=pinpoint
		}
		applyMaterial();
	}

	/**
	 * Alter the material opacity by setting the diffuse material color alpha
	 * value to the given value
	 * 
	 * @para alpha 0=transparent 1=opaque
	 */
	public static void setMaterialAlpha(float alpha)
	{
		if (alpha < 0)
			alpha = 0;
		if (alpha > 1)
			alpha = 1;
		mtldiffuse.put(3, alpha).flip(); // alpha value of diffuse color
		applyMaterial();
	}

	/**
	 * Call glMaterial() to activate these material properties in the OpenGL
	 * environment. These properties will stay in effect until you change them
	 * or disable lighting.
	 */
	public static void applyMaterial()
	{
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, mtldiffuse);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, mtlambient);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, mtlspecular);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, mtlemissive);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, mtlshininess);
	}

	// ========================================================================
	// Fog
	// ========================================================================

	/**
	 * Enable atmospheric fog effect, with the given color and density.
	 * 
	 * <PRE>
	 * setFog(new float[]
	 * { .5f, .5f, .5f, 1f }, .3f);
	 * </PRE>
	 * 
	 * @param fogColor
	 *            float[4] specifies the RGB fog color value
	 * @param fogDensity
	 *            float in range 0-1 specifies how opaque the fog will be
	 */
	public static void setFog(float[] fogColor, float fogdensity)
	{
		put(tmpFloats, fogColor);
		// turn fog on
		GL11.glEnable(GL11.GL_FOG);
		// mode: GL_EXP2 is dense fog, GL_EXP is thinner, GL_LINEAR is very thin
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
		// start and end (only apply when fog mode=GL_LINEAR
		// GL11.glFogf(GL11.GL_FOG_START, 100f);
		// GL11.glFogf(GL11.GL_FOG_END, 1000f);
		// color
		GL11.glFog(GL11.GL_FOG_COLOR, tmpFloats);
		// density
		GL11.glFogf(GL11.GL_FOG_DENSITY, fogdensity);
		// quality
		GL11.glHint(GL11.GL_FOG_HINT, GL11.GL_NICEST);
	}

	/**
	 * Enable/disable fog effect. Does not change the fog settings.
	 */
	public static void setFog(boolean on)
	{
		if (on)
		{
			GL11.glEnable(GL11.GL_FOG);
		} else
		{
			GL11.glDisable(GL11.GL_FOG);
		}
	}

	// ========================================================================
	// Time functions
	// ========================================================================


	// ========================================================================
	// Load images
	// ========================================================================

	/**
	 * Make a blank image of the given size.
	 * 
	 * @return the new GLImage
	 */
	public static GLImage makeImage(int w, int h)
	{
		ByteBuffer pixels = allocBytes(w * h * SIZE_INT);
		return new GLImage(pixels, w, h);
	}

	/**
	 * Load an image from the given file and return a GLImage object.
	 * 
	 * @param image
	 *            filename
	 * @return the loaded GLImage
	 */
	public static GLImage loadImage(String imgFilename)
	{
		GLImage img = new GLImage(imgFilename);
		if (img.isLoaded())
		{
			return img;
		}
		return null;
	}

	/**
	 * Load an image from the given file and return a ByteBuffer containing ARGB
	 * pixels.<BR>
	 * Can be used to create textures. <BR>
	 * 
	 * @param imgFilename
	 * @return
	 */
	public static ByteBuffer loadImagePixels(String imgFilename)
	{
		GLImage img = new GLImage(imgFilename);
		return img.pixelBuffer;
	}




	// ========================================================================
	// Functions to get and set framebuffer pixels
	// ========================================================================

	/**
	 * Return a ByteBuffer containing ARGB pixels of the entire screen area.
	 */
	public static ByteBuffer framePixels()
	{
		return framePixels(0, 0, Display.getDisplayMode().getWidth(),
				Display.getDisplayMode().getHeight());
	}

	/**
	 * Return a ByteBuffer containing ARGB pixels from the given screen area.
	 */
	public static ByteBuffer framePixels(int x, int y, int w, int h)
	{
		// allocate 4 bytes per pixel
		ByteBuffer pixels = allocBytes(w * h * 4);
		// Get pixels from frame buffer in ARGB format.
		GL11.glReadPixels(x, y, w, h, GL12.GL_BGRA,
				GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
		return pixels;
	}

	/**
	 * Return an int array containing ARGB pixels from the given screen area.
	 */
	public static int[] framePixelsInt(int x, int y, int w, int h)
	{
		int[] pixels = new int[w * h];
		ByteBuffer pixelsBB = framePixels(x, y, w, h);
		get(pixelsBB, pixels);
		return pixels;
	}

	/**
	 * Return the color buffer RGB value at the given screen position as
	 * byte[3].
	 * 
	 * @param x
	 *            screen position
	 * @param y
	 * @return rgb byte array
	 */
	public static byte[] getPixelColor(int x, int y)
	{
		// color value will be stored in an integer
		tmpInt.clear();
		// read the framebuffer color value at the given position, as bytes
		GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
				tmpInt);
		byte[] rgb = new byte[]
		{ tmpInt.get(0), tmpInt.get(1), tmpInt.get(2) };
		return rgb;
	}

	/**
	 * Return the depth buffer value at the given screen position.
	 * 
	 * @param x
	 *            screen position
	 * @param y
	 * @return float Z depth value
	 */
	public static float getPixelDepth(int x, int y)
	{
		return getZDepth(x, y);
	}

	/**
	 * Return the stencil buffer value at the given screen position. Stencil
	 * values are typically bytes (0-255). The value will be returned as an
	 * integer.
	 * 
	 * @param x
	 *            screen position
	 * @param y
	 * @return int stencil value
	 */
	public static int getPixelStencil(int x, int y)
	{
		return getMaskValue(x, y);
	}

	/**
	 * Save entire screen image to a texture. Will copy entire screen even if a
	 * viewport is in use. Texture param must be large enough to hold screen
	 * image (see makeTextureForScreen()).
	 * 
	 * @param txtrHandle
	 *            texture where screen image will be stored
	 * @see frameDraw()
	 * @see makeTextureForScreen()
	 */
	public static void frameCopy(int txtrHandle)
	{
		frameCopy(txtrHandle, 0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight()); // entire
																	// screen
	}

	/**
	 * Save a region of the screen to a texture. Texture must be large enough to
	 * hold screen image.
	 * 
	 * @param txtrHandle
	 *            texture where screen region will be stored
	 * @see frameDraw()
	 * @see makeTextureForScreen()
	 */
	public static void frameCopy(int txtrHandle, int x, int y, int w, int h)
	{
		GL11.glColor4f(1, 1, 1, 1); // turn off alpha and color tints
		GL11.glReadBuffer(GL11.GL_BACK);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, txtrHandle);
		// Copy screen to texture
		GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, x, y, w, h);
	}

	/**
	 * Save the current frame buffer to a PNG image. Exactly the same as
	 * screenShot().
	 * 
	 * @see screenShot()
	 */
	public static void frameSave()
	{
		screenShot();
	}

	// ========================================================================
	// Functions to render shapes.
	// ========================================================================


	/**
	 * Draws a circle with the given radius centered at the given world
	 * position.
	 */
	public static void drawCircleZ(int x, int y, int z, int radius,
			int linewidth)
	{
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(x, y, z);
			drawCircle(radius - linewidth, radius, 180);
		}
		GL11.glPopMatrix();
	}

	/**
	 * Draws a circle centered at 0,0,0. Use translate() to place circle at
	 * desired coords. Inner and outer radius specify width, stepsize is number
	 * of degrees for each segment.
	 */
	public static void drawCircle(float innerRadius, float outerRadius,
			int numSegments)
	{
		int s = 0; // start
		int e = 360; // end
		int stepSize = 360 / numSegments; // degrees per segment
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		{
			// add first 2 vertices
			float ts = (float) Math.sin(Math.toRadians(s));
			float tc = (float) Math.cos(Math.toRadians(s));
			GL11.glVertex2f(tc * innerRadius, ts * innerRadius);
			GL11.glVertex2f(tc * outerRadius, ts * outerRadius);
			// add intermediate vertices, snap to {step} degrees
			while ((s = ((s + stepSize) / stepSize) * stepSize) < e)
			{
				ts = (float) Math.sin(Math.toRadians(s));
				tc = (float) Math.cos(Math.toRadians(s));
				GL11.glVertex2f(tc * innerRadius, ts * innerRadius);
				GL11.glVertex2f(tc * outerRadius, ts * outerRadius);
			}
			// add last 2 vertices at end angle
			ts = (float) Math.sin(Math.toRadians(e));
			tc = (float) Math.cos(Math.toRadians(e));
			GL11.glVertex2f(tc * innerRadius, ts * innerRadius);
			GL11.glVertex2f(tc * outerRadius, ts * outerRadius);
		}
		GL11.glEnd();
	}

	/**
	 * call the LWJGL Sphere class to draw sphere geometry with texture
	 * coordinates and normals
	 * 
	 * @param facets
	 *            number of divisions around longitude and latitude
	 */
	public static void renderSphere(int facets)
	{
		Sphere s = new Sphere(); // an LWJGL class
		s.setOrientation(GLU.GLU_OUTSIDE); // normals point outwards
		s.setTextureFlag(true); // generate texture coords
		GL11.glPushMatrix();
		{
			GL11.glRotatef(-90f, 1, 0, 0); // rotate the sphere to align the
											// axis vertically
			s.draw(1, facets, facets); // run GL commands to draw sphere
		}
		GL11.glPopMatrix();
	}

	/**
	 * draw a sphere with 48 facets (pretty smooth) with normals and texture
	 * coords
	 */
	public static void renderSphere()
	{
		renderSphere(48);
	}

	/**
	 * Sets glLineWidth() and glPointSize() to the given width. This will affect
	 * geometry drawn using glBegin(GL_LINES), GL_LINE_STRIP, and GL_POINTS. May
	 * only work with widths up to 10 (depends on hardware).
	 */
	public static void setLineWidth(int width)
	{
		GL11.glLineWidth(width);
		GL11.glPointSize(width);
		// GL11.glEnable(GL11.GL_POINT_SMOOTH);
		// GL11.glEnable(GL11.GL_LINE_SMOOTH);
	}


	/**
	 * Enable/disable the color-material setting. When enabled, the glColor()
	 * command will change the current material color. This provides a
	 * convenient and efficient way to change material colors without having to
	 * call glMaterial(). When disabled, the glColor() command functions
	 * normally (has no affect on material colors).
	 * 
	 * @param on
	 *            when true, glColor() will set the current material color
	 */
	public static void setColorMaterial(boolean on)
	{
		if (on)
		{
			// glColor() will change the diffuse and ambient material colors
			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		} else
		{
			// glColor() behaves normally
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		}
	}

	// ========================================================================
	// Functions to build a character set and draw text strings.
	//
	// Example:
	// buildFont("Font_tahoma.png");
	// ...
	// glPrint(100, 100, 0, "Here's some text");
	// ...
	// destroyFont(); // cleanup
	// ========================================================================

	static int fontListBase = -1; // Base Display List For The character set
	static int fontTextureHandle = -1; // Texture handle for character set image

	/**
	 * Build a character set from the given texture image.
	 * 
	 * @param charSetImage
	 *            texture image containing 256 characters in a 16x16 grid
	 * @param fontWidth
	 *            how many pixels to allow per character on screen
	 * 
	 * @see destroyFont()
	 */
	public static boolean buildFont(String charSetImage, int fontWidth)
	{
		// make texture from image
		GLImage textureImg = loadImage(charSetImage);
		if (textureImg == null)
		{
			return false; // image not found
		}
		// pushAttrib();
		fontTextureHandle = makeTexture(textureImg);
		// build character set as call list of 256 textured quads
		buildFont(fontTextureHandle, fontWidth);
		// popAttrib();
		return true;
	}

	/**
	 * Build the character set display list from the given texture. Creates one
	 * quad for each character, with one letter textured onto each quad. Assumes
	 * the texture is a 256x256 image containing every character of the charset
	 * arranged in a 16x16 grid. Each character is 16x16 pixels. Call
	 * destroyFont() to release the display list memory.
	 * 
	 * Should be in ORTHO (2D) mode to render text (see setOrtho()).
	 * 
	 * Special thanks to NeHe and Giuseppe D'Agata for the "2D Texture Font"
	 * tutorial (http://nehe.gamedev.net).
	 * 
	 * @param charSetImage
	 *            texture image containing 256 characters in a 16x16 grid
	 * @param fontWidth
	 *            how many pixels to allow per character on screen
	 * 
	 * @see destroyFont()
	 */
	public static void buildFont(int fontTxtrHandle, int fontWidth)
	{
		float factor = 1f / 16f;
		float cx, cy;
		fontListBase = GL11.glGenLists(256); // Creating 256 Display Lists
		for (int i = 0; i < 256; i++)
		{
			cx = (float) (i % 16) / 16f; // X Texture Coord Of Character (0 -
											// 1.0)
			cy = (float) (i / 16) / 16f; // Y Texture Coord Of Character (0 -
											// 1.0)
			GL11.glNewList(fontListBase + i, GL11.GL_COMPILE); // Start Building
																// A List
			GL11.glBegin(GL11.GL_QUADS); // Use A 16x16 pixel Quad For Each
											// Character
			GL11.glTexCoord2f(cx, 1 - cy - factor); // Texture Coord (Bottom
													// Left)
			GL11.glVertex2i(0, 0);
			GL11.glTexCoord2f(cx + factor, 1 - cy - factor); // Texture Coord
																// (Bottom
																// Right)
			GL11.glVertex2i(16, 0);
			GL11.glTexCoord2f(cx + factor, 1 - cy); // Texture Coord (Top Right)
			GL11.glVertex2i(16, 16);
			GL11.glTexCoord2f(cx, 1 - cy); // Texture Coord (Top Left)
			GL11.glVertex2i(0, 16);
			GL11.glEnd(); // Done Building Our Quad (Character)
			GL11.glTranslatef(fontWidth, 0, 0); // Move To The Right Of The
												// Character
			GL11.glEndList(); // Done Building The Display List
		} // Loop Until All 256 Are Built
	}

	/**
	 * Clean up the allocated display lists for the character set.
	 */
	public static void destroyFont()
	{
		if (fontListBase != -1)
		{
			GL11.glDeleteLists(fontListBase, 256);
			fontListBase = -1;
		}
	}

	// ========================================================================
	// PBuffer functions
	//
	// Pbuffers are offscreen buffers that can be rendered into just like
	// the regular framebuffer. A pbuffer can be larger than the screen,
	// which allows for the creation of higher resolution images.
	//
	// ========================================================================

	/**
	 * Create a Pbuffer for use as an offscreen buffer, with the given width and
	 * height. Use selectPbuffer() to make the pbuffer the context for all
	 * subsequent opengl commands. Use selectDisplay() to make the Display the
	 * context for opengl commands.
	 * <P>
	 * 
	 * @param width
	 * @param height
	 * @return Pbuffer
	 * @see selectPbuffer(), selectDisplay()
	 */
	public static Pbuffer makePbuffer(final int width, final int height)
	{
		Pbuffer pbuffer = null;
		try
		{
			pbuffer = new Pbuffer(width, height, new PixelFormat(24, // bitsperpixel
					8, // alpha
					24, // depth
					8, // stencil
					0), // samples
					null, null);
		} catch (LWJGLException e)
		{
			err("GLUtils.makePbuffer(): exception " + e);
		}
		return pbuffer;
	}

	/**
	 * Make the pbuffer the current context for opengl commands. All following
	 * gl functions will operate on this buffer instead of the display.
	 * <P>
	 * NOTE: the Pbuffer may be recreated if it was lost since last used. It's a
	 * good idea to use:
	 * 
	 * <PRE>
	 * pbuff = selectPbuffer(pbuff);
	 * </PRE>
	 * 
	 * to hold onto the new Pbuffer reference if Pbuffer was recreated.
	 * 
	 * @param pb
	 *            pbuffer to make current
	 * @return Pbuffer
	 * @see selectDisplay(), makePbuffer()
	 */
	public static Pbuffer selectPbuffer(Pbuffer pb)
	{
		if (pb != null)
		{
			try
			{
				// re-create the buffer if necessary
				if (pb.isBufferLost())
				{
					int w = pb.getWidth();
					int h = pb.getHeight();
					msg("GLUtils.selectPbuffer(): Buffer contents lost - recreating the pbuffer");
					pb.destroy();
					pb = makePbuffer(w, h);
				}
				// select the pbuffer for rendering
				pb.makeCurrent();
			} catch (LWJGLException e)
			{
				err("GLUtils.selectPbuffer(): exception " + e);
			}
		}
		return pb;
	}

	/**
	 * Make the Display the current context for OpenGL commands. Subsequent gl
	 * functions will operate on the Display.
	 * 
	 * @see selectPbuffer()
	 */
	public static void selectDisplay()
	{
		try
		{
			Display.makeCurrent();
		} catch (LWJGLException e)
		{
			err("GLUtils.selectDisplay(): exception " + e);
		}
	}

	/**
	 * Copy the pbuffer contents to a texture. (Should this use
	 * glCopyTexSubImage2D()? Is RGB the fastest format?)
	 */
	public static void frameCopy(Pbuffer pbuff, int textureHandle)
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0,
				pbuff.getWidth(), pbuff.getHeight(), 0);
	}

	/**
	 * Save the current frame buffer to a PNG image. Same as
	 * screenShot(filename) but the screenshot filename will be automatically
	 * set to <applicationClassName>-<timestamp>.png
	 */
	public static void screenShot()
	{
		screenShot(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(),
				rootClass.getName() + "-" + makeTimestamp() + ".png");
	}

	/**
	 * Save the current frame buffer to a PNG image. Can also be used with the
	 * PBuffer class to copy large images or textures that have been rendered
	 * into the offscreen pbuffer.
	 */
	public static void screenShot(String imageFilename)
	{
		screenShot(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(),
				imageFilename);
	}

	/**
	 * Save the current Pbuffer to a PNG image. Same as screenShot(filename) but
	 * the Pbuffer will be saved instead of the framebuffer, and the screenshot
	 * filename will be set to <applicationClassName>-<timestamp>.png NOTE: Have
	 * to call selectPbuffer() before calling this function.
	 */
	public static void screenShot(Pbuffer pb)
	{
		screenShot(0, 0, pb.getWidth(), pb.getHeight(), rootClass.getName()
				+ "_" + makeTimestamp() + ".png");
	}

	/**
	 * Save a region of the current render buffer to a PNG image. If the current
	 * buffer is the framebuffer then this will work as a screen capture. Can
	 * also be used with the PBuffer class to copy large images or textures that
	 * have been rendered into the offscreen pbuffer.
	 * <P>
	 * WARNING: this function hogs memory! Call java with more memory (java
	 * -Xms128m -Xmx128m)
	 * <P>
	 * 
	 * @see selectPbuffer(Pbuffer)
	 * @see selectDisplay()
	 * @see savePixelsToPNG()
	 */
	public static void screenShot(int x, int y, int width, int height,
			String imageFilename)
	{
		// allocate space for ARBG pixels
		ByteBuffer framebytes = allocBytes(width * height * SIZE_INT);
		int[] pixels = new int[width * height];
		// grab the current frame contents as ARGB ints (BGRA ints reversed)
		GL11.glReadPixels(x, y, width, height, GL12.GL_BGRA,
				GL12.GL_UNSIGNED_INT_8_8_8_8_REV, framebytes);
		// copy ARGB data from ByteBuffer to integer array
		framebytes.asIntBuffer().get(pixels, 0, pixels.length);
		// free up this memory
		framebytes = null;
		// flip the pixels vertically and save to file
		GLImage.savePixelsToPNG(pixels, width, height, imageFilename, true);
	}

	/**
	 * Save a ByteBuffer of ARGB pixels to a PNG file. If flipY is true, flip
	 * the pixels on the Y axis before saving.
	 */
	public static void savePixelsToPNG(ByteBuffer framebytes, int width,
			int height, String imageFilename, boolean flipY)
	{
		if (framebytes != null && imageFilename != null)
		{
			// copy ARGB data from ByteBuffer to integer array
			int[] pixels = new int[width * height];
			framebytes.asIntBuffer().get(pixels, 0, pixels.length);
			// save pixels to file
			GLImage.savePixelsToPNG(pixels, width, height, imageFilename, flipY);
		}
	}

	/**
	 * Save the contents of the current render buffer to a PNG image. This is an
	 * older version of screenShot() that used the default OpenGL GL_RGBA pixel
	 * format which had to be swizzled into an ARGB format. I'm keeping the
	 * function here for reference.
	 * <P>
	 * If the current buffer is the framebuffer then this will work as a screen
	 * capture. Can also be used with the PBuffer class to copy large images or
	 * textures that have been rendered into the offscreen pbuffer.
	 * <P>
	 * WARNING: this function hogs memory! Call java with more memory (java
	 * -Xms128m -Xmx128)
	 * <P>
	 * 
	 * @see selectPbuffer(), selectDisplay()
	 */
	public static void screenShotRGB(int width, int height, String saveFilename)
	{
		// allocate space for RBG pixels
		ByteBuffer framebytes = GLUtils.allocBytes(width * height * 3);
		int[] pixels = new int[width * height];
		int bindex;
		// grab a copy of the current frame contents as RGB (has to be
		// UNSIGNED_BYTE or colors come out too dark)
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB,
				GL11.GL_UNSIGNED_BYTE, framebytes);
		// copy RGB data from ByteBuffer to integer array
		for (int i = 0; i < pixels.length; i++)
		{
			bindex = i * 3;
			pixels[i] = 0xFF000000 // A
					| ((framebytes.get(bindex) & 0x000000FF) << 16) // R
					| ((framebytes.get(bindex + 1) & 0x000000FF) << 8) // G
					| ((framebytes.get(bindex + 2) & 0x000000FF) << 0); // B
		}
		// free up some memory
		framebytes = null;
		// save to file (flip Y axis before saving)
		GLImage.savePixelsToPNG(pixels, width, height, saveFilename, true);
	}

	// ========================================================================
	// Stencil functions
	// ========================================================================

	/**
	 * clear the stencil buffer
	 */
	public static void clearMask()
	{
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}

	/**
	 * Begin creating a mask. This function turns off the color and depth
	 * buffers so all subsequent drawing will go only into the stencil buffer.
	 * To use: beginMask(1); renderModel(); // draw some geometry endMask();
	 */
	public static void beginMask(int maskvalue)
	{
		// turn off writing to the color buffer and depth buffer
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);

		// enable stencil buffer
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		// set the stencil test to ALWAYS pass
		GL11.glStencilFunc(GL11.GL_ALWAYS, maskvalue, 0xFFFFFFFF);
		// REPLACE the stencil buffer value with maskvalue whereever we draw
		GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
	}

	/**
	 * End the mask. Freeze the stencil buffer and activate the color and depth
	 * buffers.
	 */
	public static void endMask()
	{
		// don't let future drawing modify the contents of the stencil buffer
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

		// turn the color and depth buffers back on
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
	}

	/**
	 * Restrict rendering to the masked area. To use: GLStencil.beginMask(1);
	 * renderModel(); GLStencil.endMask();
	 */
	public static void activateMask(int maskvalue)
	{
		// enable stencil buffer
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		// until stencil test is disabled, only write to areas where the
		// stencil buffer equals the mask value
		GL11.glStencilFunc(GL11.GL_EQUAL, maskvalue, 0xFFFFFFFF);
	}

	/**
	 * turn off the stencil test so stencil has no further affect on rendering.
	 */
	public static void disableMask()
	{
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	/**
	 * Return the stencil buffer value at the given screen position.
	 */
	public static int getMaskValue(int x, int y)
	{
		tmpByte.clear();
		// read the stencil value at the given position, as an unsigned byte,
		// store it in tmpByte
		GL11.glReadPixels(x, y, 1, 1, GL11.GL_STENCIL_INDEX,
				GL11.GL_UNSIGNED_BYTE, tmpByte);
		return (int) tmpByte.get(0);
	}


	// ========================================================================
	// Native IO Buffer allocation functions
	//
	// These functions create and populate the native buffers used by LWJGL.
	// ========================================================================

	public static ByteBuffer allocBytes(int howmany)
	{
		return ByteBuffer.allocateDirect(howmany * SIZE_BYTE).order(
				ByteOrder.nativeOrder());
	}

	public static IntBuffer allocInts(int howmany)
	{
		return ByteBuffer.allocateDirect(howmany * SIZE_INT)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	public static FloatBuffer allocFloats(int howmany)
	{
		return ByteBuffer.allocateDirect(howmany * SIZE_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static DoubleBuffer allocDoubles(int howmany)
	{
		return ByteBuffer.allocateDirect(howmany * SIZE_DOUBLE)
				.order(ByteOrder.nativeOrder()).asDoubleBuffer();
	}

	public static ByteBuffer allocBytes(byte[] bytearray)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect(bytearray.length * SIZE_BYTE)
				.order(ByteOrder.nativeOrder());
		bb.put(bytearray).flip();
		return bb;
	}

	public static IntBuffer allocInts(int[] intarray)
	{
		IntBuffer ib = ByteBuffer.allocateDirect(intarray.length * SIZE_FLOAT)
				.order(ByteOrder.nativeOrder()).asIntBuffer();
		ib.put(intarray).flip();
		return ib;
	}

	public static FloatBuffer allocFloats(float[] floatarray)
	{
		FloatBuffer fb = ByteBuffer
				.allocateDirect(floatarray.length * SIZE_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		fb.put(floatarray).flip();
		return fb;
	}

	public static DoubleBuffer allocDoubles(double[] darray)
	{
		DoubleBuffer fb = ByteBuffer
				.allocateDirect(darray.length * SIZE_DOUBLE)
				.order(ByteOrder.nativeOrder()).asDoubleBuffer();
		fb.put(darray).flip();
		return fb;
	}

	public static void put(ByteBuffer b, byte[] values)
	{
		b.clear();
		b.put(values).flip();
	}

	public static void put(IntBuffer b, int[] values)
	{
		b.clear();
		b.put(values).flip();
	}

	public static void put(FloatBuffer b, float[] values)
	{
		b.clear();
		b.put(values).flip();
	}

	public static void put(DoubleBuffer b, double[] values)
	{
		b.clear();
		b.put(values).flip();
	}

	/**
	 * copy ints from the given byteBuffer into the given int array.
	 * 
	 * @param b
	 *            source ByteBuffer
	 * @param values
	 *            target integer array, must be same length as ByteBuffer
	 *            capacity/4
	 */
	public static void get(ByteBuffer b, int[] values)
	{
		b.asIntBuffer().get(values, 0, values.length);
	}

	/**
	 * copy ints from the given IntBuffer into the given int array.
	 * 
	 * @param b
	 *            source IntBuffer
	 * @param values
	 *            target integer array, must be same length as IntBuffer
	 */
	public static void get(IntBuffer b, int[] values)
	{
		b.get(values, 0, values.length);
	}

	/**
	 * return the contents of the byteBuffer as an array of ints.
	 * 
	 * @param b
	 *            source ByteBuffer
	 */
	public static int[] getInts(ByteBuffer b)
	{
		int[] values = new int[b.capacity() / SIZE_INT];
		b.asIntBuffer().get(values, 0, values.length);
		return values;
	}

	// ========================================================================
	// Misc functions
	// ========================================================================
	public static URL appletBaseURL = null;
	public static Class rootClass = GLUtils.class;

	public static boolean showMessages;

	/**
	 * Open the given file and return the InputStream. This function assumes 1)
	 * that we're running an application and the file is in the local
	 * filesystem. If not found, then assume 2) we're in a jar file and look for
	 * the file in the current jar. If not found, then assume 3) we're running
	 * an applet and look for the file relative to the applet code base.
	 * 
	 * @param filename
	 *            to open
	 */
	public static InputStream getInputStream(String filename)
	{
		InputStream in = null;

		// 1) look for file in local filesystem
		try
		{
			in = new FileInputStream(filename);
		} catch (IOException ioe)
		{
			msg("GLUtils.getInputStream (" + filename + "): " + ioe);
			if (in != null)
			{
				try
				{
					in.close();
				} catch (Exception e)
				{
				}
				in = null;
			}
		} catch (Exception e)
		{
			msg("GLUtils.getInputStream (" + filename + "): " + e);
		}

		// 2) if couldn't open file, look in jar
		if (in == null && rootClass != null)
		{
			// NOTE: class.getResource() looks for files relative to the folder
			// that the class is in.
			// ideally the class will be an application in the root of the
			// installation, see setRootClass().
			URL u = null;
			if (filename.startsWith("."))
			{ // remove leading . ie. "./file"
				filename = filename.substring(1);
			}
			try
			{
				u = rootClass.getResource(filename);
			} catch (Exception ue)
			{
				msg("GLUtils.getInputStream(): Can't find resource: " + ue);
			}
			// msg("GLUtils.getInputStream (" +filename+
			// "): try jar resource url=" + u);
			if (u != null)
			{
				try
				{
					in = u.openStream();
				} catch (Exception e)
				{
					msg("GLUtils.getInputStream (" + filename
							+ "): Can't load from jar: " + e);
				}
			}

			// 3) try loading file from applet base url
			if (in == null && appletBaseURL != null)
			{
				try
				{
					u = new URL(appletBaseURL, filename);
				} catch (Exception ue)
				{
					msg("GLUtils.getInputStream(): Can't make applet base url: "
							+ ue);
				}
				// msg("GLUtils.getInputStream (" +filename+
				// "): try applet base url=" + u);
				try
				{
					in = u.openStream();
				} catch (Exception e)
				{
					msg("GLUtils.getInputStream (" + filename
							+ "): Can't load from applet base URL: " + e);
				}
			}
		}
		return in;
	}

	/**
	 * Return an array of bytes read from an InputStream. Reads all bytes until
	 * the end of stream. Can read an arbitrary number of bytes. NOTE: Does not
	 * close the inputStream!
	 */
	public static byte[] getBytesFromStream(InputStream is)
	{
		int chunkSize = 1024;
		int totalRead = 0;
		int num = 0;
		byte[] bytes = new byte[chunkSize];
		ArrayList byteChunks = new ArrayList();

		// Read the bytes in chunks of 1024
		try
		{
			while ((num = is.read(bytes)) >= 0)
			{
				byteChunks.add(bytes);
				bytes = new byte[chunkSize];
				totalRead += num;
			}
		} catch (IOException ioe)
		{
			err("GLUtils.getBytesFromStream(): IOException " + ioe);
		}

		int numCopied = 0;
		bytes = new byte[totalRead];

		// copy byte chunks to byte array (last chunk may be partial)
		while (byteChunks.size() > 0)
		{
			byte[] byteChunk = (byte[]) byteChunks.get(0);
			int copylen = (totalRead - numCopied > chunkSize) ? chunkSize
					: (totalRead - numCopied);
			System.arraycopy(byteChunk, 0, bytes, numCopied, copylen);
			byteChunks.remove(0);
			numCopied += copylen;
		}

		msg("getBytesFromStream() read " + numCopied + " bytes.");

		return bytes;
	}

	/**
	 * Return an array of bytes read from a file.
	 */
	public static byte[] getBytesFromFile(String filename)
	{
		InputStream is = getInputStream(filename);
		byte[] bytes = getBytesFromStream(is);
		try
		{
			is.close();
		} catch (IOException ioe)
		{
			err("GLUtils.getBytesFromFile(): IOException " + ioe);
		}
		return bytes;
	}

	/**
	 * Return a String array containing the path portion of a filename
	 * (result[0]), and the fileame (result[1]). If there is no path, then
	 * result[0] will be "" and result[1] will be the full filename.
	 */
	public static String[] getPathAndFile(String filename)
	{
		String[] pathAndFile = new String[2];
		Matcher matcher = Pattern.compile("^.*/").matcher(filename);
		if (matcher.find())
		{
			pathAndFile[0] = matcher.group();
			pathAndFile[1] = filename.substring(matcher.end());
		} else
		{
			pathAndFile[0] = "";
			pathAndFile[1] = filename;
		}
		return pathAndFile;
	}

	/**
	 * Hold onto this Class for later class.getResource() calls (to load
	 * resources from JAR files, see getInputStream()) and also to get class
	 * name for use in screenshot filenames (see screenShot()).
	 * <P>
	 * To load files from a jar we need to access a class in the root folder of
	 * the installation. It's not good to use GLUtils.class because that class is
	 * in the glapp package folder, and the getResource() function will not find
	 * model, image and sound files because they're a level higher in the folder
	 * tree. Below we call this.getClass() to record the class of the
	 * application that subclasses GLUtils, ie. assume we create an app MyGame
	 * that extends GLUtils, and MyGame.class is in the root folder of the
	 * installation:
	 * 
	 * <PRE>
	 *      MyGame.class
	 *      models (folder)
	 *      images (folder)
	 *      sounds (folder)
	 * </PRE>
	 * 
	 * In this case setRootClass() will set the rootClass to MyGame. If MyGame
	 * and subfolders are packaged in a jar file, then getInputStream() should
	 * be able to do a rootClass.getResource("models/some_model.obj") and
	 * correctly retrieve the file from the JAR.
	 * <P>
	 * 
	 * @see getInputStream()
	 */
	public void setRootClass()
	{
		rootClass = this.getClass();
	}

	/**
	 * make a time stamp for filename
	 * 
	 * @return a string with format "YYYYMMDD-hhmmss"
	 */
	public static String makeTimestamp()
	{
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hours = now.get(Calendar.HOUR_OF_DAY);
		int minutes = now.get(Calendar.MINUTE);
		int seconds = now.get(Calendar.SECOND);
		String datetime = "" + year + (month < 10 ? "0" : "") + month
				+ (day < 10 ? "0" : "") + day + "-" + (hours < 10 ? "0" : "")
				+ hours + (minutes < 10 ? "0" : "") + minutes
				+ (seconds < 10 ? "0" : "") + seconds;
		return datetime;
	}

	/**
	 * Return a random floating point value between 0 and 1
	 */
	public static float random()
	{
		return (float) Math.random();
	}

	/**
	 * Return a random floating point value between 0 and upperbound (not
	 * including upperbound)
	 */
	public static float random(float upperbound)
	{
		return (float) (Math.random() * (double) upperbound);
	}

	/**
	 * Return a random integer value between 0 and upperbound (not including
	 * upperbound)
	 */
	public static int random(int upperbound)
	{
		return (int) (Math.random() * (double) upperbound);
	}

	/**
	 * Round a float value to the nearest int.
	 */
	public static int round(float f)
	{
		return Math.round(f);
	}

	/**
	 * Return true if the OpenGL context supports the given OpenGL extension.
	 */
	public static boolean extensionExists(String extensionName)
	{
		if (OpenGLextensions == null)
		{
			String[] GLExtensions = GL11.glGetString(GL11.GL_EXTENSIONS).split(
					" ");
			OpenGLextensions = new Hashtable();
			for (int i = 0; i < GLExtensions.length; i++)
			{
				OpenGLextensions.put(GLExtensions[i].toUpperCase(), "");
			}
		}
		return (OpenGLextensions.get(extensionName.toUpperCase()) != null);
	}

	/**
	 * Show a debug message on the system console (calls System.out.println()).
	 * If showMessages flag is false, does nothing.
	 * 
	 * @param text
	 */
	public static void msg(String text)
	{
		if (showMessages)
		{
			System.out.println(text);
		}
	}

	/**
	 * Show an error message on the system console (calls System.out.println()).
	 * Does not check showMessages flag.
	 * 
	 * @param text
	 */
	public static void err(String text)
	{
		System.err.println(text);
	}

	/**
	 * Find a method in the given class with the given method name. Assumes the
	 * method takes no parameters. The returned Method can be executed later
	 * using invoke() (similar to a callback function in C/C++).
	 * <P>
	 * NOTE: method invocation is very fast for methods that take no parameters.
	 * If the method takes parameters then invoking is much slower than calling
	 * the function directly through code. For this reason and for simplicity I
	 * assume there are no parameters on the function.
	 * 
	 * @param object
	 *            object that has the method we want to invoke
	 * @param methodName
	 *            name of function that we want to invoke
	 * @return the Method object
	 * @see invoke()
	 */
	public static Method method(Object object, String methodName)
	{
		Method M = null;
		try
		{
			// Look for a method with the given name and no parameters
			M = object.getClass().getMethod(methodName, null);
		} catch (Exception e)
		{
			err("GLUtils.method(): Can't find method (" + methodName + ").  " + e);
		}
		return M;
	}

	/**
	 * Similar to the static method() function, this looks for the method in the
	 * GLUtils class (or it's subclass).
	 * 
	 * @param methodName
	 *            name of function that we want to invoke
	 * @return the Method object
	 * @see invoke()
	 */
	public Method method(String methodName)
	{
		return method(this, methodName);
	}

	/**
	 * Execute a method on the given object. Assumes the method takes no
	 * parameters. Useful as a callback function.
	 * 
	 * @param object
	 *            (the object to call the method on)
	 * @param method
	 *            (the method that will be executed)
	 * @see method()
	 */
	public static void invoke(Object object, Method method)
	{
		if (object != null && method != null)
		{
			try
			{
				// Call the method with this object as the argument!
				method.invoke(object, null);
			} catch (Exception e)
			{
				// Error handling
				System.err.println("GLUtils.invoke(): couldn't invoke method "
						+ method.getName() + " on object "
						+ object.getClass().getName());
			}
		}
	}

	/**
	 * Similar to the static invoke() function, this execute a method on the
	 * GLUtils class or subclass. Assumes the method takes no parameters. Useful
	 * as a callback function.
	 * 
	 * @param method
	 *            (the method that will be executed)
	 * @see method()
	 */
	public void invoke(Method method)
	{
		if (method != null)
		{
			try
			{
				// Call the method with this object as the argument!
				method.invoke(this, null);
			} catch (Exception e)
			{
				// Error handling
				System.err.println("GLUtils.invoke(): couldn't invoke method "
						+ method.getName() + " on object "
						+ this.getClass().getName());
			}
		}
	}
}