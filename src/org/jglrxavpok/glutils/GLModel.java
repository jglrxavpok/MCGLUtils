package org.jglrxavpok.glutils;

import org.lwjgl.opengl.GL11;

/**
 * This class loads and renders a mesh from an OBJ file format. The mesh can
 * have multiple materials, including texture images.
 * 
 * Uses GL_Mesh to load a .obj file and GLMaterialLIb to load the .mtl file. It
 * assumes the .obj .mtl and any texture images are present in the same folder.
 * 
 * Also has a function, renderTextured() to draw a mesh with no groups or
 * materials. The entire mesh will be drawn as one group of triangles with one
 * texture.
 */
public class GLModel extends Model
{
	// a default material to use if none is specified
	public static GLMaterial defaultMtl = new GLMaterial();

	public GL_Mesh mesh; // holds vertex, triangle and group information
	public int displayListID;

	// !!!! this will be null. NEED to check this in OBJ_Reader and check
	// renderGroups() (BROKEN!!!)
	public GLMaterial[] groupMaterials; // holds material for each face group (1

	private String id;
										// group to 1 material)

	public GLModel(String filename)
	{
		// load OBJ file
		mesh = loadMesh(filename);
	}
	
	public void renderGroups(String group)
	{
	    renderGroup(group);
	}

	/**
	 * read the given .obj file into a GL_Mesh object
	 * 
	 * @param filename
	 *            (must end in .obj)
	 * @return the loaded GL_Mesh
	 */
	public GL_Mesh loadMesh(String filename)
	{
		if (filename.toUpperCase().endsWith(".OBJ"))
		{
			GL_OBJ_Importer importer = new GL_OBJ_Importer();
			mesh = importer.load(filename);
		} else
		{
			GL_3DS_Importer importer = new GL_3DS_Importer();
			mesh = importer.load(filename);
			System.out
					.println("GLMeshRenderer.loadMesh(): WARNING 3DS files functionality is limited");
		}
		return mesh;
	}

	/**
	 * Render mesh into a displaylist and store the listID in the flowercenter
	 * object.
	 * 
	 * @param PR
	 *            PetalRing to draw
	 */
	public void makeDisplayList()
	{
		// call the displaylist
		if (displayListID == 0)
		{
			displayListID = GL11.glGenLists(1); // allocate a display list
			GL11.glNewList(displayListID, GL11.GL_COMPILE); // Start the list
			render(mesh); // render the mesh
			GL11.glEndList(); // done
		}
	}

	/**
	 * return the display list ID created by makeDisplayList()
	 */
	public int getDisplayListID()
	{
		return displayListID;
	}

	/**
	 * recalculate normals on the mesh object
	 * 
	 * @see GL_Mesh.regenerateNormals()
	 * @see GL_Mesh.setSmoothingAngle()
	 */
	public void regenerateNormals()
	{
		if (mesh != null)
		{
			mesh.regenerateNormals();
		}
	}

	/**
	 * Draw the model. Calls the displaylist if one is created, or calls
	 * renderGroups()
	 */
	public void render()
	{
		if (displayListID == 0)
		{
			render(mesh);
		} else
		{
			GL11.glCallList(displayListID);
		}
	}

