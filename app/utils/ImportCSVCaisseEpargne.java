package utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import models.Compte;
import models.ETypeOperation;
import models.OperationImport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class ImportCSVCaisseEpargne implements IImportCSV {

	static DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	public void importer(File fichier, Compte compte) {
		try {
			List<String> lignes = FileUtils.readLines(fichier, "ISO-8859-1");

			// On commence a la ligne 4 car c'est comme ca dans le fichier de Caisse d'Epargne
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
			// TODO : gérer les erreurs
			e.printStackTrace();
		}
	}
}
