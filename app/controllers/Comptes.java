package controllers;

import java.util.List;

import models.Compte;
import models.Devise;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;

public class Comptes extends Controller {

	public static final Long OPERATION_PAR_PAGE = 15L;

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

	public static void resume(Long compteId, Long pageNumber) {
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
			notFoundIfNull(compte);
		}

		if (compte != null) {
			Long page = (pageNumber == null || pageNumber == 0) ? 1L : pageNumber;
			Long countOperation = Compte.find("select count(operation) from Operation operation where operation.compte.id=?", compte.id).first();
			Double tempNumberOfPage = countOperation.doubleValue() / OPERATION_PAR_PAGE;
			Long numberOfPage = tempNumberOfPage < 1 ? 1L : Math.round(tempNumberOfPage);

			Logger.debug("page : %s | count : %s | numberOfPage : %s", page.toString(), countOperation.toString(), numberOfPage.toString());

			List<Operation> operations = Operation.find("compte.id=? ORDER BY date DESC, id DESC", compte.id).fetch(page.intValue(), OPERATION_PAR_PAGE.intValue());

			render(compte, operations, countOperation, numberOfPage, page);
		}

		render(compte);
	}

	public static void ajouter() {
		String titre = "Ajouter";
		List<Devise> devises = Devise.findAll();
		render("Comptes/editer.html", titre, devises);
	}

	public static void editer(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		String titre = "Editer";
		List<Devise> devises = Devise.findAll();

		render(titre, compte, devises);
	}

	public static void enregistrer(@Required @Valid Compte compte) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			if (compte.id != null && compte.id > 0) {
				editer(compte.id);
			} else {
				ajouter();
			}
		}
		compte.save();
		resume(compte.id, null);
	}

	public static void rapprocher(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Operation> operations = Operation.find("compte.id=? AND etat=?", compte.id, EEtatOperation.POINTEE).fetch();
		for (Operation operation : operations) {
			operation.etat = EEtatOperation.RAPPROCHEE;
			operation.save();
		}
		Comptes.resume(compte.id, null);
	}

	public static void vider(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		// Update du Solde du compte a partir de la première opération
		Operation operation = Operation.find("compte.id=? ORDER BY date ASC, id ASC", compte.id).first();
		if (operation.type == ETypeOperation.CREDIT) {
			operation.solde = operation.compte.solde + operation.montant;
			operation.compte.solde = operation.solde;
		} else {
			operation.solde = operation.compte.solde - operation.montant;
			operation.compte.solde = operation.solde;
		}
		compte.save();

		JPA.em().createNativeQuery("DELETE FROM Operation_Tags WHERE operation_id in (SELECT id FROM Operation WHERE compte_id=?)").setParameter(1, compteId).executeUpdate();
		JPA.em().createNativeQuery("DELETE FROM Operation WHERE compte_id=?").setParameter(1, compteId).executeUpdate();

		Comptes.resume(compteId, null);
	}
}