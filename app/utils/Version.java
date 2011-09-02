package utils;

import play.Play;

public class Version {
	public static final String version = Play.configuration.getProperty("application.version", "please set [application.version] in you application.conf");
	public static final String buildNumber = Play.configuration.getProperty("application.buildNumber", "please set [application.buildNumber] in you application.conf");
	public static final String buildDate = Play.configuration.getProperty("application.buildDate", "please set [application.buildDate] in you application.conf");
}
