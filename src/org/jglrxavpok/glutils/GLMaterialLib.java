
package org.jglrxavpok.glutils;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jglrxavpok.glutils.*;

/**
 *  Loads a library of materials from a .mtl file into an array of GLMaterial objects.
 */
public class GLMaterialLib {
    // path to the .mtl file (loadMaterial() will set these)
    // we'll load texture images from same folder as material file
    public String filepath = "";
    public String filename = "";

    // array of materials loaded from .mtl file
    GLMaterial[] materials;


    public GLMaterialLib(String mtlFilename) {
    	if (mtlFilename != null && mtlFilename.length() > 0) {
    		materials = loadMaterials(mtlFilename);
    	}
    }

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *
	 * functions to load and save materials (from/to .mtl file)
	 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public GLMaterial[] loadMaterials(String mtlFilename) {
        GLMaterial[] mtls = null;
        // Separate leading path from filename (we'll load textures from same folder)
        String[] pathParts = GLUtils.getPathAndFile(mtlFilename);
        filepath = pathParts[0];
        filename = pathParts[1];
        // read the mtl file
        try {
        	mtls = loadMaterials(new BufferedReader(new InputStreamReader(GLUtils.getInputStream(mtlFilename))));
        }
        catch (Exception e) {
        	System.out.println("GLMaterialLib.loadMaterials(): Exception when loading " + mtlFilename + ": " + e);
        }
    	return mtls;
    }

    /**
     * MTL file format in a nutshell:
     *
     * <PRE>
     * 		newmtl white          // begin material and specify name
     * 		Kd 1.0 1.0 1.0        // diffuse rgb
     * 		Ka 0.2 0.2 0.2        // ambient rgb
     * 		Ks 0.6 0.6 0.6        // specular rgb
     * 		Ns 300                // shininess 0-1000
     *      d 0.5                 // alpha 0-1
     *      map_Kd texture.jpg    // texture file
     *                            // blank line ends material definition
     * </PRE>
     */
    public GLMaterial[] loadMaterials(BufferedReader br) {
    	ArrayList mtlslist = new ArrayList();
    	GLMaterial material = null;
    	String line = "";

    	float[] rgb;
    	try {
    		while ((line = br.readLine()) != null) {
    			// remove extra whitespace
    			line = line.trim();
    			if (line.length() > 0) {
    				if (line.startsWith("#")) {
    					// ignore comments
    				}
    				else if (line.startsWith("newmtl")) {
    					// newmtl some_name
    					material = new GLMaterial();  // start new material
    					material.setName(line.substring(7));
    					mtlslist.add(material);      // add to list
    				}
    				else if (line.startsWith("Kd")) {
    					// Kd 1.0 0.0 0.5
    					if ((rgb = read3Floats(line)) != null) {
    						material.setDiffuse(rgb);
    					}
    				}
    				else if (line.startsWith("Ka")) {
    					// Ka 1.0 0.0 0.5
    					if ((rgb = read3Floats(line)) != null) {
    						material.setAmbient(rgb);
    					}
    				}
    				else if (line.startsWith("Ks")) {
    					// Ks 1.0 0.0 0.5
    					if ((rgb = read3Floats(line)) != null) {
    						material.setSpecular(rgb);
    					}
    				}
    				else if (line.startsWith("Ns")) {
    					// Ns 500.5
    					// shininess in mtl file is 0-1000
    					if ((rgb = read3Floats(line)) != null) {
    						// convert to opengl 0-127
    						int shininessValue = (int) ((rgb[0] / 1000f) * 127f);
    						material.setShininess( shininessValue );
    					}
    				}
    				else if (line.startsWith("d")) {
    					// d 1.0
    					// alpha value of material 0=transparent 1=opaque
    					if ((rgb = read3Floats(line)) != null) {
    						material.setAlpha( rgb[0] );
    					}
    				}
    				else if (line.startsWith("illum")) {
    					// illum (0, 1, or 2)
    					// lighting for material 0=disable, 1=ambient & diffuse (specular is black), 2 for full lighting.
    					if ((rgb = read3Floats(line)) != null) {
    						// not yet
    					}
    				}
    				else if (line.startsWith("map_Kd")) {
    					// map_Kd filename
    					// add a texture to the material
    				    if(line.length() >= 8)
    				    {
        					String textureFile = line.substring(7);
        			        if (textureFile != null && !textureFile.equals("")) {
            					int textureHandle = 0;
        			        	try {
        					    	textureHandle = GLUtils.makeTexture(filepath + textureFile);
        			        	}
        			        	catch (Exception e) {
        			        		System.out.println("GLMaterialLib.loadMaterials(): could not load texture file (" +line+ ")" + e);
        			        		e.printStackTrace();
        			        	}
            					material.setTextureFile(textureFile);
            					material.setTexture(textureHandle);
        			        }
    				    }
    				}
    			}
    		}
    	} catch (Exception e) {
    		System.out.println("GLMaterialLib.loadMaterials() failed at line: " + line);
    		e.printStackTrace();
    	}
    	// debug:
    	System.out.println("GLMaterialLib.loadMaterials(): loaded " + mtlslist.size() + " materials ");
    	// return array of materials
    	GLMaterial[] mtls = new GLMaterial[ mtlslist.size() ];
    	mtlslist.toArray(mtls);
    	return mtls;
    }

