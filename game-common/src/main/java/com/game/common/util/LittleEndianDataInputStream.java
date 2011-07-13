package com.game.common.util;

/*
 * LittleEndianDataInputStream.java
 *
 * Copyright (C) 2002 Kevin J. Duling (kevin@dark-horse.net)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Created on May 13, 2002, 2:52 PM
 */

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A DataInputStream-like object that reads in Little Endian data.
 *
 * @author Kevin J. Duling
 * @version $Revision: 1.1 $
 * @see java.io.DataInputStream
 */
public class LittleEndianDataInputStream extends FilterInputStream implements DataInput {

	protected final DataInputStream dis;

	/**
	 * Construct a LittleEndianDataInputStream from a InputStream
	 *
	 * @param in
	 *            a java.io.InputStream object
	 */
	public LittleEndianDataInputStream(InputStream in) {
		super(in);

		dis = new DataInputStream(in);
	}

	/**
	 * See the general contract of the readBoolean method of DataInput. Bytes
	 * for this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream has reached the end.
	 * @return the boolean value read
	 */
	@Override
	public boolean readBoolean() throws IOException {
		return dis.readBoolean();
	}

	/**
	 * See the general contract of the readByte method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return a byte
	 */
	@Override
	public byte readByte() throws IOException {
		return dis.readByte();
	}

	/**
	 * See the general contract of the readChar method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading two
	 *             bytes.
	 * @return the next two bytes of this input stream as a Unicode character.
	 */
	@Override
	public char readChar() throws IOException, EOFException {
		return dis.readChar();
	}

	/**
	 * See the general contract of the readDouble method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input streams reaches the end before reading eight
	 *             bytes.
	 * @return the next eight bytes of this input stream, interpreted as a
	 *         double.
	 */
	@Override
	public double readDouble() throws IOException, EOFException {
		return Double.longBitsToDouble(this.readLong());
	}

	/**
	 * See the general contract of the readFloat method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading four
	 *             bytes.
	 * @return the next four bytes of this input stream, interpreted as a float.
	 */
	@Override
	public float readFloat() throws IOException, EOFException {
		return Float.intBitsToFloat(this.readInt());
	}

	/**
	 * See the general contract of the readFully method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @param b
	 *            the buffer into which the data is read.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading all the
	 *             bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void readFully(byte[] b) throws IOException, EOFException {
		dis.readFully(b);
	}

	/**
	 * Reads len bytes from an input stream. This method blocks until one of the
	 * following conditions occurs:
	 * <UL>
	 * <LI>len bytes of input data are available, in which case a normal return
	 * is made.</LI>
	 * <LI>End of file is detected, in which case an EOFException is thrown.</LI>
	 * <LI>An I/O error occurs, in which case an IOException other than
	 * EOFException is thrown.</LI>
	 * </UL>
	 * If b is null, a NullPointerException is thrown. If off is negative, or
	 * len is negative, or off+len is greater than the length of the array b,
	 * then an IndexOutOfBoundsException is thrown. If len is zero, then no
	 * bytes are read. Otherwise, the first byte read is stored into element
	 * b[off], the next one into b[off+1], and so on. The number of bytes read
	 * is, at most, equal to len.
	 *
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            an int specifying the offset into the data.
	 * @param len
	 *            an int specifying the number of bytes to read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes.
	 */
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException, EOFException {
		dis.readFully(b, off, len);
	}

	/**
	 * See the general contract of the readInt method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading four
	 *             bytes.
	 * @return the next four bytes of this input stream, interpreted as an int.
	 */
	@Override
	public int readInt() throws IOException, EOFException {
		int res = 0;

		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8)
			res |= (dis.readByte() & 0xff) << shiftBy;

		return res;
	}

	/**
	 * This method does not properly convert bytes to characters. As of JDK 1.1,
	 * the preferred way to read lines of text is via the
	 * BufferedReader.readLine() method.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return the next line of text from this input stream.
	 * @deprecated Used here only for compatibility with DataInputStream
	 */
	@Deprecated
	@Override
	public String readLine() throws IOException {
		return dis.readLine();
	}

	/**
	 * See the general contract of the readLong method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading two
	 *             bytes.
	 * @return the next eight bytes of this input stream, interpreted as a long.
	 */
	@Override
	public long readLong() throws IOException, EOFException {
		long res = 0;

		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8)
			res |= (dis.readByte() & 0xff) << shiftBy;

		return res;
	}

	/**
	 * See the general contract of the readShort method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @throws EOFException
	 *             if this input stream reaches the end before reading two
	 *             bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return the next two bytes of this input stream, interpreted as a signed
	 *         16-bit number.
	 */
	@Override
	public short readShort() throws IOException, EOFException {
		final int low = this.readByte() & 0xff;
		final int high = this.readByte() & 0xff;

		return (short) (high << 8 | low);
	}

	/**
	 * Reads from the stream in a representation of a Unicode character string
	 * encoded in Java modified UTF-8 format; this string of characters is then
	 * returned as a String. The details of the modified UTF-8 representation
	 * are exactly the same as for the readUTF method of DataInput.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return a Unicode string
	 */
	@Override
	public String readUTF() throws IOException {
		return dis.readUTF();
	}

	/**
	 * See the general contract of the readUnsignedByte method of DataInput.
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws EOFException
	 *             if this input stream reaches the end before reading two
	 *             bytes.
	 * @return the next byte of this input stream, interpreted as an unsigned
	 *         8-bit number.
	 */
	@Override
	public int readUnsignedByte() throws IOException, EOFException {
		return dis.readUnsignedByte();
	}

	/**
	 * See the general contract of the readUnsignedShort method of DataInput.
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return the next two bytes of this input stream, interpreted as an
	 *         unsigned 16-bit integer.
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		final int low = this.readByte() & 0xff;
		final int high = this.readByte() & 0xff;

		return (high << 8 | low);
	}

	/**
	 * See the general contract of the skipBytes method of DataInput. Bytes for
	 * this operation are read from the contained input stream.
	 *
	 * @param n
	 *            the number of bytes to be skipped.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @return the actual number of bytes skipped.
	 */
	@Override
	public int skipBytes(int n) throws IOException {
		return dis.skipBytes(n);
	}

	public String readString(int l) throws IOException {
		byte[] buffer = new byte[l];
		dis.readFully(buffer, 0, l);

		for (int i = 0; i < l; i++)
			if (buffer[i] == '\0')
				return new String(buffer, 0, i);

		return new String(buffer);
	}
}
