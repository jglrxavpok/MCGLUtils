package org.jglrxavpok.glutils;

import java.nio.FloatBuffer;

/**
 * Define a 4x4 matrix, and provide functions to create common
 * matrices for 3D operations, such as rotate, scale and translate.
 */
public class GL_Matrix
{
	// vars hold a 4x4 matrix, defaults to identity matrix.
	public float m00=1, m01=0, m02=0, m03=0;
	public float m10=0, m11=1, m12=0, m13=0;
	public float m20=0, m21=0, m22=1, m23=0;
	public float m30=0, m31=0, m32=0, m33=1;
	
	/**
	 * Default to the identity matrix
	 */
	public GL_Matrix()
	{
	}
	
	/**
	 * Create a matrix with the three given axes
	 */
	public GL_Matrix(GL_Vector right, GL_Vector up, GL_Vector forward)
	{
		m00 = right.x;
		m10 = right.y;
		m20 = right.z;
		m01 = up.x;
		m11 = up.y;
		m21 = up.z;
		m02 = forward.x;
		m12 = forward.y;
		m22 = forward.z;
	}
	
	/**
	 * copy a two dimensional float array into this Matrix
	 */
	public void importFromArray(float[][] data)
	{
		// insure that the array is 4x4
		if (data.length<4) return;
		for (int i=0;i<4;i++) if (data[i].length<4) return;
		
		m00=data[0][0];  m01=data[0][1];  m02=data[0][2];  m03=data[0][3];
		m10=data[1][0];  m11=data[1][1];  m12=data[1][2];  m13=data[1][3];
		m20=data[2][0];  m21=data[2][1];  m22=data[2][2];  m23=data[2][3];
		m30=data[3][0];  m31=data[3][1];  m32=data[3][2];  m33=data[3][3];
	}
	
	/**
	 * return a two dimensional float array containing this Matrix
	 */
	public float[][] exportToArray()
	{
		float data[][] = new float[4][4];
		data[0][0]=m00;  data[0][1]=m01;  data[0][2]=m02;  data[0][3]=m03;
		data[1][0]=m10;  data[1][1]=m11;  data[1][2]=m12;  data[1][3]=m13;
		data[2][0]=m20;  data[2][1]=m21;  data[2][2]=m22;  data[2][3]=m23;
		data[3][0]=m30;  data[3][1]=m31;  data[3][2]=m32;  data[3][3]=m33;
		return data;
	}
	
	///////////////////////////////////////////////////////////
	// Factory Methods
	///////////////////////////////////////////////////////////
	
	/**
	 * create a Matrix shifted by the given amounts
	 */
	public static GL_Matrix translateMatrix(float dx, float dy, float dz)
	{
		GL_Matrix m = new GL_Matrix();
		m.m03=dx;
		m.m13=dy;
		m.m23=dz;
		return m;
	}
	
	/**
	 * create a Matrix to change scale
	 */
	public static GL_Matrix scaleMatrix(float dx, float dy, float dz)
	{
		GL_Matrix m=new GL_Matrix();
		m.m00=dx;
		m.m11=dy;
		m.m22=dz;
		return m;
	}
	
	/**
	 * create a Matrix to scale all axes equally
	 */
	public static GL_Matrix scaleMatrix(float d)
	{
		return GL_Matrix.scaleMatrix(d,d,d);
	}
	
	/**
	 * create a rotation matrix
	 */
	public static GL_Matrix rotateMatrix(float dx, float dy, float dz)
	{
		GL_Matrix out=new GL_Matrix();
		float SIN;
		float COS;
		
		if (dx!=0)
		{
			GL_Matrix m =new GL_Matrix();
			SIN = (float)Math.sin(dx);
			COS = (float)Math.cos(dx);
			m.m11=COS;
			m.m12=SIN;
			m.m21=-SIN;
			m.m22=COS;
			out.transform(m);
		}
		if (dy!=0)
		{
			GL_Matrix m =new GL_Matrix();
			SIN = (float)Math.sin(dy);
			COS = (float)Math.cos(dy);
			m.m00=COS;
			m.m02=SIN;
			m.m20=-SIN;
			m.m22=COS;
			out.transform(m);
		}
		if (dz!=0)
		{
			GL_Matrix m =new GL_Matrix();
			SIN = (float)Math.sin(dz);
			COS = (float)Math.cos(dz);
			m.m00=COS;
			m.m01=SIN;
			m.m10=-SIN;
			m.m11=COS;
			out.transform(m);
		}
		return out;
	}
	
