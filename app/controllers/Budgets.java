package controllers;

import java.util.Date;
import java.util.List;

import models.Budget;
import models.Compte;
import models.LigneBudget;
import models.Tag;
import models.User;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.LigneBudgetUtils;

@With(Secure.class)
public class Budgets extends Controller {
	@Before
	static void defaultData() {
		User connectedUser = Security.connectedUser();

		List<Compte> allComptes = Compte.find("user=?", connectedUser).fetch();
		renderArgs.put("allComptes", allComptes);
	}

	public static void index(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
			notFoundIfNull(compte);
		} else {
			compte = Compte.find("user=?", connectedUser).first();
			notFoundIfNull(compte);
			index(compte.id);
		}

		Budget budget = Budget.find("compte.id=?", compte.id).first();

		Float totalActuel = 0F;
		Float totalPrevisionnel = 0F;

		if (budget != null) {
			for (LigneBudget ligneBudget : budget.lignes) {
				totalActuel = totalActuel + ligneBudget.montantActuel;
				totalPrevisionnel = totalPrevisionnel + ligneBudget.montantEcheance + ligneBudget.montantManuel;
			}
		}
		Date actualMonth = new Date();

		render(compte, actualMonth, budget, totalActuel, totalPrevisionnel);
	}

	public static void ajouterLigne() {
		User connectedUser = Security.connectedUser();

		List<Budget> budgets = Budget.find("compte.user=?", connectedUser).fetch();
		List<Tag> tags = Tag.findAll();

		String titre = "Ajouter";
		render("Budgets/editerLigne.html", titre, budgets, tags);
	}

	public static void editerLigne(Long ligneBudgetId) {
		User connectedUser = Security.connectedUser();

		List<Budget> budgets = Budget.find("compte.user=?", connectedUser).fetch();
		List<Tag> tags = Tag.findAll();

		LigneBudget ligneBudget = LigneBudget.findById(ligneBudgetId);
		notFoundIfNull(ligneBudget);

		String titre = "Editer";
		render(titre, ligneBudget, budgets, tags);
	}

	public static void enregistrerLigne(@Required @Valid LigneBudget ligneBudget) {
		if (validation.hasErrors()) {
			if (ligneBudget.id != null && ligneBudget.id > 0) {
				String titre = "Editer";
				List<Budget> budgets = Budget.findAll();
				List<Tag> tags = Tag.findAll();
				render("Budgets/editerLigne.html", titre, ligneBudget, budgets, tags);
			} else {
				String titre = "Ajouter";
				List<Budget> budgets = Budget.findAll();
				List<Tag> tags = Tag.findAll();
				render("Budgets/editerLigne.html", titre, ligneBudget, budgets, tags);
			}
		}

		User connectedUser = Security.connectedUser();
		if (ligneBudget.budget.compte.user.id != connectedUser.id) {
			forbidden("Vous n'êtes pas le propriétaire de ce compte");
		}

		if (ligneBudget.montantManuel == null) {
			ligneBudget.montantManuel = 0F;
		}

		LigneBudgetUtils.refresh(ligneBudget);

		flash.success("La ligne a été créée avec succès");

		index(null);
	}

	public static void supprimerLigne(Long ligneBudgetId) {
		User connectedUser = Security.connectedUser();

		LigneBudget ligneBudget = LigneBudget.find("id=? AND budget.compte.user=?", ligneBudgetId, connectedUser).first();
		notFoundIfNull(ligneBudget);

		ligneBudget.delete();

		flash.success("La ligne a été supprimée avec succès");

		index(null);
	}

	public static void refresh() {
		LigneBudgetUtils.refreshAll();
		index(null);
	}
}
