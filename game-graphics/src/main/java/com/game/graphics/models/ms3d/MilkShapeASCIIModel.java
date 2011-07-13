package com.game.graphics.models.ms3d;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.game.graphics.models.Joint;
import com.game.graphics.models.Keyframe;
import com.game.graphics.models.Material;
import com.game.graphics.models.Mesh;
import com.game.graphics.models.Model;
import com.game.graphics.models.Triangle;
import com.game.graphics.models.Vertex;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Sprite;

public class MilkShapeASCIIModel extends AbstractMilkShapeModel {

	protected int currentFrame;

	public MilkShapeASCIIModel(InputStream in, Graphics graphics) throws IOException {
		super (graphics);

		currentFrame = 0;

		MilkShapeASCIIModelReader reader = new MilkShapeASCIIModelReader(new InputStreamReader(in));
		for (String line;(line = reader.readLine()) != null;) {
			line = line.toLowerCase();

			if (line.startsWith("frames:"))
				super.numFrames = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
			else if (line.startsWith("frame:"))
				currentFrame = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
			else if (line.startsWith("meshes:")) {
				int numMeshes = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				super.meshes = this.parseMeshes(reader, numMeshes);
			}
			else if (line.startsWith("materials:")) {
				int numMaterials = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				super.materials = this.parseMaterials(reader, numMaterials);
			}
			else if (line.startsWith("bones:")) {
				int numJoints = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
				super.joints = this.parseJoints(reader, numJoints);
			}
			else
				throw new IOException("Unrecognised line in MilkShapeModel: " + line);
		}

		// Update the material and joint reference in all meshes
		super.init();
	}

	private Mesh[] parseMeshes(MilkShapeASCIIModelReader reader, int numMeshes) throws IOException {
		Mesh[] meshes = new Mesh[numMeshes];

		for (int i = 0;i < numMeshes;i++) {
			// Read the header
			String header = reader.readLineNotNull();

			// Take the name, surrounded by " marks
			String name = header.substring(1, header.lastIndexOf('"'));

			// Remove the name, we should have 2 integers left, separated by a space
			header = header.substring(header.lastIndexOf('"') + 1).trim();
			String[] tokens = header.split(" ");
			int flags = Integer.parseInt(tokens[0]);
			int materialIndex = Integer.parseInt(tokens[1]);

			// Read the vertices
			int numVertices = reader.readInt();
			Vertex[] vertices = new Vertex[numVertices];
			for (int j = 0;j < numVertices;j++) {
				tokens = reader.readLineNotNull().split(" ");
				vertices[j] = new Vertex(
					Integer.parseInt(tokens[0]),	// flags
					new Vector3f(
						Float.parseFloat(tokens[1]),	// x
						Float.parseFloat(tokens[2]),	// y
						Float.parseFloat(tokens[3])		// z
					),
					new Vector2f(
						Float.parseFloat(tokens[4]),// u
						Float.parseFloat(tokens[5])	// v
					),
					Integer.parseInt(tokens[6])		// bone index
				);
			}

			// Read the normals
			int numNormals = reader.readInt();
			Vector3f[] normals = new Vector3f[numNormals];
			for (int j = 0;j < numNormals;j++) {
				tokens = reader.readLineNotNull().split(" ");
				normals[j] = new Vector3f(
					Float.parseFloat(tokens[0]),	// x
					Float.parseFloat(tokens[1]),	// y
					Float.parseFloat(tokens[2])		// z
				);
			}

			// Read the triangles
			int numTriangles = reader.readInt();
			Triangle[] triangles = new Triangle[numTriangles];
			for (int j = 0;j < numTriangles;j++) {
				tokens = reader.readLineNotNull().split(" ");

				Triangle.Point[] points = {
					new Triangle.Point(
						vertices[Integer.parseInt(tokens[1])],	// vertex 1
						normals[Integer.parseInt(tokens[4])]	// normal 1
					),
					new Triangle.Point(
						vertices[Integer.parseInt(tokens[2])],	// vertex 2
						normals[Integer.parseInt(tokens[5])]	// normal 2
					),
					new Triangle.Point(
						vertices[Integer.parseInt(tokens[3])],	// vertex 3
						normals[Integer.parseInt(tokens[6])]	// normal 3
					)
				};

				triangles[j] = new Triangle(
					Integer.parseInt(tokens[0]),	// flags
					points,							// points
					Integer.parseInt(tokens[7])	// smoothing group
				);
			}

			meshes[i] = new Mesh(name, flags, materialIndex, triangles);
		}

		return meshes;
	}