	///////////////////////////////////////////////////////////
	// Public Methods
	///////////////////////////////////////////////////////////
	
	public void translate(float dx, float dy, float dz)
	{
		transform(translateMatrix(dx,dy,dz));
	}
	
	public void scale(float dx, float dy, float dz)
	{
		transform(scaleMatrix(dx,dy,dz));
	}
	
	public void scale(float d)
	{
		transform(scaleMatrix(d));
	}
	
	public void rotate(float dx, float dy, float dz)
	{
		transform(rotateMatrix(dx,dy,dz));
	}
	
	public void scaleSelf(float dx, float dy, float dz)
	{
		preTransform(scaleMatrix(dx,dy,dz));
	}
	
	public void scaleSelf(float d)
	{
		preTransform(scaleMatrix(d));
	}
	
	public void rotateSelf(float dx, float dy, float dz)
	{
		preTransform(rotateMatrix(dx,dy,dz));
	}
	
	/**
	 * reset to the identity matrix
	 */
	public void reset()
	{
		m00=1; m01=0; m02=0; m03=0;
		m10=0; m11=1; m12=0; m13=0;
		m20=0; m21=0; m22=1; m23=0;
		m30=0; m31=0; m32=0; m33=1;
	}
	
	/**
	 *  Transform the given vector using this matrix. Return the
	 *  transformed vector (the original vector is not modified).
	 *
	 *  @param v   GL_Vector to be transformed
	 *  @return    the transformed GL_Vector
	 */
	public GL_Vector transform(GL_Vector v)
	{
		if (v != null) {
			float newx, newy, newz;
			newx = v.x*m00 + v.y*m01 + v.z*m02+ m03;
			newy = v.x*m10 + v.y*m11 + v.z*m12+ m13;
			newz = v.x*m20 + v.y*m21 + v.z*m22+ m23;
			return new GL_Vector(newx,newy,newz);
		}
		return null;
	}
	
	/**
	 * transforms this matrix by matrix n from left (this=n x this)
	 */
	public void transform(GL_Matrix n)
	{
		GL_Matrix m = this.getClone();
		
		m00 = n.m00*m.m00 + n.m01*m.m10 + n.m02*m.m20;
		m01 = n.m00*m.m01 + n.m01*m.m11 + n.m02*m.m21;
		m02 = n.m00*m.m02 + n.m01*m.m12 + n.m02*m.m22;
		m03 = n.m00*m.m03 + n.m01*m.m13 + n.m02*m.m23 + n.m03;
		m10 = n.m10*m.m00 + n.m11*m.m10 + n.m12*m.m20;
		m11 = n.m10*m.m01 + n.m11*m.m11 + n.m12*m.m21;
		m12 = n.m10*m.m02 + n.m11*m.m12 + n.m12*m.m22;
		m13 = n.m10*m.m03 + n.m11*m.m13 + n.m12*m.m23 + n.m13;
		m20 = n.m20*m.m00 + n.m21*m.m10 + n.m22*m.m20;
		m21 = n.m20*m.m01 + n.m21*m.m11 + n.m22*m.m21;
		m22 = n.m20*m.m02 + n.m21*m.m12 + n.m22*m.m22;
		m23 = n.m20*m.m03 + n.m21*m.m13 + n.m22*m.m23 + n.m23;
	}
	
