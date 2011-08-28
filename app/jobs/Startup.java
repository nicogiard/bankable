package jobs;

import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Startup extends Job<Void> {

	@Override
	public void doJob() throws Exception {

		Logger.info("System Information:");
		Logger.info("  Timezone: " + System.getProperty("user.timezone"));
		Logger.info("  Java Version: " + System.getProperty("java.runtime.version"));
		Logger.info("  Java Vendor: " + System.getProperty("java.specification.vendor"));
		Logger.info("  JVM Version: " + System.getProperty("java.vm.version"));
		Logger.info("  JVM Vendor: " + System.getProperty("java.vm.vendor"));
		Logger.info("  JVM Runtime: " + System.getProperty("java.vm.name"));
		Logger.info("  Username: " + System.getProperty("user.name"));
		Logger.info("  Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		Logger.info("  Architecture: : " + System.getProperty("os.arch"));

		Logger.info("Runtime Information:");
		Logger.info("  Version: " + utils.Version.version);
		Logger.info("  Build Number: " + utils.Version.buildNumber);
		Logger.info("  Build Date: " + utils.Version.buildDate);
	}
}
