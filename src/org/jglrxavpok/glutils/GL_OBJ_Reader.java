package org.jglrxavpok.glutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jglrxavpok.glutils.*;

/** 
 * Based on Object3D.java by Jeremy Adams (elias4444) august 2005
 *
 * Read an OBJ file into ArrayLists which can then be imported into a final
 * mesh class.  This class just loads and holds data, and can be used independently
 * of specific 3D model classes.
 *
 * Populates a "faces" Arraylist with all faces in the mesh.  Also stores face
 * groups in the "groups" Arraylist (an Arraylist that contains Group objects).
 *
 * jul 2006: add Group class to hold group faces, materialname and groupname
 * jul 2006: read usemtl command and store material names with groups
 *
 */
public class GL_OBJ_Reader {
    // These three hold the vertex, texture and normal coordinates
    // There is only one set of verts, textures, normals for entire file
    public ArrayList vertices = new ArrayList();  // Contains float[3] for each Vertex (XYZ)
    public ArrayList normals = new ArrayList();     // Contains float[3] for each normal
    public ArrayList textureCoords = new ArrayList();  // Contains float[3] for each texture map coord (UVW)

    // Hold all faces in the mesh
    public ArrayList faces = new ArrayList();

    // Holds groups of faces (with name and material for each group)
    public ArrayList groups = new ArrayList();

    // name of material file, or null if no material libe is given
    public String materialLibeName = null;

    // materials loaded from .mtl file (or a default material if no mtl file is found)
    public GLMaterialLib materialLib;

    // path and name of .obj file
    public String filepath = "";
    public String filename = "";

    // mesh min and max points
    public float leftpoint = 0;    // x-
    public float rightpoint = 0;   // x+
    public float bottompoint = 0;  // y-
    public float toppoint = 0;     // y+
    public float farpoint = 0;     // z-
    public float nearpoint = 0;    // z+


    public GL_OBJ_Reader(String objfilename) {  // Construct from file name
        loadobject(objfilename);
    }


    public GL_OBJ_Reader(InputStream in) {  // Construct from inputstream
        loadobject(in);
    }


    public void loadobject(String objfilename) {  // load from String filename
        if (objfilename != null && objfilename.length() > 0) {
            // Separate leading path from filename (we'll load material libe from same folder)
            String[] pathParts = GLUtils.getPathAndFile(objfilename);
            filepath = pathParts[0];
            filename = pathParts[1];
            // Load it
            try {
                loadobject(GLUtils.getInputStream(objfilename));
            }
            catch (Exception e) {
                System.out.println("GL_OBJ_Reader.loadobject(): Failed to read file: " + objfilename + " " + e);
            }
        }
    }


