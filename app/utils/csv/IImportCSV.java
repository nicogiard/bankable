package utils.csv;

import java.io.File;

import models.Compte;

public interface IImportCSV {
	void importer(File fichier, Compte compte);
}
