package com.game.graphics.math;

import org.lwjgl.util.vector.Vector3f;

// Some parts based on: http://mypage.iu.edu/~natjohns/najgl/
public class Matrix34f {

	public float m00, m01, m02, m03;
	public float m10, m11, m12, m13;
	public float m20, m21, m22, m23;

	public Matrix34f() {

	}

	public Matrix34f(Matrix34f m) {
		this.copy(m);
	}

	public void copy(Matrix34f m) {
		this.m00 = m.m00;
		this.m01 = m.m01;
		this.m02 = m.m02;
		this.m03 = m.m03;

		this.m10 = m.m10;
		this.m11 = m.m11;
		this.m12 = m.m12;
		this.m13 = m.m13;

		this.m20 = m.m20;
		this.m21 = m.m21;
		this.m22 = m.m22;
		this.m23 = m.m23;
	}

	// Concat this matrix with another and set the result into this matrix
	public void concat(Matrix34f m) {
		this.concat(this, m);
	}

	// Concat 2 matrices and set the result into this matrix
	public void concat(Matrix34f a, Matrix34f b) {
		this.m00 = a.m00 * b.m00 + a.m01 * b.m10 + a.m02 * b.m20;
		this.m01 = a.m00 * b.m01 + a.m01 * b.m11 + a.m02 * b.m21;
		this.m02 = a.m00 * b.m02 + a.m01 * b.m12 + a.m02 * b.m22;
		this.m03 = a.m00 * b.m03 + a.m01 * b.m13 + a.m02 * b.m23 + a.m03;

		this.m10 = a.m10 * b.m00 + a.m11 * b.m10 + a.m12 * b.m20;
		this.m11 = a.m10 * b.m01 + a.m11 * b.m11 + a.m12 * b.m21;
		this.m12 = a.m10 * b.m02 + a.m11 * b.m12 + a.m12 * b.m22;
		this.m13 = a.m10 * b.m03 + a.m11 * b.m13 + a.m12 * b.m23 + a.m13;

		this.m20 = a.m20 * b.m00 + a.m21 * b.m10 + a.m22 * b.m20;
		this.m21 = a.m20 * b.m01 + a.m21 * b.m11 + a.m22 * b.m21;
		this.m22 = a.m20 * b.m02 + a.m21 * b.m12 + a.m22 * b.m22;
		this.m23 = a.m20 * b.m03 + a.m21 * b.m13 + a.m22 * b.m23 + a.m23;
	}

	// TODO: Fix (wtf does this do, tidy it up!)
	public void angleMatrix(Vector3f v) {
        float angle;
        float sr, sp, sy, cr, cp, cy;

        angle = (float) (v.z * (Math.PI * 2 / 360));
        sy = (float) java.lang.Math.sin(angle);
        cy = (float) java.lang.Math.cos(angle);
        angle = (float) (v.y * (Math.PI * 2 / 360));
        sp = (float) java.lang.Math.sin(angle);
        cp = (float) java.lang.Math.cos(angle);
        angle = (float) (v.x * (Math.PI * 2 / 360));
        sr = (float) java.lang.Math.sin(angle);
        cr = (float) java.lang.Math.cos(angle);

        // matrix = (Z * Y) * X
        this.m00 = cp * cy;
        this.m10 = cp * sy;
        this.m20 = -sp;

        this.m01 = sr * sp * cy + cr * -sy;
        this.m11 = sr * sp * sy + cr * cy;
        this.m21 = sr * cp;

        this.m02 = (cr * sp * cy + -sr * -sy);
        this.m12 = (cr * sp * sy + -sr * cy);
        this.m22 = cr * cp;

        this.m03 = 0.0f;
        this.m13 = 0.0f;
        this.m23 = 0.0f;
	}

	public Vector3f vectorIRotate(Vector3f v) {
        return new Vector3f(
        	v.x * this.m00 + v.y * this.m10 + v.z * this.m20,
        	v.x * this.m01 + v.y * this.m11 + v.z * this.m21,
        	v.x * this.m02 + v.y * this.m12 + v.z * this.m22
        );
	}

	public Vector3f vectorRotate(Vector3f v) {
		return new Vector3f(
			Vector3f.dot(v, new Vector3f(this.m00, this.m01, this.m02)),
			Vector3f.dot(v, new Vector3f(this.m10, this.m11, this.m22)),
			Vector3f.dot(v, new Vector3f(this.m20, this.m21, this.m22))
		);
	}

	@Override
	public String toString() {
		String str = "";

		str += "[ " + this.m00 + " " + this.m01 + " " + this.m02 + " " + this.m03 + " ]";
		str += "[ " + this.m10 + " " + this.m11 + " " + this.m12 + " " + this.m13 + " ]";
		str += "[ " + this.m20 + " " + this.m21 + " " + this.m22 + " " + this.m23 + " ]";

		return str;
	}
}
