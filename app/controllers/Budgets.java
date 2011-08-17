package controllers;

import java.util.Date;
import java.util.List;

import models.Budget;
import models.Compte;
import models.LigneBudget;
import models.Tag;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;

public class Budgets extends Controller {
	@Before
	static void defaultData() {
		List<Compte> allComptes = Compte.findAll();

		renderArgs.put("allComptes", allComptes);
	}

	public static void index(Long compteId) {
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
			notFoundIfNull(compte);
		} else {
			compte = Compte.find("").first();
			notFoundIfNull(compte);
			index(compte.id);
		}

		Budget budget = Budget.find("compte.id=?", compte.id).first();

		Float totalActuel = 0F;
		Float totalPrevisionnel = 0F;

		if (budget != null) {
			for (LigneBudget ligneBudget : budget.lignes) {
				totalActuel += ligneBudget.montantActuel;
				totalPrevisionnel += ligneBudget.montantEcheance + ligneBudget.montantManuel;
			}
		}
		Date actualMonth = new Date();

		render(compte, actualMonth, budget, totalActuel, totalPrevisionnel);
	}

	public static void ajouterLigne() {
		String titre = "Ajouter";

		List<Budget> budgets = Budget.findAll();
		List<Tag> tags = Tag.findAll();
		render("Budgets/editerLigne.html", titre, budgets, tags);
	}

	public static void editerLigne(Long ligneBudgetId) {
		String titre = "Editer";

		List<Budget> budgets = Budget.findAll();
		List<Tag> tags = Tag.findAll();

		LigneBudget ligneBudget = LigneBudget.findById(ligneBudgetId);
		notFoundIfNull(ligneBudget);

		render(titre, ligneBudget, budgets, tags);
	}

	public static void enregistrerLigne(@Required @Valid LigneBudget ligneBudget) {
		if (validation.hasErrors()) {
			params.flash();
			validation.keep();
			if (ligneBudget.id != null && ligneBudget.id > 0) {
				editerLigne(ligneBudget.id);
			} else {
				ajouterLigne();
			}
		}
		// TODO : calculer le montantEcheance
		// List<Echeance> echeances = Echeance.find("tag.id=?", ligneBudget.tag.id).fetch();

		ligneBudget.montantEcheance = 0F;
		ligneBudget.montantActuel = 0F;
		ligneBudget.save();

		flash.success("La ligne a été créée avec succès");

		index(null);
	}

	public static void supprimerLigne(Long ligneBudgetId) {
		LigneBudget ligneBudget = LigneBudget.findById(ligneBudgetId);
		notFoundIfNull(ligneBudget);

		ligneBudget.delete();

		flash.success("La ligne a été supprimée avec succès");

		index(null);
	}
}
