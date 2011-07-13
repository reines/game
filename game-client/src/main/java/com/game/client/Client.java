package com.game.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.mina.core.RuntimeIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.engine.Camera;
import com.game.client.ui.ClientFrame;
import com.game.client.ui.HUD;
import com.game.client.ui.LoginWindow;
import com.game.common.codec.Packet;
import com.game.common.codec.PacketBuilder;
import com.game.common.model.Hash;
import com.game.common.model.Item;
import com.game.common.model.PlayerProfile;
import com.game.common.util.PersistenceManager;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.renderer.Graphics3D;

public final class Client extends ClientFrame {
	private static final Logger log = LoggerFactory.getLogger(Client.class);

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 36954;
	public static final int[] ICON_SIZES = {128, 64, 32, 16};
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;

	protected static final Options options;

	static {
		options = new Options();

		options.addOption("h", "help", false, "print this help.");
		options.addOption("v", "disable-vsync", false, "disable vsync, default false.");
		options.addOption("p", "port", true, "server port number to connect to, default " + DEFAULT_PORT + ".");
		options.addOption("s", "server", true, "server hostname to connect to, default " + DEFAULT_HOST + ".");
	}

	public static final void main(String[] args) {
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine config = parser.parse(options, args);

			if (config.hasOption("h")) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("java " + Client.class.getSimpleName(), options);
				return;
			}

			Client client = new Client(config);
			client.run();
		}
		catch (ParseException e) {
			log.error("Error parsing command line options: " + e);
		}
		catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}

	protected static BufferedImage[] loadIcons(String name, int[] sizes) {
		BufferedImage[] iconImages = new BufferedImage[sizes.length];
		for (int i = 0;i < iconImages.length;i++) {
			try {
				URL resource = Client.class.getResource(name + sizes[i] + ".png");
				if (resource == null) {
					// fatal error
					throw new RuntimeException("Resource not found: icon" + name + sizes[i] + ".png");
				}

				iconImages[i] = ImageIO.read(resource);
			}
			catch (IOException e) {
				// fatal error
				throw new RuntimeException("Error loading icon: " + e);
			}
		}

		return iconImages;
	}

	protected LoginWindow loginWindow;
	protected final CommandLine config;
	protected final Connection connection;
	protected final WorldManager world;
	protected final Camera camera;
	protected final HUD hud;
	protected final PublicKey publicKey;

	public Client(CommandLine config) {
		super ("Test Client", DEFAULT_WIDTH, DEFAULT_HEIGHT, Client.loadIcons("icon", ICON_SIZES));

		this.config = config;

		if (config.hasOption("v"))
			super.setVSync(false);

		loginWindow = new LoginWindow(this);
		connection = new Connection(this);
		world = new WorldManager(this);
		camera = world.getCamera();
		hud = new HUD(this);

		publicKey = (PublicKey) PersistenceManager.load(Client.class.getResource("publickey.xml"));

		// Pre-load the item definitions
		Item.load();

		log.info("New client started");
	}

	public HUD getHUD() {
		return hud;
	}

	public Connection getConnection() {
		return connection;
	}

	public WorldManager getWorldManager() {
		return world;
	}

	public boolean isLoggedIn() {
		return loginWindow == null;
	}

	public void login(final String username, final String password) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Random random = new Random();

				String hostname = config.getOptionValue("s", DEFAULT_HOST);
				int port = DEFAULT_PORT;
				if (config.hasOption("p")) {
					try {
						port = Integer.parseInt(config.getOptionValue("p"));
					}
					catch (NumberFormatException e) {
						log.error("Invalid port number: " + config.getOptionValue("p") + ", trying default: " + port);
					}
				}

				try {
					connection.open(hostname, port);

					Hash usernameHash = new Hash(username.toLowerCase());// Clean the username and hash it to get the users ID
					Hash passwordHash = new Hash(usernameHash + password); // Salt the password and hash it

					PacketBuilder packet = new PacketBuilder(Packet.Type.LOGIN_SEND);

					packet.putHash(usernameHash);
					packet.putHash(passwordHash);

					long encryptionSeed = random.nextLong();
					packet.putLong(encryptionSeed);

					long decryptionSeed = random.nextLong();
					packet.putLong(decryptionSeed);

					// Encrypt the login packet with our public key
					packet.encrypt(publicKey);

					connection.write(packet);

					// from now on we want to encrypt outgoing packets
					connection.enableEncryption(encryptionSeed, decryptionSeed);
				}
				catch (RuntimeIoException rioe) {
					connection.close();

					log.warn("Failed to connect to: " + hostname + ":" + port);
					loginWindow.failed("Failed to connect to the game server.");
				}
			}
		}).start();
	}

	public void loginSuccess(PlayerProfile profile) {
		world.init(profile);
		loginWindow = null;

		log.info("Successfully logged in.");
	}

	public void loginFailed(String message) {
		loginWindow.failed(message);
		connection.close();
	}

	public void handleLogout() {
		if (loginWindow != null)
			return;

		connection.close();
		loginWindow = new LoginWindow(this);
	}

	@Override
	protected void update(long now) {
		// Update the connection
		connection.update(now);

		// If there is a login window, update it
		if (loginWindow != null) {
			loginWindow.update(now);
			return;
		}

		// Update the world
		world.update(now);

		// Update the HUD
		hud.update(now);
	}

	@Override
	public void display(Graphics g) {
		Graphics2D g2d = g.get2D();

		// If there is a login window, display it
		if (loginWindow != null) {
			g2d.begin();
			{
				loginWindow.display(g2d);
			}
			g2d.end();

			return;
		}

		Graphics3D g3d = g.get3D();

		g3d.begin(world.getLocation(), camera.getZoom(), camera.getRotation());
		{
			// Display the world
			world.display(g3d);
		}
		g3d.end();

		g2d.begin();
		{
			// Draw the HUD on-top of the world
			hud.display(g2d);
		}
		g2d.end();
	}

	@Override
	public void mouseClicked(int x, int y, boolean left) {
		// If there is a login window, it was clicked
		if (loginWindow != null) {
			loginWindow.mouseClicked(x, y, left);
			return;
		}

		// If the HUD was clicked, don't pass the key to the world
		if (hud.mouseClicked(x, y, left))
			return;

		// Pass the click to the world
		world.mouseClicked(x, y, left);
	}

	@Override
	public void keyPressed(int keyCode, char keyChar) {
		// If there is a login window, the key goes there
		if (loginWindow != null) {
			loginWindow.keyPressed(keyCode, keyChar);
			return;
		}

		// Send the key to the HUD
		hud.keyPressed(keyCode, keyChar);
	}

	@Override
	protected void close() {
		connection.destroy();
	}
}
