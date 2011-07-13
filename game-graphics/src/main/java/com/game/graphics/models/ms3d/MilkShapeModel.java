package com.game.graphics.models.ms3d;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.game.common.util.LittleEndianDataInputStream;
import com.game.graphics.models.Joint;
import com.game.graphics.models.Keyframe;
import com.game.graphics.models.Material;
import com.game.graphics.models.Mesh;
import com.game.graphics.models.Model;
import com.game.graphics.models.Triangle;
import com.game.graphics.models.Vertex;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Sprite;

// MilkShape3Dv4 loader, see: http://chumbalum.swissquake.ch/ms3d/ms3dspec.txt
public class MilkShapeModel extends AbstractMilkShapeModel {

	public static final String ID = "MS3D000000";
	public static final int VERSION = 4;

	protected float animationFPS;
	protected float currentTime;

	public MilkShapeModel(InputStream in, Graphics graphics) throws IOException {
		super(graphics);

		animationFPS = 0;
		currentTime = 0;

		LittleEndianDataInputStream data = new LittleEndianDataInputStream(new BufferedInputStream(in));

		// Confirm the file header is valid
		this.verifyHeader(data);

		super.meshes = this.parseMeshes(data);

		// Parse the materials
		materials = this.parseMaterials(data);

		animationFPS = data.readFloat();
		currentTime = data.readFloat();

		super.numFrames = data.readInt();

		// Parse the bone data
		joints = this.parseJoints(data);

		// There is some extra data next, but it's extra so lets ignore it unless we decide we need it

		// Update the material and joint reference in all meshes
		super.init();
	}

	private void verifyHeader(LittleEndianDataInputStream data) throws IOException {
		// Load the ID field and confirm it matches
		String identifier = data.readString(10);
		if (!ID.equals(identifier))
			throw new IOException("Attempted to load an unrecognised model format: " + identifier);

		int version = data.readInt();
		if (version != VERSION)
			throw new IOException("Attempted to load unsupported model version: " + version);
	}

	private Mesh[] parseMeshes(LittleEndianDataInputStream data) throws IOException {
		int numVertices = data.readUnsignedShort();
		Vertex[] vertices = new Vertex[numVertices];
		for (int i = 0;i < numVertices;i++) {
			int flags = data.readByte();
			Vector3f position = new Vector3f(
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			);

			int boneIndex = data.readByte();

			// Skip "reference count"
			for (int skip = 1, skipped = 0;skipped < skip;)
				skipped += data.skipBytes(skip - skipped);

			vertices[i] = new Vertex(
				flags,
				position,
				new Vector2f(), // Set later on
				boneIndex
			);
		}

		int numTriangles = data.readUnsignedShort();
		Triangle[] triangles = new Triangle[numTriangles];
		for (int i = 0;i < numTriangles;i++) {
			int flags = data.readUnsignedShort();

			int[] vertexIndices = new int[3];
			for (int j = 0;j < 3;j++)
				vertexIndices[j] = data.readUnsignedShort();

			Vector3f[] normals = new Vector3f[3];
			for (int j = 0;j < 3;j++) {
				normals[j] = new Vector3f(
					data.readFloat(),
					data.readFloat(),
					data.readFloat()
				);
			}

			Vector2f[] texturePosition = new Vector2f[3];
			for (int j = 0;j < 3;j++)
				texturePosition[j] = new Vector2f();

			for (int j = 0;j < 3;j++)
				texturePosition[j].x = data.readFloat();

			for (int j = 0;j < 3;j++)
				texturePosition[j].y = data.readFloat();

			Triangle.Point[] points = new Triangle.Point[3];
			for (int j = 0;j < 3;j++) {
				Vertex vertex = vertices[vertexIndices[j]];
				vertex.texturePosition.x = texturePosition[j].x;
				vertex.texturePosition.y = texturePosition[j].y;

				points[j] = new Triangle.Point(
					vertex,
					normals[j]
				);
			}

			int smoothingGroup = data.readByte();

			// Skip "group index"
			for (int skip = 1, skipped = 0;skipped < skip;)
				skipped += data.skipBytes(skip - skipped);

			triangles[i] = new Triangle(
				flags,
				points,
				smoothingGroup
			);
		}

		int numMeshes = data.readUnsignedShort();
		Mesh[] meshes = new Mesh[numMeshes];

		for (int i = 0;i < numMeshes;i++) {
			int flags = data.readByte();

			byte[] buffer = new byte[32];
			data.readFully(buffer, 0, buffer.length);
			String name = new String(buffer).trim();

			int _numTriangles = data.readUnsignedShort();
			Triangle[] _triangles = new Triangle[_numTriangles];
			for (int j = 0;j < _numTriangles;j++)
				_triangles[j] = triangles[data.readUnsignedShort()];

			int materialIndex = data.readByte();

			meshes[i] = new Mesh(name, flags, materialIndex, _triangles);
		}

		return meshes;
	}

