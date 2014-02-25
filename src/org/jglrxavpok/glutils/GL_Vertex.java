
package org.jglrxavpok.glutils;

import java.util.ArrayList;

/**
 * Vertex contains an xyz position, and a list of neighboring
 * triangles.  Normals and texture coordinates are stored in the
 * GL_Triangle object, since each face may have different normals and
 * texture coords for the same vertex.
 *
 * The "neighbor" triangle list holds all triangles that contains
 * this vertex.  Not to be confused with the GL_Triangle.neighborsP1,
 * GL_Triangle.neighborsP2, etc. that contain only neighbors that should
 * be smoothed into the given triangle.
 *
 * jun 2006: added makeClone()
 */
public class GL_Vertex
{
    public GL_Vector pos = new GL_Vector();  // xyz coordinate of vertex
    public GL_Vector posS = new GL_Vector(); // xyz Screen coords of projected vertex
    public int ID;      // index into parent objects vertexData vector
    public ArrayList neighborTris = new ArrayList(); // Neighbor triangles of this vertex


    public GL_Vertex() {
        pos = new GL_Vector(0f, 0f, 0f);
    }

    public GL_Vertex(float xpos, float ypos, float zpos) {
        pos = new GL_Vector(xpos, ypos, zpos);
    }

    public GL_Vertex(float xpos, float ypos, float zpos, float u, float v) {
        pos = new GL_Vector(xpos, ypos, zpos);
    }

    public GL_Vertex(GL_Vector ppos) {
        pos = ppos.getClone();
    }

    /**
     * add a neighbor triangle to this vertex
     */
    void addNeighborTri(GL_Triangle triangle)
    {
        if (!neighborTris.contains(triangle)) {
        	neighborTris.add(triangle);
        }
    }

    /**
     * clear the neighbor triangle list
     */
    void resetNeighbors()
    {
    	neighborTris.clear();
    }


    public GL_Vertex makeClone() {
        GL_Vertex newVertex = new GL_Vertex();
        newVertex.pos = pos.getClone();
        newVertex.posS = posS.getClone();
        newVertex.ID = ID;
        return newVertex;
    }


    public String toString() {
        return new String("<vertex  x=" + pos.x + " y=" + pos.y + " z=" + pos.z + ">\r\n");
    }

}