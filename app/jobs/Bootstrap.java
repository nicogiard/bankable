package jobs;

import models.Compte;
import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
	public void doJob() {
		if (Play.mode.equals(Mode.PROD)) {
			Play.ctxPath = "/bankable";
			Router.load("/bankable");
		}
		if (Compte.count() == 0) {
			Fixtures.loadModels("initial-data.yml");
		}
	}
}