	private Material[] parseMaterials(LittleEndianDataInputStream data) throws IOException {
		int numMaterials = data.readUnsignedShort();
		Material[] materials = new Material[numMaterials];

		for (int i = 0;i < numMaterials;i++) {
			String name = data.readString(32);
			if (name.isEmpty())
				throw new IOException("Found no-named material.");

			float[] ambient = {
				data.readFloat(),
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			};

			float[] diffuse = {
				data.readFloat(),
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			};

			float[] specular = {
				data.readFloat(),
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			};

			float[] emissive = {
				data.readFloat(),
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			};

			float shininess = data.readFloat();
			float transparency = data.readFloat();

			// Skip "mode"
			for (int skip = 1, skipped = 0;skipped < skip;)
				skipped += data.skipBytes(skip - skipped);

			// Load the color map texture, if there is one
			Sprite colorMap = null;
			String colorMapName = data.readString(128);
			if (!colorMapName.isEmpty()) {
				URL resource = Model.class.getResource(colorMapName);
				if (resource == null)
					throw new IOException("Unable to find color map resource: " + colorMapName);

				colorMap = graphics.loadSprite(resource);
			}

			// Load the alpha map texture, if there is one
			Sprite alphaMap = null;
			String alphaMapName = data.readString(128);
			if (!alphaMapName.isEmpty()) {
				URL resource = Model.class.getResource(alphaMapName);
				if (resource == null)
					throw new IOException("Unable to find alpha map resource: " + alphaMapName);

				alphaMap = graphics.loadSprite(resource);
			}

			materials[i] = new Material(name, ambient, diffuse, specular, emissive, shininess, transparency, colorMap, alphaMap);
		}

		return materials;
	}

	private Joint[] parseJoints(LittleEndianDataInputStream data) throws IOException {
		int numJoints = data.readShort();
		Map<String, Joint> joints = new HashMap<String, Joint>();

		for (int i = 0;i < numJoints;i++) {
			int flags = data.readByte();

			String name = data.readString(32);
			if (name.isEmpty())
				throw new IOException("Found no-named joint.");

			String parentName = data.readString(32);
			Joint parent = null;

			if (!parentName.isEmpty()) {
				if (!joints.containsKey(parentName))
					throw new IOException("Unable to find parent for joint: " + name);

				parent = joints.get(parentName);
			}

			Vector3f rotation = new Vector3f(
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			);

			Vector3f position = new Vector3f(
				data.readFloat(),
				data.readFloat(),
				data.readFloat()
			);

			int numRotationKeyframes = data.readShort();
			int numPositionKeyframes = data.readShort();

			Keyframe[] rotationKeyframes = new Keyframe[numRotationKeyframes];
			for (int j = 0;j < numRotationKeyframes;j++) {
				rotationKeyframes[j] = new Keyframe(
					data.readFloat(),		// time
					new Vector3f(
						data.readFloat(),	// x
						data.readFloat(),	// y
						data.readFloat()	// z
					)
				);
			}

			Keyframe[] positionKeyframes = new Keyframe[numPositionKeyframes];
			for (int j = 0;j < numPositionKeyframes;j++) {
				positionKeyframes[j] = new Keyframe(
					data.readFloat(),		// time
					new Vector3f(
						data.readFloat(),	// x
						data.readFloat(),	// y
						data.readFloat()	// z
					)
				);
			}

			joints.put(name, new Joint(name, parent, flags, position, rotation, positionKeyframes, rotationKeyframes));
		}

		return joints.values().toArray(new Joint[joints.size()]);
	}
}
