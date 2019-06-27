package com.company.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class PropertyManager {

	private static final Logger LOGGER = LogManager.getLogger();

	public static Properties API_PROPERTIES;

	private final static String APP_PROPERTIES_NAME = "config.properties";

	static {
		API_PROPERTIES = loadPropertyFile(APP_PROPERTIES_NAME);
	}

	/**
	 * Load property file.
	 *
	 * @param filename
	 *            the filename
	 * @return the properties
	 */
	private static Properties loadPropertyFile(String filename) {
		Properties properties = new Properties();
		InputStream inputStream = PropertyManager.class.getClassLoader().getResourceAsStream(filename);
		try {
			properties.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			properties = null;
			LOGGER.error("exception is reading file: [{}]", filename, e);
			Assert.fail("property file could not be read");
		}
		return properties;
	}

}
