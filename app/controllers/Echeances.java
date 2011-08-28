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
import utils.LigneBudgetUtils;
import utils.PlanningEcheanceUtils;
import controllers.utils.SessionUtil;

public class Echeances extends Controller {

	private static final String CALENDAR_MODE = "CAL";
	private static final String LIST_MODE = "LIST";
	private static final String DEFAULT_MODE = CALENDAR_MODE;

	@Before
	static void defaultData() {
		List<Compte> allComptes = Compte.findAll();
		renderArgs.put("allComptes", allComptes);
	}

	public static void index(Long compteId, Long echeanceId) {
		Logger.debug(">> Echeances.index >> compteId=%d | echeanceId=%d", compteId, echeanceId);
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
			notFoundIfNull(compte);
		} else {
			compte = Compte.find("").first();
			notFoundIfNull(compte);
			index(compte.id, echeanceId);
		}

		// Recuperation dans la session du mode de visualisation des echeances
		String mode = SessionUtil.getSessionParam(SessionUtil.SESSION_PARAM_ECHEANCES_MODE, true, DEFAULT_MODE);
		renderArgs.put(SessionUtil.SESSION_PARAM_ECHEANCES_MODE, mode);
		Logger.debug("Visualisation mode = %s", mode);

		if (LIST_MODE.equals(mode)) {
			Logger.debug("Display list mode");
			list(compteId, echeanceId);
		} else {
			Logger.debug("display calendar mode");
			List<Echeance> echeances = Echeance.find("compte.id=? ORDER BY type ASC, description ASC", compte.id).fetch();
			render(compte, echeances);
		}
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
			if (echeance.id != null && echeance.id > 0) {
				String titre = "Editer";
				List<Compte> comptes = Compte.findAll();
				List<Tag> tags = Tag.findAll();
				render(titre, echeance, comptes, tags);
			} else {
				String titre = "Ajouter";
				List<Compte> comptes = Compte.findAll();
				List<Tag> tags = Tag.findAll();
				render("Echeances/editer.html", titre, echeance, comptes, tags);
			}
		}
		echeance.save();

		LigneBudgetUtils.refreshAll();

		index(null, null);
	}

	public static void calendrier(Long compteId, Date date) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Echeance> allEcheances = Echeance.find("compte.id=?", compte.id).fetch();
		PlanningEcheanceUtils.compute(date, allEcheances);

		Calendrier calendrier = PlanningEcheanceUtils.buildCalendrier(date);
		List<models.PlanningEcheance> plannings = models.PlanningEcheance.find("select pe from PLANNINGECHEANCE pe join pe.echeance e where e.compte.id=?", 1L).fetch();
		for (models.PlanningEcheance planningEcheance : plannings) {
			for (Semaine semaine : calendrier.semaines) {
				for (Jour jour : semaine.jours) {
					if (PlanningEcheanceUtils.isEqual(jour.date, planningEcheance.date)) {
						jour.echeances.add(planningEcheance.echeance);
						Logger.debug("ajout de l'écheance %s au jour %s", planningEcheance.echeance, jour.date);
					}
				}
			}
		}

		render(compte, calendrier);
	}

	public static void list(Long compteId, Long echeanceId) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		List<Echeance> echeances = Echeance.find("compte.id=?", compte.id).fetch();

		Echeance echeanceSelectionnee = null;
		if (echeanceId != null) {
			echeanceSelectionnee = Echeance.findById(echeanceId);
			notFoundIfNull(echeanceSelectionnee);

		}

		renderArgs.put(SessionUtil.SESSION_PARAM_ECHEANCES_MODE, LIST_MODE);
		render("Echeances/list.html", compte, echeances, echeanceSelectionnee);
	}
}
