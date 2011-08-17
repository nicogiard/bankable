package controllers;

import java.util.Date;
import java.util.List;

import models.Calendrier;
import models.Compte;
import models.Echeance;
import models.Jour;
import models.Semaine;
import models.Tag;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import utils.PlanningEcheance;

public class Echeances extends Controller {

	@Before
	static void defaultData() {
	}

	public static void index(Long compteId) {
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
		} else {
			compte = Compte.find("").first();
			index(compte.id);
		}

		List<Compte> allComptes = Compte.findAll();

		List<Echeance> echeances = Echeance.find("compte.id=? ORDER BY type ASC, description ASC", compte.id).fetch();
		render(allComptes, compte, echeances);
	}

	public static void ajouter() {
		String titre = "Ajouter";

		List<Compte> comptes = Compte.findAll();
		List<Tag> tags = Tag.findAll();
		render("Echeances/editer.html", titre, comptes, tags);
	}

	public static void editer(Long echeanceId) {
		String titre = "Editer";

		List<Compte> comptes = Compte.findAll();
		List<Tag> tags = Tag.findAll();

		Echeance echeance = Echeance.findById(echeanceId);
		notFoundIfNull(echeance);

		render(titre, echeance, comptes, tags);
	}

	public static void enregistrer(@Required @Valid Echeance echeance) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			if (echeance.id != null && echeance.id > 0) {
				editer(echeance.id);
			} else {
				ajouter();
			}
		}
		echeance.save();
		index(null);
	}

	public static void calendrier(Long compteId, Date date) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Echeance> allEcheances = Echeance.find("compte.id=?", compte.id).fetch();
		PlanningEcheance.compute(date, allEcheances);

		Calendrier calendrier = PlanningEcheance.buildCalendrier(date);
		List<models.PlanningEcheance> plannings = models.PlanningEcheance.find("select pe from PlanningEcheance pe join pe.echeance e where e.compte.id=?", 1L).fetch();
		for (models.PlanningEcheance planningEcheance : plannings) {
			for (Semaine semaine : calendrier.semaines) {
				for (Jour jour : semaine.jours) {
					if (PlanningEcheance.isEqual(jour.date, planningEcheance.date)) {
						jour.echeances.add(planningEcheance.echeance);
						Logger.debug("ajout de l'écheance %s au jour %s", planningEcheance.echeance, jour.date);
					}
				}
			}
		}

		render(compte, calendrier);
	}
}