    public void loadobject(InputStream in) {  // load from inputStream
        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            loadobject(br);
        }
    }

    /**
     * OBJ file format in a nutshell:
     * First part of file lists vertex data: vert coords, texture coords and normals.
     * These lines start with v, vt and vn respectively.
     * Second part of file lists faces.  These lines start with f.
     * Each face is defined as a set of verts, usually three, but may be more.
     * The face definition line contains triplets: three numbers separated by
     * slashes.  Each number is an index into the vert, texture coord, or normal list.
     * NOTE: these lists are indexed starting with 1, not 0.
     */
    public void loadobject(BufferedReader br) {
        String line = "";
        String materialName = ""; // current material
        int materialID = -1;  // -1 means no material found

        // make a default group to start (to hold faces not in any group)
        Group group = new Group("default");
        groups.add(group);

		try {
			while ((line = br.readLine()) != null) {
				// remove extra whitespace
				line = line.trim();
				line = line.replaceAll("  ", " ");
				if (line.length() > 0) {
					if (line.startsWith("v ")) {
						// vertex coord line looks like: v 2.628657 -5.257312 8.090169 [optional W value]
						vertices.add(read3Floats(line));
					}
					else if (line.startsWith("vt")) {
						// texture coord line looks like: vt 0.187254 0.276553 0.000000
						textureCoords.add(read3Floats(line));
					}
					else if (line.startsWith("vn")) {
						// normal line looks like: vn 0.083837 0.962494 -0.258024
						normals.add(read3Floats(line));
					}
					else if (line.startsWith("f ")) {
						// Face line looks like: f 1/3/1 13/20/13 16/29/16
						Face f = readFace(line);
						// assign material ID to polygon
						f.materialID = materialID;
						faces.add(f);           // add to complete face list
						group.faces.add(f);     // add to current group
						group.numTriangles += f.numTriangles(); // track number of triangles in group
					}
                    else if (line.startsWith("g ")) {
                        // Group line looks like: g someGroupName
                        String groupname = (line.length()>1)? line.substring(2).trim() : "";
                        // "select" the given group
                        group = findGroup(groupname);
                        // not found: start new group
                        if (group == null) {
                            group = new Group(groupname);
                            group.materialname = materialName;  // assign current material to new group
                            group.materialID = materialID;
                            groups.add(group);
                        }
                    }
                    else if (line.startsWith("usemtl")) {
                        // material line: usemtl materialName
                        materialName = line.substring(7).trim();
                        // lookup material name in libe
                        materialID = (materialLib == null)? -1 : materialLib.findID(materialName);
                        // assign material to current group
                        group.materialname = materialName;
                        group.materialID = materialID;
                        //System.out.println("got usemtl " +group.name + ".materialname now is " + materialName);
                    }
                    else if (line.startsWith("mtllib")) {
                        // material library line: mtllib materialLibeFile.mtl
                        materialLibeName = line.substring(7).trim();
                        if (materialLibeName.startsWith("./")) {
                            materialLibeName = materialLibeName.substring(2);
                        }
                        // load material library
                        materialLib = new GLMaterialLib(filepath + materialLibeName);
                    }
				}
			}
		} catch (Exception e) {
			System.out.println("GL_OBJ_Reader.loadObject() failed at line: " + line);
		}

        // remove empty groups
        for (int g=groups.size()-1; g >= 0; g--) {
            if (getGroupFaces(g).size() <= 0) {
                //System.out.println("REMOVE EMPTY GROUP " + g);
                groups.remove(g);
            }
        }

		// find min/max points of mesh
		calcDimensions();

		// debug:
		System.out.println("GL_OBJ_Reader: read " + numpolygons()
						+ " faces in " + groups.size() + " groups");
        // debug:
        for (int i=0; i < groups.size(); i++) {
            System.out.println("\tGROUP " + i + " " + ((Group)groups.get(i)).name + " has " + ((Group)groups.get(i)).faces.size() + " faces, material is " + ((Group)groups.get(i)).materialname );
        }
	}

    /**
     * Parse three floats from the given input String.  Ignore the
     * first token (the line type identifier, ie. "v", "vn", "vt").
     * Return array: float[3].
     * @param line  contains line from OBJ file
     * @return array of 3 float values
     */
	private float[] read3Floats(String line)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(line, " ");
			st.nextToken();   // throw out line marker (vn, vt, etc.)
			if (st.countTokens() == 2) { // texture uv may have only 2 values
				return new float[] {Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken()),
									0};
			}
			else {
				return new float[] {Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken())};
			}
		}
		catch (Exception e)
		{
			System.out.println("GL_OBJ_Reader.read3Floats(): error on line '" + line + "', " + e);
			return null;
		}
	}

    /**
     * look for the given groupname in the groups list
     * @param name
     * @return the found Group or null if not found
     */
    public Group findGroup(String name) {
        for (int i=0; i < groups.size(); i++) {
            if (((Group)groups.get(i)).name.equals(name)) {
                return (Group)groups.get(i);
            }
        }
        return null;
    }

	/**
	 * Read a face definition from line and return a Face object.
	 * Face line looks like: f 1/3/1 13/20/13 16/29/16
     * Three or more sets of numbers, each set contains vert/txtr/norm
     * references.  A reference is an index into the vert or txtr
     * or normal list.
	 * @param line   string from OBJ file with face definition
	 * @return       Face object
	 */
	private Face readFace(String line) {
        // throw out "f " at start of line, then split
        String[] triplets = line.substring(2).split(" ");
        int[] v = new int[triplets.length];
        int[] vt = new int[triplets.length];
        int[] vn = new int[triplets.length];
        for (int i = 0; i < triplets.length; i++) {
            // triplets look like  13/20/13  and hold
            // vert/txtr/norm indices.  If no texture coord has been
            // assigned, may be 13//13.  Substitute 0 so split works.
            String[] vertTxtrNorm = triplets[i].replaceAll("//", "/0/").split("/");
            if (vertTxtrNorm.length > 0) {
            	v[i] = convertIndex(vertTxtrNorm[0],vertices.size());
            }
            if (vertTxtrNorm.length > 1) {
                vt[i] = convertIndex(vertTxtrNorm[1],textureCoords.size());
            }
            if (vertTxtrNorm.length > 2) {
                vn[i] = convertIndex(vertTxtrNorm[2],normals.size());
            }
        }
        return  new Face(v,vt,vn);
	}

    /**
     * Convert a vertex reference number into the correct vertex array index.
     * <BR>
     * Face definitions in the OBJ file refer to verts, texture coords and
     * normals using a reference number. The reference numbers is the position
     * of the vert in the vertex list, in the order read from the OBJ file.
     * Reference numbers start at 1, and can be negative (to refer back into
     * the vert list starting at the bottom, though this seems to be rare). The
     * same approach applies to texture coords and normals.
     * <BR>
     * This function converts reference numbers to an array index starting at 0,
     * and converts negative reference numbers to 0-N array indexes.  It returns
     * -1 if the token is blank, meaning there was no data given (ie. there
     * is no texture coord or normal available).
     * <BR>
     * @param token   a token from the OBJ file containing a numeric value or blank
     * @return idx    will be 0 - N index into vert array, or -1 if token is blank
     */
    public int convertIndex(String token, int numVerts) {
        int idx = Integer.valueOf(token).intValue(); // OBJ file index starts at 1
        idx = (idx < 0) ? (numVerts + idx) : idx-1;  // convert index to start at 0
        return idx;
    }

    /**
     *  Find min/max points of mesh.
     */
    public void calcDimensions() {
        float[] vertex;
        // reset min/max points
        leftpoint = rightpoint = 0;
        bottompoint = toppoint = 0;
        farpoint = nearpoint = 0;
        // loop through all groups
        for (int g = 0; g < groups.size(); g++) {
            ArrayList faces = ((Group)groups.get(g)).faces;
            // loop through all faces in group (ie. triangles)
            for (int f = 0; f < faces.size(); f++) {
            	Face face = (Face) faces.get(f);
                int[] vertIDs = face.vertexIDs;
                // loop through all vertices in face
                for (int v = 0; v < vertIDs.length; v++) {
					vertex = (float[]) vertices.get(vertIDs[v]);
					if (vertex[0] > rightpoint)  rightpoint = vertex[0];
					if (vertex[0] < leftpoint)   leftpoint = vertex[0];
					if (vertex[1] > toppoint)    toppoint = vertex[1];
					if (vertex[1] < bottompoint) bottompoint = vertex[1];
					if (vertex[2] > nearpoint) 	 nearpoint = vertex[2];
					if (vertex[2] < farpoint)    farpoint = vertex[2];
				}
    	    }
        }
    }

    public float getXWidth() {
        return rightpoint - leftpoint;
    }

    public float getYHeight() {
        return toppoint - bottompoint;
    }

    public float getZDepth() {
        return nearpoint - farpoint;
    }

    public int numpolygons() {
        int number = 0;
        for (int i = 0; i < groups.size(); i++) {
            number += ((Group)groups.get(i)).faces.size();
        }
        return number;
    }

    //========================================================================
    // These functions get group information without having to expose the
    // Group class to the outside world.
    //========================================================================

    public int numGroups() {
        return groups.size();
    }
    public String getGroupName(int g) {
        return ((Group)groups.get(g)).name;
    }
    public ArrayList getGroupFaces(int g) {
        return ((Group)groups.get(g)).faces;
    }
    public String getGroupMaterialName(int g) {
        return ((Group)groups.get(g)).materialname;
    }
    public int getGroupTriangleCount(int g) {
        return ((Group)groups.get(g)).numTriangles;
    }

    //========================================================================
    // Group class holds one group of faces with a name and material
    //========================================================================

    class Group {
        String name;
        String materialname;
	int materialID;  // index into materials array
        int numTriangles;
        ArrayList faces;

        public void Group_(ArrayList faces, String name, String materialname) {
            this.name = name;
            this.materialname = materialname;
            this.faces = faces;
        }

        public Group(String name) {
            this.name = name;
            this.materialname = "";
            this.faces = new ArrayList();
        }
    }
}



