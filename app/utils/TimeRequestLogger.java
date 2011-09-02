package utils;

import play.Logger;

public class TimeRequestLogger {
	public static void log(String requestAction, long result) {
		Logger.info("request [%s] time spent : %s ms", requestAction, result);
	}
}
