package controllers;

import java.math.BigInteger;
import java.util.List;

import models.Compte;
import models.ETypeOperation;
import models.Operation;
import models.utils.ResumeCompte;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller {

	@Before
	static void defaultData() {
		List<Compte> comptes = Compte.findAll();

		Double total = Compte.find("select sum(compte.solde) from Compte compte").first();
		if (total == null) {
			total = 0D;
		}

		renderArgs.put("comptes", comptes);
		renderArgs.put("total", total);
	}

	public static void index() {
		ResumeCompte resume = new ResumeCompte();

		// TODO : ajouter les Informations Today/Yesterday

		render(resume);
	}

	public static void resume(Long compteId) {
		Compte compte = null;
		if (compteId != null && compteId > 0) {
			compte = Compte.findById(compteId);
		}
		notFoundIfNull(compte);

		ResumeCompte resume = new ResumeCompte();
		resume.operations = Operation.find("compte.id = ? ORDER BY id ASC", compte.id).fetch();

		Float solde = compte.solde;
		List<Operation> operationsPourBalance = Operation.find("compte.id = ? ORDER BY id DESC", compte.id).fetch();
		for (Operation operation : operationsPourBalance) {
			if (operation.type == ETypeOperation.CREDIT) {
				solde = solde - operation.montant;
			} else {
				solde = solde + operation.montant;
			}
			resume.soldes.put(operation.date, solde);
		}

		resume.countNoTag = (BigInteger) JPA.em().createNativeQuery("select count(o.id) from operation o where o.compte_id=? and o.id not in (select distinct operation_id from operation_tags)").setParameter(1, compte.id).getSingleResult();

		resume.tagsCredit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, count(ot.tag_id) as count from tag t inner join operation_tags ot on t.id = ot.tag_id inner join operation o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.CREDIT.toString()).getResultList();

		resume.tagsDebit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, count(ot.tag_id) as count from tag t inner join operation_tags ot on t.id = ot.tag_id inner join operation o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.DEBIT.toString()).getResultList();

		// TODO : ajouter les Informations Today/Yesterday

		render(compte, resume);
	}
}
