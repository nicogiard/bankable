package controllers;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import javax.activation.MimetypesFileTypeMap;

import models.Compte;
import models.EEtatOperation;
import models.ETypeOperation;
import models.Operation;
import models.OperationImport;
import models.Tag;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.Controller;

public class Operations extends Controller {

	static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	public static void ajouter(Long compteId) {
		String titre = "Ajouter";
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();

		render("Operations/editer.html", titre, compte, allTags);
	}

	public static void editer(Long compteId, Long operationId) {
		String titre = "Editer";
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		Operation operation = Operation.findById(operationId);
		notFoundIfNull(operation);

		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();

		render(titre, compte, operation, allTags);
	}

	public static void enregistrer(@Required @Valid Operation operation, String tags) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			if (operation.id != null && operation.id > 0) {
				editer(operation.compte.id, operation.id);
			} else {
				ajouter(operation.compte.id);
			}
		}

		// TODO : Est ce qu'il ne faudrait pas ici faire le recalcul de tous les soldes des opérations arrivant après ?
		if (operation.type == ETypeOperation.DEBIT) {
			operation.solde = operation.compte.solde - operation.montant;
			operation.compte.solde = operation.solde;
		} else {
			operation.solde = operation.compte.solde + operation.montant;
			operation.compte.solde = operation.solde;
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

		if (params._contains("valid")) {
			flash.success("L'opération a été enregistrée avec succès");
			ajouter(operation.compte.id);
		}
		if (params._contains("validQuit")) {
			Comptes.resume(operation.compte.id, null);
		}
	}

	public static void supprimer(Long compteId, Long operationId) {
		JPA.em().createNativeQuery("DELETE FROM Operation_Tags ot WHERE ot.operation_id=?").setParameter(1, operationId).executeUpdate();
		JPA.em().createNativeQuery("DELETE FROM Operation o WHERE o.id=?").setParameter(1, operationId).executeUpdate();
		// TODO : Est ce qu'il ne faudrait pas ici faire le recalcul de tous les soldes des opérations arrivant après ?
		Comptes.resume(compteId, null);
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
				try {
					List<String> lignes = FileUtils.readLines(fichier, "ISO-8859-1");

					// On commence a la ligne 4 car c'est comme ca dans le fichier de CE
					for (int i = 4; i < lignes.size(); i++) {
						String ligne = lignes.get(i);
						Scanner scan = new Scanner(ligne).useDelimiter(";");

						String date = scan.next();
						String numero = scan.next();
						String libelle = scan.next();
						String debit = scan.next();
						String credit = scan.next();
						String detail = scan.next();

						OperationImport operationImport = new OperationImport();
						try {
							operationImport.date = dateFormatter.parse(date);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						operationImport.numero = numero;
						operationImport.libelle = libelle;
						if (StringUtils.isNotBlank(debit)) {
							operationImport.montant = new Float(debit.replace(",", ".").replace("-", ""));
							operationImport.type = ETypeOperation.DEBIT;
						} else if (StringUtils.isNotBlank(credit)) {
							operationImport.montant = new Float(credit.replace(",", "."));
							operationImport.type = ETypeOperation.CREDIT;
						}
						operationImport.detail = detail;
						operationImport.compte = compte;
						operationImport.save();

						scan.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				operation.solde = operation.compte.solde - operation.montant;
				operation.compte.solde = operation.solde;
			} else {
				operation.solde = operation.compte.solde + operation.montant;
				operation.compte.solde = operation.solde;
			}
			operation.etat = EEtatOperation.NONPOINTEE;
			operation.compte.save();

			operation.save();
		}

		JPA.em().createNativeQuery("DELETE FROM OperationImport WHERE compte_id=?").setParameter(1, compte.id).executeUpdate();

		Comptes.resume(compte.id, null);
	}
}
