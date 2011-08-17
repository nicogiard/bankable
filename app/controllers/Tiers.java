package controllers;

import java.util.List;

import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Controller;

/**
 * 
 * @author f.meurisse
 */
public class Tiers extends Controller {

	public static void index() {
		List<models.Tiers> tiersListe = models.Tiers.all().fetch();
		render(tiersListe);
	}

	public static void ajouter() {
		List<models.Tiers> tiersListe = models.Tiers.all().fetch();
		models.Tiers tiers = new models.Tiers();
		render("Tiers/index.html", tiersListe, tiers);
	}

	public static void editer(Long tiersId) {
		List<models.Tiers> tiersListe = models.Tiers.all().fetch();
		models.Tiers tiers = models.Tiers.findById(tiersId);
		notFoundIfNull(tiers);
		render("Tiers/index.html", tiersListe, tiers);
	}

	public static void enregistrer(@Required @Valid models.Tiers tiers) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			List<models.Tiers> tiersListe = models.Tiers.all().fetch();
			render("Tiers/index.html", tiersListe, tiers);
		}

		tiers.save();
		flash.success("Le tiers a été enregistrée avec succès");

		if (params._contains("valid")) {
			ajouter();
		}
		if (params._contains("validQuit")) {
			index();
		}
	}
	
	public static void supprimer(Long tiersId) {
		models.Tiers tiers = models.Tiers.findById(tiersId);
		notFoundIfNull(tiers);
		tiers.delete();
		flash.success("Le tiers a été supprimé avec succès");
		index();
	}
}