	/**
	 * transforms this matrix by matrix n from right (this=this x n)
	 */
	public void preTransform(GL_Matrix n)
	{
		GL_Matrix m=this.getClone();
		
		m00 = m.m00*n.m00 + m.m01*n.m10 + m.m02*n.m20;
		m01 = m.m00*n.m01 + m.m01*n.m11 + m.m02*n.m21;
		m02 = m.m00*n.m02 + m.m01*n.m12 + m.m02*n.m22;
		m03 = m.m00*n.m03 + m.m01*n.m13 + m.m02*n.m23 + m.m03;
		m10 = m.m10*n.m00 + m.m11*n.m10 + m.m12*n.m20;
		m11 = m.m10*n.m01 + m.m11*n.m11 + m.m12*n.m21;
		m12 = m.m10*n.m02 + m.m11*n.m12 + m.m12*n.m22;
		m13 = m.m10*n.m03 + m.m11*n.m13 + m.m12*n.m23 + m.m13;
		m20 = m.m20*n.m00 + m.m21*n.m10 + m.m22*n.m20;
		m21 = m.m20*n.m01 + m.m21*n.m11 + m.m22*n.m21;
		m22 = m.m20*n.m02 + m.m21*n.m12 + m.m22*n.m22;
		m23 = m.m20*n.m03 + m.m21*n.m13 + m.m22*n.m23 + m.m23;
	}
	
	/**
	 * Multiply the two matrices.  Return m1 x m2
	 */
	public static GL_Matrix multiply(GL_Matrix m1, GL_Matrix m2)
	{
		GL_Matrix m = new GL_Matrix();
		
		m.m00 = m1.m00*m2.m00 + m1.m01*m2.m10 + m1.m02*m2.m20;
		m.m01 = m1.m00*m2.m01 + m1.m01*m2.m11 + m1.m02*m2.m21;
		m.m02 = m1.m00*m2.m02 + m1.m01*m2.m12 + m1.m02*m2.m22;
		m.m03 = m1.m00*m2.m03 + m1.m01*m2.m13 + m1.m02*m2.m23 + m1.m03;
		m.m10 = m1.m10*m2.m00 + m1.m11*m2.m10 + m1.m12*m2.m20;
		m.m11 = m1.m10*m2.m01 + m1.m11*m2.m11 + m1.m12*m2.m21;
		m.m12 = m1.m10*m2.m02 + m1.m11*m2.m12 + m1.m12*m2.m22;
		m.m13 = m1.m10*m2.m03 + m1.m11*m2.m13 + m1.m12*m2.m23 + m1.m13;
		m.m20 = m1.m20*m2.m00 + m1.m21*m2.m10 + m1.m22*m2.m20;
		m.m21 = m1.m20*m2.m01 + m1.m21*m2.m11 + m1.m22*m2.m21;
		m.m22 = m1.m20*m2.m02 + m1.m21*m2.m12 + m1.m22*m2.m22;
		m.m23 = m1.m20*m2.m03 + m1.m21*m2.m13 + m1.m22*m2.m23 + m1.m23;
		return m;
	}
	
	/**
	 * return a string representation of this matrix
	 */
	public String toString()
	{
		StringBuffer out=new StringBuffer("<Matrix: \r\n");
		out.append(m00+","+m01+","+m02+","+m03+",\r\n");
		out.append(m10+","+m11+","+m12+","+m13+",\r\n");
		out.append(m20+","+m21+","+m22+","+m23+",\r\n");
		out.append(m30+","+m31+","+m32+","+m33+">\r\n");
		return out.toString();
	}
	
	/**
	 * return a copy of this matrix
	 */
	public GL_Matrix getClone()
	{
		GL_Matrix m=new GL_Matrix();
		m.m00=m00;  m.m01=m01;  m.m02=m02;  m.m03=m03;
		m.m10=m10;  m.m11=m11;  m.m12=m12;  m.m13=m13;
		m.m20=m20;  m.m21=m21;  m.m22=m22;  m.m23=m23;
		m.m30=m30;  m.m31=m31;  m.m32=m32;  m.m33=m33;
		return m;
	}
	
