package controllers;

import java.math.BigInteger;
import java.util.List;

import models.Compte;
import models.ETypeOperation;
import models.Operation;
import models.PlanningEcheance;
import models.User;
import models.utils.ResumeCompte;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Application extends Controller {

	@Before
	static void defaultData() {
		User connectedUser = Security.connectedUser();

		List<Compte> comptes = Compte.find("user=?", connectedUser).fetch();
		renderArgs.put("comptes", comptes);

		Double total = Compte.find("select sum(compte.solde) from Compte compte WHERE user=?", connectedUser).first();
		if (total == null) {
			total = 0D;
		}
		renderArgs.put("total", total);
	}

	public static void index() {
		User connectedUser = Security.connectedUser();

		ResumeCompte resume = new ResumeCompte();

		DateTime today = new DateTime();
		MutableDateTime firstDayOfWeek = new MutableDateTime(today);
		firstDayOfWeek.setDayOfWeek(1);
		MutableDateTime lastDayOfWeek = new MutableDateTime(today);
		lastDayOfWeek.setDayOfWeek(7);

		resume.todayCredits = Operation.count("date=? AND type=? AND compte.user=?", today.toDate(), ETypeOperation.CREDIT, connectedUser);
		resume.todayDebits = Operation.count("date=? AND type=? AND compte.user=?", today.toDate(), ETypeOperation.DEBIT, connectedUser);
		resume.todayEcheances = PlanningEcheance.count("date=? AND echeance.compte.user=?", today.toDate(), connectedUser);

		resume.weekCredits = Operation.count("date BETWEEN ? AND ? AND type=? AND compte.user=?", firstDayOfWeek.toDate(), lastDayOfWeek.toDate(), ETypeOperation.CREDIT, connectedUser);
		resume.weekDebits = Operation.count("date BETWEEN ? AND ? AND type=? AND compte.user=?", firstDayOfWeek.toDate(), lastDayOfWeek.toDate(), ETypeOperation.DEBIT, connectedUser);
		resume.weekEcheances = PlanningEcheance.count("date BETWEEN ? AND ? AND echeance.compte.user=?", firstDayOfWeek.toDate(), lastDayOfWeek.toDate(), connectedUser);

		render(resume);
	}

	public static void resume(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null && compteId > 0) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		}
		notFoundIfNull(compte);

		ResumeCompte resume = new ResumeCompte();
		resume.operations = Operation.find("compte.id=? ORDER BY id ASC", compte.id).fetch();

		Float solde = compte.solde;
		List<Operation> operationsPourBalance = Operation.find("compte.id=? ORDER BY id DESC", compte.id).fetch();
		for (Operation operation : operationsPourBalance) {
			if (operation.type == ETypeOperation.CREDIT) {
				solde = solde - operation.montant;
			} else {
				solde = solde + operation.montant;
			}
			resume.soldes.put(operation.date, solde);
		}

		resume.countNoTagCredit = (BigInteger) JPA.em().createNativeQuery("select count(o.id) from OPERATION o where o.compte_id=? and o.type=? and o.id not in (select distinct operation_id from OPERATION_TAGS)").setParameter(1, compte.id)
				.setParameter(2, ETypeOperation.CREDIT.toString()).getSingleResult();
		resume.countNoTagDebit = (BigInteger) JPA.em().createNativeQuery("select count(o.id) from OPERATION o where o.compte_id=? and o.type=? and o.id not in (select distinct operation_id from OPERATION_TAGS)").setParameter(1, compte.id)
				.setParameter(2, ETypeOperation.DEBIT.toString()).getSingleResult();

		resume.tagsCredit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, count(ot.tag_id) as count from TAG t inner join OPERATION_TAGS ot on t.id = ot.tag_id inner join OPERATION o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.CREDIT.toString()).getResultList();

		resume.tagsDebit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, count(ot.tag_id) as count from TAG t inner join OPERATION_TAGS ot on t.id = ot.tag_id inner join OPERATION o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.DEBIT.toString()).getResultList();

		DateTime today = new DateTime();
		MutableDateTime firstDayOfWeek = new MutableDateTime(today);
		firstDayOfWeek.setDayOfWeek(1);
		MutableDateTime lastDayOfWeek = new MutableDateTime(today);
		lastDayOfWeek.setDayOfWeek(7);

		resume.todayCredits = Operation.count("compte.id=? AND date=? AND type=?", compte.id, today.toDate(), ETypeOperation.CREDIT);
		resume.todayDebits = Operation.count("compte.id=? AND date=? AND type=?", compte.id, today.toDate(), ETypeOperation.DEBIT);
		resume.todayEcheances = PlanningEcheance.count("echeance.compte.id=? AND date=?", compte.id, today.toDate());

		resume.weekCredits = Operation.count("compte.id=? AND date BETWEEN ? AND ? AND type=?", compte.id, firstDayOfWeek.toDate(), lastDayOfWeek.toDate(), ETypeOperation.CREDIT);
		resume.weekDebits = Operation.count("compte.id=? AND date BETWEEN ? AND ? AND type=?", compte.id, firstDayOfWeek.toDate(), lastDayOfWeek.toDate(), ETypeOperation.DEBIT);
		resume.weekEcheances = PlanningEcheance.count("echeance.compte.id=? AND date BETWEEN ? AND ?", compte.id, firstDayOfWeek.toDate(), lastDayOfWeek.toDate());

		render(compte, resume);
	}
}