	private Material[] parseMaterials(MilkShapeASCIIModelReader reader, int numMaterials) throws IOException {
		Material[] materials = new Material[numMaterials];

		for (int i = 0;i < numMaterials;i++) {
			// Take the name, surrounded by " marks
			String name = reader.readQuotedString();
			if (name == null)
				throw new IOException("Found no-named material.");

			String[] tokens;

			tokens = reader.readLineNotNull().split(" ");
			float[] ambient = {
				Float.parseFloat(tokens[0]),
				Float.parseFloat(tokens[1]),
				Float.parseFloat(tokens[2]),
				Float.parseFloat(tokens[3])
			};

			tokens = reader.readLineNotNull().split(" ");
			float[] diffuse = {
				Float.parseFloat(tokens[0]),
				Float.parseFloat(tokens[1]),
				Float.parseFloat(tokens[2]),
				Float.parseFloat(tokens[3])
			};

			tokens = reader.readLineNotNull().split(" ");
			float[] specular = {
				Float.parseFloat(tokens[0]),
				Float.parseFloat(tokens[1]),
				Float.parseFloat(tokens[2]),
				Float.parseFloat(tokens[3])
			};

			tokens = reader.readLineNotNull().split(" ");
			float[] emissive = {
				Float.parseFloat(tokens[0]),
				Float.parseFloat(tokens[1]),
				Float.parseFloat(tokens[2]),
				Float.parseFloat(tokens[3])
			};

			float shininess = reader.readFloat();
			float transparency = reader.readFloat();

			// Load the color map texture, if there is one
			Sprite colorMap = null;
			String colorMapName = reader.readQuotedString();
			if (colorMapName != null) {
				URL resource = Model.class.getResource(colorMapName);
				if (resource == null)
					throw new IOException("Unable to find color map resource: " + colorMapName);

				colorMap = graphics.loadSprite(resource);
			}

			// Load the alpha map texture, if there is one
			Sprite alphaMap = null;
			String alphaMapName = reader.readQuotedString();
			if (alphaMapName != null) {
				URL resource = Model.class.getResource(alphaMapName);
				if (resource == null)
					throw new IOException("Unable to find alpha map resource: " + alphaMapName);

				alphaMap = graphics.loadSprite(resource);
			}

			materials[i] = new Material(name, ambient, diffuse, specular, emissive, shininess, transparency, colorMap, alphaMap);
		}

		return materials;
	}

	private Joint[] parseJoints(MilkShapeASCIIModelReader reader, int numJoints) throws IOException {
		Map<String, Joint> joints = new HashMap<String, Joint>();

		for (int i = 0;i < numJoints;i++) {
			String name = reader.readQuotedString();
			if (name == null)
				throw new IOException("Found no-named joint.");

			String parentName = reader.readQuotedString();
			Joint parent = null;

			if (parentName != null) {
				if (!joints.containsKey(parentName))
					throw new IOException("Unable to find parent for joint: " + name);

				parent = joints.get(parentName);
			}

			String[] tokens = reader.readLineNotNull().split(" ");

			int flags = Integer.parseInt(tokens[0]);

			Vector3f position = new Vector3f(
				Float.parseFloat(tokens[1]),		// x
				Float.parseFloat(tokens[2]),		// y
				Float.parseFloat(tokens[3])			// z
			);

			Vector3f rotation = new Vector3f(
				Float.parseFloat(tokens[4]),		// x
				Float.parseFloat(tokens[5]),		// y
				Float.parseFloat(tokens[6])			// z
			);

			int numPositionKeyframes = reader.readInt();
			Keyframe[] positionKeyframes = new Keyframe[numPositionKeyframes];
			for (int j = 0;j < numPositionKeyframes;j++) {
				tokens = reader.readLineNotNull().split(" ");
				positionKeyframes[j] = new Keyframe(
					Float.parseFloat(tokens[0]),		// time
					new Vector3f(
						Float.parseFloat(tokens[1]),	// x
						Float.parseFloat(tokens[2]),	// y
						Float.parseFloat(tokens[3])		// z
					)
				);
			}

			int numRotationKeyframes = reader.readInt();
			Keyframe[] rotationKeyframes = new Keyframe[numRotationKeyframes];
			for (int j = 0;j < numRotationKeyframes;j++) {
				tokens = reader.readLineNotNull().split(" ");
				rotationKeyframes[j] = new Keyframe(
					Float.parseFloat(tokens[0]),		// time
					new Vector3f(
						Float.parseFloat(tokens[1]),	// x
						Float.parseFloat(tokens[2]),	// y
						Float.parseFloat(tokens[3])		// z
					)
				);
			}

			joints.put(name, new Joint(name, parent, flags, position, rotation, positionKeyframes, rotationKeyframes));
		}

		return joints.values().toArray(new Joint[joints.size()]);
	}
}