	/**
	 * return the inverse of this matrix
	 */
	public GL_Matrix inverse()
	{
		GL_Matrix m = new GL_Matrix();
		
		float q1 = m12;  float q6 = m10*m01;  float q7 = m10*m21;  float q8 = m02;
		float q13 = m20*m01;  float q14 = m20*m11;  float q21 = m02*m21;  float q22 = m03*m21;
		float q25 = m01*m12;  float q26 = m01*m13;  float q27 = m02*m11;  float q28 = m03*m11;
		float q29 = m10*m22;  float q30 = m10*m23;  float q31 = m20*m12;  float q32 = m20*m13;
		float q35 = m00*m22;  float q36 = m00*m23;  float q37 = m20*m02;  float q38 = m20*m03;
		float q41 = m00*m12;  float q42 = m00*m13;  float q43 = m10*m02;  float q44 = m10*m03;
		float q45 = m00*m11;  float q48 = m00*m21;
		float q49 = q45*m22-q48*q1-q6*m22+q7*q8;
		float q50 = q13*q1-q14*q8;
		float q51 = 1/(q49+q50);
		
		m.m00 = (m11*m22*m33-m11*m23*m32-m21*m12*m33+m21*m13*m32+m31*m12*m23-m31*m13*m22)*q51;
		m.m01 = -(m01*m22*m33-m01*m23*m32-q21*m33+q22*m32)*q51;
		m.m02 = (q25*m33-q26*m32-q27*m33+q28*m32)*q51;
		m.m03 = -(q25*m23-q26*m22-q27*m23+q28*m22+q21*m13-q22*m12)*q51;
		m.m10 = -(q29*m33-q30*m32-q31*m33+q32*m32)*q51;
		m.m11 = (q35*m33-q36*m32-q37*m33+q38*m32)*q51;
		m.m12 = -(q41*m33-q42*m32-q43*m33+q44*m32)*q51;
		m.m13 = (q41*m23-q42*m22-q43*m23+q44*m22+q37*m13-q38*m12)*q51;
		m.m20 = (q7*m33-q30*m31-q14*m33+q32*m31)*q51;
		m.m21 = -(q48*m33-q36*m31-q13*m33+q38*m31)*q51;
		m.m22 = (q45*m33-q42*m31-q6*m33+q44*m31)*q51;
		m.m23 = -(q45*m23-q42*m21-q6*m23+q44*m21+q13*m13-q38*m11)*q51;
		
		return m;
	}
	
	/**
	 * vCreate the billboard matrix: a rotation matrix created from an arbitrary set
	 * of axis.  Store those axis values in the first 3 columns of the matrix.  Col
	 * 1 is the X axis, col 2 is the Y axis, and col 3 is the Z axis.  We are
	 * rotating right into X, up into Y, and look into Z.  The rotation matrix
	 * created from the rows will translate the arbitrary axis set to the global
	 * vaxis set.  Lastly, OpenGl stores the matrices by columns, so enter the data
	 * into the array columns first.
	 *
	 * pos: position of billboard
	 * right, up, look: orientation of billboard x,y,z axes
	 */
	public static void createBillboardMatrix(FloatBuffer matrix, GL_Vector right, GL_Vector up, GL_Vector look, GL_Vector pos)
	{
		matrix.put(0,  right.x);
		matrix.put(1,  right.y);
		matrix.put(2,  right.z);
		matrix.put(3,  0);
		
		matrix.put(4,  up.x);
		matrix.put(5,  up.y);
		matrix.put(6,  up.z);
		matrix.put(7,  0);
		
		matrix.put(8,  look.x);
		matrix.put(9,  look.y);
		matrix.put(10, look.z);
		matrix.put(11, 0);
		
		// Add the translation in as well.
		matrix.put(12, pos.x);
		matrix.put(13, pos.y);
		matrix.put(14, pos.z);
		matrix.put(15, 1);
	}
}