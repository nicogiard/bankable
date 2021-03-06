package controllers;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import models.Compte;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import models.OperationImport;
import models.Tag;
import models.User;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.LigneBudgetUtils;
import utils.OperationUtils;
import utils.csv.ImportCSVCaisseEpargne;
import controllers.utils.Pagination;

@With(Secure.class)
public class Operations extends Controller {

	private static final Pagination operationsPagination = new Pagination();

	@Before
	static void defaultData() {
		// Récupération de la pagination
		// Dans l'ordre params puis session
		String pageParam = params.get("page");
		if (pageParam == null) {
			pageParam = session.get("operationsPagination.page");
		}
		if (pageParam == null) {
			pageParam = "1";
		}
		operationsPagination.setPage(Integer.valueOf(pageParam));
		session.put("operationsPagination.page", pageParam);
	}

	public static void ajouter(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		List<Tag> allTags = Tag.find("user=? ORDER BY nom ASC", connectedUser).fetch();
		List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();

		String titre = "Ajouter";
		render("Operations/editer.html", titre, compte, allTags, allTiers);
	}

	public static void editer(Long compteId, Long operationId, String libelle, String tiers, Float montant, String tag, Date date) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		Operation operation = Operation.findById(operationId);
		notFoundIfNull(operation);

		List<Tag> allTags = Tag.find("user=? ORDER BY nom ASC", connectedUser).fetch();
		List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();

		String titre = "Editer";
		render(titre, compte, operation, allTags, allTiers, libelle, tiers, montant, tag, date);
	}

	public static void enregistrer(@Required @Valid Operation operation, String tags, String libelle, String tiers, Float montant, String tag, Date date) {
		User connectedUser = Security.connectedUser();
		if (validation.hasErrors()) {
			if (operation.id != null && operation.id > 0) {
				String titre = "Editer";
				Compte compte = operation.compte;
				notFoundIfNull(compte);

				List<Tag> allTags = Tag.find("user=? ORDER BY nom ASC", connectedUser).fetch();
				List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();
				render("Operations/editer.html", titre, compte, operation, allTags, allTiers);
			} else {
				String titre = "Ajouter";
				Compte compte = operation.compte;
				notFoundIfNull(compte);

				List<Tag> allTags = Tag.find("user=? ORDER BY nom ASC", connectedUser).fetch();
				List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();
				render("Operations/editer.html", titre, compte, operation, allTags, allTiers);
			}
		}

		if (operation.compte.user.id != connectedUser.id) {
			forbidden("Vous n'êtes pas le propriétaire de ce compte");
		}

		Float oldMontant = operation.getMontantFromDatabase();

		if (!oldMontant.equals(operation.montant)) {
			Logger.debug("Le montant de l'opération d'id [%s] a changé : %s -> %s", operation.id, operation.montant, oldMontant);
			if (operation.type == ETypeOperation.DEBIT) {
				operation.compte.solde = (operation.compte.solde + oldMontant) - operation.montant;
			} else {
				operation.compte.solde = (operation.compte.solde - oldMontant) + operation.montant;
			}
		}

		operation.tags.clear();
		OperationUtils.extractTags(operation, tags);

		operation.compte.save();
		operation.save();

		LigneBudgetUtils.refreshAll();

		if (params._contains("valid")) {
			flash.success("L'opération a été enregistrée avec succès");
			ajouter(operation.compte.id);
		}
		if (params._contains("validQuit")) {
			Comptes.index(operation.compte.id, libelle, tiers, montant, tag, date);
		}
	}

	public static void supprimer(Long compteId, Long operationId, String libelle, String tiers, Float montant, String tag, Date date) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		Operation operation = Operation.find("id=? AND compte.id=? AND compte.user=?", operationId, compte.id, connectedUser).first();
		notFoundIfNull(operation);

		if (ETypeOperation.DEBIT == operation.type) {
			compte.solde = compte.solde + operation.montant;
		} else {
			compte.solde = compte.solde - operation.montant;
		}

		compte.save();
		operation.delete();

		LigneBudgetUtils.refreshAll();

		Comptes.index(compte.id, libelle, tiers, montant, tag, date);
	}

	public static void pointer(Long compteId, Long operationId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		Operation operation = Operation.find("id=? AND compte.id=? AND compte.user=?", operationId, compte.id, connectedUser).first();
		notFoundIfNull(operation);

		if (operation.etat == EEtatOperation.NONPOINTEE) {
			operation.etat = EEtatOperation.POINTEE;
		} else {
			operation.etat = EEtatOperation.NONPOINTEE;
		}
		operation.save();
	}

	public static void importer(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);
		render(compte);
	}

	public static void importUpload(Long compteId, File fichier) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		if (fichier != null && fichier.exists()) {
			if (fichier.getName().endsWith(".csv") && "application/octet-stream".equals(new MimetypesFileTypeMap().getContentType(fichier))) {
				// TODO : faire du multi opérateur d'import
				new ImportCSVCaisseEpargne().importer(fichier, compte);
			} else {
				validation.addError("file.nocsv", "Le fichier n'est pas de type csv");
			}
		} else {
			validation.addError("file.unknown", "Le fichier n'existe pas");
		}

		List<OperationImport> operationsImport = OperationImport.find("ORDER BY date ASC, id DESC").fetch();
		render("Operations/importer.html", compte, operationsImport);
	}

	public static void importerOperations(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		List<OperationImport> operationsImport = OperationImport.find("ORDER BY id DESC").fetch();
		notFoundIfNull(operationsImport);

		for (OperationImport operationImport : operationsImport) {
			Operation operation = Operation.copyFromImport(operationImport);
			operation.compte.save();
			operation.save();
		}

		JPA.em().createNativeQuery("DELETE FROM OPERATIONIMPORT WHERE compte_id=?").setParameter(1, compte.id).executeUpdate();

		LigneBudgetUtils.refreshAll();

		Comptes.index(compte.id, null, null, null, null, null);
	}

	public static void operationsSansTag(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
			notFoundIfNull(compte);
		}

		if (compte != null) {
			Long countOperation = Compte.find("SELECT count(operation) FROM Operation operation WHERE operation.compte=? and operation.tags IS EMPTY", compte).first();
			operationsPagination.setElementCount(countOperation);

			List<Operation> operations = Operation.find("SELECT operation FROM Operation operation WHERE operation.compte=? AND operation.tags IS EMPTY ORDER BY operation.date DESC, operation.id DESC", compte).fetch(operationsPagination.getPage(),
					operationsPagination.getPageSize());

			Pagination pagination = operationsPagination;
			render(compte, operations, pagination);
		}
		render(compte);
	}
}