    // always return array of four floats (usually containing RGBA, but
    // in some cases contains only one value at pos 0).
    private float[] read3Floats(String line)
    {
    	try
    	{
    		StringTokenizer st = new StringTokenizer(line, " ");
    		st.nextToken();   // throw out line identifier (Ka, Kd, etc.)
    		if (st.countTokens() == 1) {
    			return new float[] {Float.parseFloat(st.nextToken()), 0f, 0f, 0f};
    		}
    		else if (st.countTokens() == 3) { // RGBA (force A to 1)
    			return new float[] {Float.parseFloat(st.nextToken()),
    					Float.parseFloat(st.nextToken()),
    					Float.parseFloat(st.nextToken()),
    					1f };
    		}
    	}
    	catch (Exception e)
    	{
    		System.out.println("GLMaterialLib.read3Floats(): error on line '" + line + "', " + e);
    	}
    	return null;
    }

    /**
     * Write an array of GLMaterial objects to a .mtl file.
     * @param mtls      array of materials to write to file
     * @param filename  name of .mtl file
     */
    public void writeLibe(GLMaterial[] mtls, String filename) {
    	try {
    		PrintWriter mtlfile = new PrintWriter(new FileWriter(filename));
    		writeLibe(mtls, mtlfile);
    		mtlfile.close();
    	} catch (IOException e) {
    		System.out.println("GLMaterialLib.writeLibe(): IOException:" + e);
    	}
    }

    public void writeLibe(GLMaterial[] mtls, PrintWriter out) {
    	if (out != null) {
    		out.println("#");
    		out.println("# Wavefront material file for use with OBJ file");
    		out.println("# Created by GLMaterialLib.java");
    		out.println("#");
    		out.println("");
    		for (int i = 0; i < mtls.length; i++) {
    			write(out, mtls[i]);
    		}
    	}
    }

    /**
     *  Write one material.
     */
    public void write(PrintWriter out, GLMaterial mtl)
    {
    	if (out != null) {
    		out.println("newmtl " + mtl.mtlname);
    		out.println("Ka " + mtl.ambient.get(0) + " " + mtl.ambient.get(1) + " " + mtl.ambient.get(2));
    		out.println("Kd " + mtl.diffuse.get(0) + " " + mtl.diffuse.get(1) + " " + mtl.diffuse.get(2));
    		out.println("Ks " + mtl.specular.get(0) + " " + mtl.specular.get(1) + " " + mtl.specular.get(2));
    		out.println("Ns " + ( (mtl.shininess.get(0) / 128.0) * 1000.0));
    		if (mtl.textureFile != null && !mtl.textureFile.equals("")) {
    			out.println("map_Kd " + mtl.textureFile);
    		}
    		if (mtl.getAlpha() != 1f) {
    			out.println("d " + mtl.getAlpha());
    		}
    		out.println("");
    	}
    }

    /**
     * return a duplicate of this material.  all values are duplicated except
     * the texture, which is passed by reference to the clone (to prevent
     * multiple copies of the same texture).
     *
     * @return the cloned material
     */
    public GLMaterial getClone(GLMaterial mtl) {
    	GLMaterial clone = new GLMaterial();
    	clone.setDiffuse( new float[] {mtl.diffuse.get(0),mtl.diffuse.get(1),mtl.diffuse.get(2),mtl.diffuse.get(3)} );
    	clone.setAmbient( new float[] {mtl.ambient.get(0),mtl.ambient.get(1),mtl.ambient.get(2),mtl.ambient.get(3)} );
    	clone.setSpecular( new float[] {mtl.specular.get(0),mtl.specular.get(1),mtl.specular.get(2),mtl.specular.get(3)} );
    	clone.setGlowColor( new float[] {mtl.emission.get(0),mtl.emission.get(1),mtl.emission.get(2),mtl.emission.get(3)} );
    	clone.setShininess( mtl.shininess.get(0) );
    	// set the texture filename and handle in the clone (clones share 1 texture)
    	clone.textureFile = mtl.textureFile;
    	clone.textureHandle = mtl.textureHandle;
    	clone.setName( mtl.mtlname + "-copy");
    	return clone;
    }

    /**
     * find a material by name in an array of GLMaterial objects
     */
    public GLMaterial find(String materialName) {
    	int mtl_idx = findID(materialName);
    	if (mtl_idx >= 0) {
    		return materials[mtl_idx];
    	}
    	return null;
    }

    /**
     * find a material by name in an array of GLMaterial objects
     * return the array index of the material
     */
    public int findID(String materialName) {
    	if (materials != null && materialName != null) {
    		for (int m=0; m < materials.length; m++) {
    			if (materials[m].mtlname.equals(materialName)) {
    				return m;
    			}
    		}
    	}
    	return -1;
    }

}