	/**
	 * Draw one group from the mesh. This will activate the correct material for
	 * the group (including textures).
	 * 
	 * @param groupName
	 *            name of group (from obj file)
	 */
	public void renderGroup(String groupName)
	{
		int GID = -1; // group id

		// find group by name
		for (int g = 0; g < mesh.numGroups(); g++)
		{
			if (mesh.getGroupName(g).equals(groupName))
			{
				GID = g;
				break;
			}
		}
		if (GID == -1)
		{
			return;
		}

		// draw the triangles in this group
		GLMaterial[] materials = mesh.materials; // loaded from the .mtl file
		GL_Triangle[] triangles = mesh.getGroupFaces(GID); // each group may
															// have a material
		GLMaterial mtl;
		GL_Triangle t;
		int currMtl = -1;
		int i = 0;

		// draw all triangles in object
		for (i = 0; i < triangles.length;)
		{
			t = triangles[i];

			// activate new material and texture
			currMtl = t.materialID;
			mtl = (materials != null && materials.length > 0 && currMtl >= 0) ? materials[currMtl]
					: defaultMtl;
			mtl.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mtl.textureHandle);

			// draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			for (; i < triangles.length && (t = triangles[i]) != null
					&& currMtl == t.materialID; i++)
			{
				GL11.glTexCoord2f(t.uvw1.x, t.uvw1.y);
				GL11.glNormal3f(t.norm1.x, t.norm1.y, t.norm1.z);
				GL11.glVertex3f((float) t.p1.pos.x, (float) t.p1.pos.y,
						(float) t.p1.pos.z);

				GL11.glTexCoord2f(t.uvw2.x, t.uvw2.y);
				GL11.glNormal3f(t.norm2.x, t.norm2.y, t.norm2.z);
				GL11.glVertex3f((float) t.p2.pos.x, (float) t.p2.pos.y,
						(float) t.p2.pos.z);

				GL11.glTexCoord2f(t.uvw3.x, t.uvw3.y);
				GL11.glNormal3f(t.norm3.x, t.norm3.y, t.norm3.z);
				GL11.glVertex3f((float) t.p3.pos.x, (float) t.p3.pos.y,
						(float) t.p3.pos.z);
			}
			GL11.glEnd();
		}
	}

	/**
	 * This is a simple way to render a mesh with no materials. Draws the mesh
	 * with normals and texture coordinates. Loops through all triangles in the
	 * mesh object (ignores groups and materials).
	 * 
	 * @param o
	 *            mesh object to render
	 */
	public void renderTextured(int textureHandle)
	{
		GL_Triangle t;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
		GL11.glBegin(GL11.GL_TRIANGLES);
		for (int j = 0; j < mesh.triangles.length; j++)
		{ // draw all triangles in object
			t = mesh.triangles[j];

			GL11.glTexCoord2f(t.uvw1.x, t.uvw1.y);
			GL11.glNormal3f(t.norm1.x, t.norm1.y, t.norm1.z);
			GL11.glVertex3f((float) t.p1.pos.x, (float) t.p1.pos.y,
					(float) t.p1.pos.z);

			GL11.glTexCoord2f(t.uvw2.x, t.uvw2.y);
			GL11.glNormal3f(t.norm2.x, t.norm2.y, t.norm2.z);
			GL11.glVertex3f((float) t.p2.pos.x, (float) t.p2.pos.y,
					(float) t.p2.pos.z);

			GL11.glTexCoord2f(t.uvw3.x, t.uvw3.y);
			GL11.glNormal3f(t.norm3.x, t.norm3.y, t.norm3.z);
			GL11.glVertex3f((float) t.p3.pos.x, (float) t.p3.pos.y,
					(float) t.p3.pos.z);
		}
		GL11.glEnd();
	}

	/**
	 * Render a mesh with materials. If no materials exist (none are defined in
	 * the mesh, or the materials file was not found), then a default material
	 * will be applied and texture 0 will be activated (see GLMaterial.java for
	 * the default material settings).
	 */
	public void render(GL_Mesh m)
	{
		GLMaterial[] materials = m.materials; // loaded from the .mtl file
		GLMaterial mtl;
		GL_Triangle t;
		int currMtl = -1;
		int i = 0;

		// draw all triangles in object
		for (i = 0; i < m.triangles.length;)
		{
			t = m.triangles[i];

			// activate new material and texture
			currMtl = t.materialID;
			mtl = (materials != null && materials.length > 0 && currMtl >= 0) ? materials[currMtl]
					: defaultMtl;
			mtl.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, mtl.textureHandle);

			// draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			for (; i < m.triangles.length && (t = m.triangles[i]) != null
					&& currMtl == t.materialID; i++)
			{
				GL11.glTexCoord2f(t.uvw1.x, t.uvw1.y);
				GL11.glNormal3f(t.norm1.x, t.norm1.y, t.norm1.z);
				GL11.glVertex3f((float) t.p1.pos.x, (float) t.p1.pos.y,
						(float) t.p1.pos.z);

				GL11.glTexCoord2f(t.uvw2.x, t.uvw2.y);
				GL11.glNormal3f(t.norm2.x, t.norm2.y, t.norm2.z);
				GL11.glVertex3f((float) t.p2.pos.x, (float) t.p2.pos.y,
						(float) t.p2.pos.z);

				GL11.glTexCoord2f(t.uvw3.x, t.uvw3.y);
				GL11.glNormal3f(t.norm3.x, t.norm3.y, t.norm3.z);
				GL11.glVertex3f((float) t.p3.pos.x, (float) t.p3.pos.y,
						(float) t.p3.pos.z);
			}
			GL11.glEnd();
		}
	}

	public void renderMeshNormals()
	{
		GL_Triangle t;
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(0, 1, 0);
		GL11.glBegin(GL11.GL_LINES);
		{
			for (int j = 0; j < mesh.triangles.length; j++)
			{ // draw all triangles in object
				t = mesh.triangles[j];
				t.norm1.normalize();
				t.norm2.normalize();
				t.norm3.normalize();

				GL11.glVertex3f((float) t.p1.pos.x, (float) t.p1.pos.y,
						(float) t.p1.pos.z);
				GL11.glVertex3f((float) (t.p1.pos.x + t.norm1.x),
						(float) (t.p1.pos.y + t.norm1.y),
						(float) (t.p1.pos.z + t.norm1.z));

				GL11.glVertex3f((float) t.p2.pos.x, (float) t.p2.pos.y,
						(float) t.p2.pos.z);
				GL11.glVertex3f((float) (t.p2.pos.x + t.norm2.x),
						(float) (t.p2.pos.y + t.norm2.y),
						(float) (t.p2.pos.z + t.norm2.z));

				GL11.glVertex3f((float) t.p3.pos.x, (float) t.p3.pos.y,
						(float) t.p3.pos.z);
				GL11.glVertex3f((float) (t.p3.pos.x + t.norm3.x),
						(float) (t.p3.pos.y + t.norm3.y),
						(float) (t.p3.pos.z + t.norm3.z));
			}
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

}
