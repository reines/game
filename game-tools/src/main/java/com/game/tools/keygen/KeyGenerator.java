package com.game.tools.keygen;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.util.PersistenceManager;
import com.game.tools.fontconverter.FontConverter;

public class KeyGenerator {
	private static final Logger log = LoggerFactory.getLogger(FontConverter.class);

	public static final int DEFAULT_SIZE = 1024;

	protected static final Options options;

	static {
		options = new Options();

		options.addOption("h", "help", false, "Print this help.");
		options.addOption("po", "public-output-file", true, "Path to the public key output file, default publickey.xml.");
		options.addOption("vo", "private-output-file", true, "Path to the private key output file, default privatekey.xml.");
		options.addOption("s", "size", true, "The key size in bits, default " + DEFAULT_SIZE + ".");
	}

	public static void main(String[] args) {
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine config = parser.parse(options, args);

			if (config.hasOption("h")) {
				HelpFormatter help = new HelpFormatter();
				help.printHelp("java " + KeyGenerator.class.getSimpleName(), options);
				return;
			}

			int size = DEFAULT_SIZE;
			if (config.hasOption("s")) {
				try {
					size = Integer.parseInt(config.getOptionValue("s"));
				}
				catch (NumberFormatException e) {
					log.error("Invalid key size: " + config.getOptionValue("s") + ", trying default: " + size);
				}
			}

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(size);

			KeyPair pair = generator.generateKeyPair();

			File privateFile = new File(config.getOptionValue("po", "privatekey.xml"));
			PersistenceManager.save(pair.getPrivate(), privateFile);
			log.info("Saved private key to: " + privateFile.getAbsolutePath());

			File publicFile = new File(config.getOptionValue("vo", "publickey.xml"));
			PersistenceManager.save(pair.getPublic(), publicFile);
			log.info("Saved public key to: " + publicFile.getAbsolutePath());
		}
		catch (ParseException e) {
			log.error("Error parsing command line options: " + e);
		}
		catch (NoSuchAlgorithmException e) {
			log.error("Unable to find RSA algorithm support.");
		}
		catch (IOException e) {
			log.error("Error saving file: " + e);
		}
		catch (RuntimeException e) {
			log.error(e.getMessage());
		}
	}
}
