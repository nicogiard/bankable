package controllers;

import java.util.List;

import models.Compte;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import controllers.utils.Pagination;

public class Comptes extends Controller {

	private static final Pagination comptesPagination = new Pagination();

	@Before
	static void defaultData() {
		List<Compte> comptes = Compte.findAll();
		renderArgs.put("comptes", comptes);

		Double total = Compte.find("select sum(compte.solde) from Compte compte").first();
		if (total == null) {
			total = 0D;
		}
		renderArgs.put("total", total);

		// Récupération de la pagination
		// Dans l'ordre params puis session
		String pageParam = params.get("page");
		if (pageParam == null) {
			pageParam = session.get("comptesPagination.page");
		}
		if (pageParam == null) {
			pageParam = "1";
		}
		comptesPagination.setPage(Integer.valueOf(pageParam));
		session.put("comptesPagination.page", pageParam);
	}

	public static void resume(Long compteId) {
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
			notFoundIfNull(compte);
		}

		if (compte != null) {
			Long countOperation = Compte.find("select count(operation) from Operation operation where operation.compte.id=?", compte.id).first();
			comptesPagination.setElementCount(countOperation);

			List<Operation> operations = Operation.find("compte.id=? ORDER BY date DESC, id DESC", compte.id).fetch(comptesPagination.getPage(), comptesPagination.getPageSize());

			Pagination pagination = comptesPagination;
			render(compte, operations, pagination);
		}
		render(compte);
	}

	public static void ajouter() {
		String titre = "Ajouter";
		render("Comptes/editer.html", titre);
	}

	public static void editer(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		String titre = "Editer";

		render(titre, compte);
	}

	public static void enregistrer(@Required @Valid Compte compte) {
		if (validation.hasErrors()) {
			if (compte.id != null && compte.id > 0) {
				String titre = "Editer";
				render(titre, compte);
			} else {
				String titre = "Ajouter";
				render("Comptes/editer.html", titre);
			}
		}
		compte.save();
		resume(compte.id);
	}

	public static void rapprocher(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Operation> operations = Operation.find("compte.id=? AND etat=?", compte.id, EEtatOperation.POINTEE).fetch();
		for (Operation operation : operations) {
			operation.etat = EEtatOperation.RAPPROCHEE;
			operation.save();
		}
		Comptes.resume(compte.id);
	}

	public static void vider(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		// Update du Solde du compte a partir de la première opération
		Operation operation = Operation.find("compte.id=? ORDER BY date ASC, id ASC", compte.id).first();
		if (operation.type == ETypeOperation.CREDIT) {
			operation.compte.solde = operation.compte.solde + operation.montant;
		} else {
			operation.compte.solde = operation.compte.solde - operation.montant;
		}
		compte.save();

		JPA.em().createNativeQuery("DELETE FROM Operation_Tags WHERE operation_id in (SELECT id FROM Operation WHERE compte_id=?)").setParameter(1, compteId).executeUpdate();
		JPA.em().createNativeQuery("DELETE FROM Operation WHERE compte_id=?").setParameter(1, compteId).executeUpdate();

		Comptes.resume(compteId);
	}
}