package jobs;

import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;

@On("0 0 4 * * ?")
@OnApplicationStart
public class EcheanceJob extends Job {
	public final void doJob() {
		// List<Echeance> echeances = Echeance.findAll();
		// PlanningEcheance.compute(new Date(), echeances);
	}
}