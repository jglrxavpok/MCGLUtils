package org.jglrxavpok.glutils;

import java.io.*;
import java.util.ArrayList;

/*
 * Read an OBJ file and load it into a GL_Mesh.  GL_Mesh is a basic
 * mesh object that holds vertex and triangle data.  The GL_OBJ_Reader
 * loads data pretty much as it is in the OBJ, and this Importer converts
 * the idiosyncracies of the OBJ format into a straightforward vert/triangle
 * structure.
 *
 * NOTE: OBJ files can contain polygon faces. The GL_Mesh holds only
 * triangles, so all faces will be converted to triangles.
 *
 * jun 2006: in makeMeshObject() store uv coord with each vertex.
 * jul 2006: preserve face groups from the obj.
 */
public class GL_OBJ_Importer
{
	private GL_OBJ_Reader reader = null;
	private GL_Mesh mesh = null;

	public GL_OBJ_Importer()
	{
	}

	public GL_Mesh load(String filename)
	{
		System.out.println("GL_OBJ_Importer.import(): Load object from OBJ "
				+ filename);
		reader = new GL_OBJ_Reader(filename);
		System.out.println("GL_OBJ_Importer.importFromStream(): model has "
				+ reader.faces.size() + " faces and " + reader.vertices.size()
				+ " vertices.  Mtl file is " + reader.materialLibeName);
		return makeMeshObject(reader);
	}

	public GL_Mesh importFromStream(InputStream inStream)
	{
		System.out
				.println("GL_OBJ_Importer.importFromStream(): Load object from OBJ...");
		reader = new GL_OBJ_Reader(inStream);
		System.out.println("GL_OBJ_Importer.importFromStream(): model has "
				+ reader.faces.size() + " faces and " + reader.vertices.size()
				+ " vertices.  Mtl file is " + reader.materialLibeName);
		return makeMeshObject(reader);
	}

	/**
	 * create a GL_Mesh (mesh object) from the data read by a GL_OBJ_Reader
	 */
	public GL_Mesh makeMeshObject(GL_OBJ_Reader objData)
	{
		ArrayList verts = objData.vertices;
		ArrayList txtrs = objData.textureCoords;
		ArrayList norms = objData.normals;
		ArrayList faces = objData.faces;

		// make a new mesh
		mesh = new GL_Mesh();
		mesh.name = objData.filename;
		mesh.materialLibeName = objData.materialLibeName;
		mesh.materials = (objData.materialLib != null) ? objData.materialLib.materials
				: null;

		// add verts to GL_Mesh
		for (int i = 0; i < verts.size(); i++)
		{
			float[] coords = (float[]) verts.get(i);
			mesh.addVertex(coords[0], coords[1], coords[2]);
		}

		// allocate space for groups
		mesh.makeGroups(objData.numGroups());

		// init each group (allocate space for triangles)
		for (int g = 0; g < objData.numGroups(); g++)
		{
			mesh.initGroup(g, objData.getGroupName(g),
					objData.getGroupMaterialName(g),
					objData.getGroupTriangleCount(g));
		}

		// add triangles to GL_Mesh. OBJ "face" may be a triangle,
		// quad or polygon. Convert all faces to triangles.
		for (int g = 0; g < objData.numGroups(); g++)
		{
			int triCount = 0;
			faces = objData.getGroupFaces(g);
			for (int i = 0; i < faces.size(); i++)
			{
				Face face = (Face) faces.get(i);
				// put verts, normals, texture coords into triangle(s)
				if (face.vertexIDs.length == 3)
				{
					addTriangle(mesh, g, triCount, face, txtrs, norms, 0, 1, 2,
							face.materialID);
					triCount++;
				} else if (face.vertexIDs.length == 4)
				{
					// convert quad to two triangles
					addTriangle(mesh, g, triCount, face, txtrs, norms, 0, 1, 2,
							face.materialID);
					triCount++;
					addTriangle(mesh, g, triCount, face, txtrs, norms, 0, 2, 3,
							face.materialID);
					triCount++;
				} else
				{
					// convert polygon to triangle fan, with first vertex (0)
					// at center: 0,1,2 0,2,3 0,3,4 0,4,5
					for (int n = 0; n < face.vertexIDs.length - 2; n++)
					{
						addTriangle(mesh, g, triCount, face, txtrs, norms, 0,
								n + 1, n + 2, face.materialID);
						triCount++;
					}
				}
			}
		}

		// optimize the GL_Mesh
		mesh.rebuild();

		// if no normals were loaded, generate some
		if (norms.size() == 0)
		{
			mesh.regenerateNormals();
		}

		mesh.finalize();
		return mesh;
	}

	/**
	 * Add a new triangle to the GL_Mesh. This assumes that the vertices have
	 * already been added to the GL_Mesh, in the same order that they were in
	 * the OBJ. Also the mesh has groups allocated with triangle arrays.
	 * 
	 * @param obj
	 *            GL_Mesh
	 * @param groupNum
	 *            the group to add the triangles to
	 * @param triNum
	 *            the index of the triangle in the group
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
	public GL_Triangle addTriangle(GL_Mesh obj, int groupNum, int triNum,
			Face face, ArrayList txtrs, ArrayList norms, int v1, int v2,
			int v3, int mtlID)
	{
		// An OBJ face may have many vertices (can be a polygon).
		// Make a new triangle with the specified three verts.
		GL_Triangle t = new GL_Triangle(obj.vertex(face.vertexIDs[v1]),
				obj.vertex(face.vertexIDs[v2]), obj.vertex(face.vertexIDs[v3]));

		// put texture coords into triangle
		if (txtrs.size() > 0 && face.textureIDs.length > 1)
		{ // if texture coords were loaded
			float[] uvw;
			if(face.textureIDs[v1] >= 0)
			{
				uvw = (float[]) txtrs.get(face.textureIDs[v1]); // txtr coord for
				t.uvw1 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
			}
			if(face.textureIDs[v2] >= 0)
			{
				uvw = (float[]) txtrs.get(face.textureIDs[v2]); // txtr coord for
				t.uvw2 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
			}

			if(face.textureIDs[v3] >= 0)
			{
				uvw = (float[]) txtrs.get(face.textureIDs[v3]); // txtr coord for
				t.uvw3 = new GL_Vector(uvw[0], uvw[1], uvw[2]);
			}

		}

		// put normals into triangle (NOTE: normalID can be -1!!! could barf
		// here!!!)
		if (norms.size() > 0)
		{ // if normals were loaded
			float[] norm;
			norm = (float[]) norms.get(face.normalIDs[v1]); // normal for vert 1
			t.norm1 = new GL_Vector(norm[0], norm[1], norm[2]);
			norm = (float[]) norms.get(face.normalIDs[v2]); // normal for vert 2
			t.norm2 = new GL_Vector(norm[0], norm[1], norm[2]);
			norm = (float[]) norms.get(face.normalIDs[v3]); // normal for vert 3
			t.norm3 = new GL_Vector(norm[0], norm[1], norm[2]);
		}

		// store material number in triangle
		t.materialID = mtlID;

		// add triangle to given group in GL_Mesh
		obj.addTriangle(t, groupNum, triNum);

		return t;
	}
}