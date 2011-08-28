package controllers;

import java.io.File;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import models.Compte;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import models.OperationImport;
import models.Tag;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;
import utils.LigneBudgetUtils;
import utils.csv.ImportCSVCaisseEpargne;

public class Operations extends Controller {

	public static void ajouter(Long compteId) {
		String titre = "Ajouter";
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
		List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();

		render("Operations/editer.html", titre, compte, allTags, allTiers);
	}

	public static void editer(Long compteId, Long operationId) {
		String titre = "Editer";
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		Operation operation = Operation.findById(operationId);
		notFoundIfNull(operation);

		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
		List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();

		render(titre, compte, operation, allTags, allTiers);
	}

	public static void enregistrer(@Required @Valid Operation operation, String tags, Float oldMontant) {
		if (validation.hasErrors()) {
			if (operation.id != null && operation.id > 0) {
				String titre = "Editer";
				Compte compte = operation.compte;
				notFoundIfNull(compte);

				List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
				List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();
				render("Operations/editer.html", titre, compte, operation, allTags, allTiers);
			} else {
				String titre = "Ajouter";
				Compte compte = operation.compte;
				notFoundIfNull(compte);

				List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
				List<models.Tiers> allTiers = models.Tiers.find("ORDER BY designation ASC").fetch();
				render("Operations/editer.html", titre, compte, operation, allTags, allTiers);
			}
		}

		if (oldMontant == null) {
			oldMontant = 0F;
		}
		if (operation.type == ETypeOperation.DEBIT) {
			operation.compte.solde = (operation.compte.solde + oldMontant) - operation.montant;
		} else {
			operation.compte.solde = (operation.compte.solde - oldMontant) + operation.montant;
		}

		operation.tags.clear();
		if (StringUtils.isNotBlank(tags)) {
			String[] tabTags = tags.split(",");
			for (String stringTag : tabTags) {
				Tag tag = Tag.find("nom=?", stringTag.trim()).first();
				if (tag == null) {
					tag = new Tag();
					tag.nom = stringTag.trim();
					tag.save();
				}
				operation.tags.add(tag);
			}
		}

		operation.compte.save();
		operation.save();

		LigneBudgetUtils.refreshAll();

		if (params._contains("valid")) {
			flash.success("L'opération a été enregistrée avec succès");
			ajouter(operation.compte.id);
		}
		if (params._contains("validQuit")) {
			Comptes.index(operation.compte.id);
		}
	}

	public static void supprimer(Long compteId, Long operationId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);
		Operation operation = Operation.findById(operationId);
		notFoundIfNull(operation);

		if (ETypeOperation.DEBIT == operation.type) {
			compte.solde = compte.solde + operation.montant;
		} else {
			compte.solde = compte.solde - operation.montant;
		}

		compte.save();
		operation.delete();

		LigneBudgetUtils.refreshAll();

		Comptes.index(compteId);
	}

	public static void pointer(Long compteId, Long operationId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		Operation operation = Operation.findById(operationId);
		notFoundIfNull(operation);

		if (operation.etat == EEtatOperation.NONPOINTEE) {
			operation.etat = EEtatOperation.POINTEE;
		} else {
			operation.etat = EEtatOperation.NONPOINTEE;
		}
		operation.save();
	}

	public static void importer(Long compteId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);
		render(compte);
	}

	public static void importUpload(Long compteId, File fichier) {
		Compte compte = Compte.findById(compteId);
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
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<OperationImport> operationsImport = OperationImport.find("ORDER BY id DESC").fetch();
		notFoundIfNull(operationsImport);

		for (OperationImport operationImport : operationsImport) {
			Operation operation = new Operation();
			operation.date = operationImport.date;
			operation.numero = operationImport.numero;
			operation.libelle = operationImport.libelle;
			operation.montant = operationImport.montant;
			operation.type = operationImport.type;
			operation.detail = operationImport.detail;
			operation.compte = operationImport.compte;
			if (operation.type == ETypeOperation.DEBIT) {
				operation.compte.solde = operation.compte.solde - operation.montant;
			} else {
				operation.compte.solde = operation.compte.solde + operation.montant;
			}
			operation.etat = EEtatOperation.NONPOINTEE;
			operation.compte.save();

			operation.save();
		}

		JPA.em().createNativeQuery("DELETE FROM OPERATIONIMPORT WHERE compte_id=?").setParameter(1, compte.id).executeUpdate();

		LigneBudgetUtils.refreshAll();

		Comptes.index(compte.id);
	}
}
