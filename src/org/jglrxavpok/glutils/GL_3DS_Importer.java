package org.jglrxavpok.glutils;

import java.io.*;
import java.util.ArrayList;

/**
 * Import a 3DS file into a GL_Model object.
 */
public class GL_3DS_Importer
{
	// Reads the 3DS files into vertex and face lists
	private GL_3DS_Reader reader = new GL_3DS_Reader();

	// Mesh object will hold the data in a renderable form
	private GL_Mesh mesh = new GL_Mesh();

	public GL_3DS_Importer()
	{
	}

	public GL_Mesh load(String filename)
	{
		System.out.println("GL_3DS_Importer.import(): Load object from "
				+ filename);
		reader = new GL_3DS_Reader(filename);
		System.out.println("GL_3DS_Importer.importFromStream(): model has "
				+ reader.faces.size() + " faces and " + reader.vertices.size()
				+ " vertices. ");
		return makeMeshObject(reader.vertices, reader.textureCoords,
				reader.normals, reader.faces);
	}

	/**
	 * Load the 3DS file and store into a mesh.
	 */
	public GL_Mesh importFromStream(InputStream inStream)
	{
		System.out.println("importFromStream(): Load object from stream...");
		reader.load3DSFromStream(inStream);
		System.out.println("importFromStream(): model has "
				+ reader.faces.size() + " faces and " + reader.vertices.size()
				+ " vertices and " + reader.textureCoords.size()
				+ " txtrcoords.");
		return makeMeshObject(reader.vertices, reader.textureCoords,
				reader.normals, reader.faces);
	}

	/**
	 * create a GL_Object (mesh object) from the data read by a 3DS_Reader
	 * 
	 * @param verts
	 *            ArrayList of vertices
	 * @param txtrs
	 *            ArrayList of texture coordinates
	 * @param norms
	 *            ArrayList of normal
	 * @param faces
	 *            ArrayList of Face objects (triangles)
	 * @return
	 */
	public GL_Mesh makeMeshObject(ArrayList verts, ArrayList txtrs,
			ArrayList norms, ArrayList faces)
	{
		mesh = new GL_Mesh(); // mesh object
		mesh.name = "3DS";

		// add verts to GL_object
		for (int i = 0; i < verts.size(); i++)
		{
			float[] coords = (float[]) verts.get(i);
			mesh.addVertex(coords[0], coords[1], coords[2]);
		}

		// add triangles to GL_object. 3DS "face" is always a triangle.
		for (int i = 0; i < faces.size(); i++)
		{
			Face face = (Face) faces.get(i);
			// put verts, normals, texture coords into triangle
			addTriangle(mesh, face, txtrs, norms);
		}

		// optimize the GL_Object and generate normals
		mesh.rebuild();

		// if no normals were loaded, generate some
		if (norms.size() == 0)
		{
			mesh.regenerateNormals();
		}

		return mesh;
	}

	/**
	 * Add a new triangle to the GL_Object. This assumes that the vertices have
	 * already been added to the GL_Object, in the same order that they were in
	 * the 3DS.
	 * 
	 * @param obj
	 *            GL_Object
	 * @param face
	 *            a face from the OBJ file
	 * @param txtrs
	 *            ArrayList of texture coords from the OBJ file
	 * @param norms
	 *            ArrayList of normals from the OBJ file
	 * @param v1
	 *            vertices to use for the triangle (face may have >3 verts)
	 * @param v2
	 * @param v3
	 * @return
	 */
	public GL_Triangle addTriangle(GL_Mesh obj, Face face, ArrayList txtrs,
			ArrayList norms)
	{
		// An OBJ face may have many vertices (can be a polygon).
		// Make a new triangle with the specified three verts.
		GL_Triangle t = new GL_Triangle(obj.vertex(face.vertexIDs[0]),
				obj.vertex(face.vertexIDs[1]), obj.vertex(face.vertexIDs[2]));

		// put texture coords into triangle
		if (txtrs.size() > 0)
		{ // if texture coords were loaded
			float[] uvw;
			uvw = (float[]) txtrs.get(face.textureIDs[0]); // txtr coord for
															// vert 1
			t.uvw1 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
			uvw = (float[]) txtrs.get(face.textureIDs[1]); // txtr coord for
															// vert 2
			t.uvw2 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
			uvw = (float[]) txtrs.get(face.textureIDs[2]); // txtr coord for
															// vert 3
			t.uvw3 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
		}

		// put normals into triangle (NOTE: normalID can be -1!!! could barf
		// here!!!)
		if (norms.size() > 0)
		{ // if normals were loaded
			float[] norm;
			norm = (float[]) norms.get(face.normalIDs[0]); // normal for vert 1
			t.norm1 = new GL_Vector(norm[0], norm[1], norm[2]);
			norm = (float[]) norms.get(face.normalIDs[1]); // normal for vert 2
			t.norm2 = new GL_Vector(norm[0], norm[1], norm[2]);
			norm = (float[]) norms.get(face.normalIDs[2]); // normal for vert 3
			t.norm3 = new GL_Vector(norm[0], norm[1], norm[2]);
		}

		// add triangle to GL_object
		mesh.addTriangle(t);

		return t;
	}
}