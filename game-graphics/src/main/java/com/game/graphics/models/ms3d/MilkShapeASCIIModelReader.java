package com.game.graphics.models.ms3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MilkShapeASCIIModelReader extends BufferedReader {
	public MilkShapeASCIIModelReader(InputStreamReader in) {
		super (in);
	}

	@Override
	public String readLine() throws IOException {
		String line = null;

		while ((line = super.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("//"))
				continue;

			// This is a valid line, return it to process
			break;
		}

		return line;
	}

	public String readLineNotNull() throws IOException {
		String line = this.readLine();
		if (line == null)
			throw new IOException("Prematurely reached end of file.");

		return line;
	}

	public String readQuotedString() throws IOException {
		String line = this.readLineNotNull();
		line = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
		if (line.isEmpty())
			return null;

		return line;
	}

	public int readInt() throws IOException {
		return Integer.parseInt(this.readLineNotNull());
	}

	public float readFloat() throws IOException {
		return Float.parseFloat(this.readLineNotNull());
	}
}
