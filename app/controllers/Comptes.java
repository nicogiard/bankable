package controllers;

import java.util.Date;
import java.util.List;

import models.Compte;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import models.User;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import controllers.utils.Pagination;

@With(Secure.class)
public class Comptes extends Controller {

	private static final Pagination comptesPagination = new Pagination();

	@Before
	static void defaultData() {
		User connectedUser = Security.connectedUser();

		List<Compte> comptes = Compte.find("user=?", connectedUser).fetch();
		renderArgs.put("comptes", comptes);

		Double total = Compte.find("SELECT sum(compte.solde) FROM Compte compte WHERE user=?", connectedUser).first();
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

	public static void index(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
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

	public static void filtrer(Long compteId, String libelle, String tiers, Float montant, String tag, Date date) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
			notFoundIfNull(compte);
		}

		if (compte != null) {
			StringBuilder sbCountOperations = new StringBuilder("select count(operation) from Operation operation where operation.compte.id=?");
			StringBuilder sbOperations = new StringBuilder("compte.id=?");

			if (StringUtils.isNotBlank(libelle)) {
				sbCountOperations.append(" AND operation.libelle LIKE '%").append(libelle).append("%'");
				sbOperations.append(" AND libelle LIKE '%").append(libelle).append("%'");
			}
			if (StringUtils.isNotBlank(tiers)) {
				sbCountOperations.append(" AND operation.tiers.designation LIKE '%").append(tiers).append("%'");
				sbOperations.append(" AND tiers.designation LIKE '%").append(tiers).append("%'");
			}
			if (montant != null) {
				sbCountOperations.append(" AND operation.montant=").append(montant).append("");
				sbOperations.append(" AND montant=").append(montant).append("");
			}
			if (StringUtils.isNotBlank(tag)) {
				sbCountOperations.append(" AND operation.tag LIKE '%").append(tag).append("%'");
				sbOperations.append(" AND tag LIKE '%").append(tag).append("%'");
			}
			if (date != null) {
				sbCountOperations.append(" AND operation.date='").append(date).append("'");
				sbOperations.append(" AND date='").append(date).append("'");
			}

			sbOperations.append(" ORDER BY date DESC, id DESC");

			Logger.debug("request CountOperations with filter : %s", sbCountOperations.toString());
			Logger.debug("request Operations with filter : %s", sbCountOperations.toString());

			Long countOperation = Compte.find(sbCountOperations.toString(), compte.id).first();
			comptesPagination.setElementCount(countOperation);

			List<Operation> operations = Operation.find(sbOperations.toString(), compte.id).fetch(comptesPagination.getPage(), comptesPagination.getPageSize());

			Pagination pagination = comptesPagination;
			render("Comptes/index.html", compte, operations, pagination, libelle, montant, tag, date);
		}
		render("Comptes/index.html", compte);
	}

	public static void ajouter() {
		String titre = "Ajouter";
		render("Comptes/editer.html", titre);
	}

	public static void editer(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
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

		User connectedUser = Security.connectedUser();
		if (compte.user.id != connectedUser.id) {
			forbidden("Vous n'êtes pas le propriétaire de ce compte");
		}

		compte.save();
		index(compte.id);
	}

	public static void rapprocher(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		List<Operation> operations = Operation.find("compte.id=? AND etat=?", compte.id, EEtatOperation.POINTEE).fetch();
		for (Operation operation : operations) {
			operation.etat = EEtatOperation.RAPPROCHEE;
			operation.save();
		}
		Comptes.index(compte.id);
	}

	public static void vider(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		// Update du Solde du compte a partir de la première opération
		Operation operation = Operation.find("compte.id=? ORDER BY date ASC, id ASC", compte.id).first();
		if (operation.type == ETypeOperation.CREDIT) {
			compte.solde = compte.solde - operation.montant;
		} else {
			compte.solde = compte.solde + operation.montant;
		}
		compte.save();

		JPA.em().createNativeQuery("DELETE FROM Operation_Tags WHERE operation_id in (SELECT id FROM Operation WHERE compte_id=?)").setParameter(1, compteId).executeUpdate();
		JPA.em().createNativeQuery("DELETE FROM Operation WHERE compte_id=?").setParameter(1, compteId).executeUpdate();

		Comptes.index(compteId);
	}
}