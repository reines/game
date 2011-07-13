package com.game.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.codec.Packet;
import com.game.common.model.Item;
import com.game.common.model.Tile;
import com.thoughtworks.xstream.XStream;

public class PersistenceManager {
	private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

	public class PacketHandler {
		public Packet.Type[] types;
		public Class<?> handler;
	}

	public class ItemHandler {
		public int[] ids;
		public Class<?> handler;
	}

	protected static final XStream xstream;

	static {
		xstream = new XStream();

		xstream.alias("type", Packet.Type.class);

		xstream.alias("PacketHandler", PacketHandler.class);
		xstream.alias("ItemHandler", ItemHandler.class);

		xstream.alias("ItemDef", Item.Definition.class);
		xstream.alias("TileDef", Tile.Definition.class);
	}

	public static void alias(String name, Class<?> type) {
		xstream.alias(name, type);
	}

	public static Object load(URL path) {
		try {
			InputStream in = path.openStream();
			Object o = xstream.fromXML(in);
			in.close();

			return o;
		}
		catch (IOException ioe) {
			log.error("Error loading object: " + ioe.getMessage());
			System.exit(1); // fatal error

			return null;
		}
	}

	public static void save(Object o, File f) throws IOException {
		OutputStream out = new FileOutputStream(f);
		xstream.toXML(o, out);
		out.close();
	}
}
