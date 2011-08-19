package utils;

import java.util.List;

import models.Budget;
import models.ETypeEcheance;
import models.ETypeOperation;
import models.LigneBudget;
import models.Operation;
import models.PlanningEcheance;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import play.db.jpa.JPA;

public class LigneBudgetUtils {

	public static void refreshAll() {
		DateTime today = new DateTime();
		MutableDateTime firstDayOfMonth = new MutableDateTime(today);
		firstDayOfMonth.setDayOfMonth(1);
		firstDayOfMonth.setTime(0);

		MutableDateTime lastDayOfMonth = new MutableDateTime(today);
		lastDayOfMonth.setDayOfMonth(1);
		lastDayOfMonth.addMonths(1);
		lastDayOfMonth.addDays(-1);
		lastDayOfMonth.setTime(0);

		List<Budget> budgets = Budget.findAll();
		for (Budget budget : budgets) {
			for (LigneBudget ligneBudget : budget.lignes) {
				// Calcul du montant Echeance
				ligneBudget.montantEcheance = 0F;
				List<PlanningEcheance> planningEcheances = PlanningEcheance.find("echeance.tag.id=? AND date BETWEEN ? AND ?", ligneBudget.tag.id, firstDayOfMonth.toDate(), lastDayOfMonth.toDate()).fetch();
				for (PlanningEcheance planningEcheance : planningEcheances) {
					if (planningEcheance.echeance.type == ETypeEcheance.CREDIT) {
						ligneBudget.montantEcheance = ligneBudget.montantEcheance + planningEcheance.echeance.montant;
					} else {
						ligneBudget.montantEcheance = ligneBudget.montantEcheance - planningEcheance.echeance.montant;
					}
				}

				// Calcul du montant Actuel
				ligneBudget.montantActuel = 0F;
				List<Operation> operations = JPA.em().createNativeQuery("SELECT o.* FROM operation o INNER JOIN operation_tags ot ON o.id=ot.operation_id WHERE ot.tag_id=? AND o.date BETWEEN ? AND ?", Operation.class)
						.setParameter(1, ligneBudget.tag.id).setParameter(2, firstDayOfMonth.toDate()).setParameter(3, lastDayOfMonth.toDate()).getResultList();
				for (Operation operation : operations) {
					if (operation.type == ETypeOperation.CREDIT) {
						ligneBudget.montantActuel = ligneBudget.montantActuel + operation.montant;
					} else {
						ligneBudget.montantActuel = ligneBudget.montantActuel - operation.montant;
					}
				}

				ligneBudget.save();
			}
		}
	}

	public static void refresh(LigneBudget ligneBudget) {
		DateTime today = new DateTime();
		MutableDateTime firstDayOfMonth = new MutableDateTime(today);
		firstDayOfMonth.setDayOfMonth(1);
		firstDayOfMonth.setTime(0);

		MutableDateTime lastDayOfMonth = new MutableDateTime(today);
		lastDayOfMonth.setDayOfMonth(1);
		lastDayOfMonth.addMonths(1);
		lastDayOfMonth.addDays(-1);
		lastDayOfMonth.setTime(0);

		// Calcul du montant Echeance
		ligneBudget.montantEcheance = 0F;
		List<PlanningEcheance> planningEcheances = PlanningEcheance.find("echeance.tag.id=? AND date BETWEEN ? AND ?", ligneBudget.tag.id, firstDayOfMonth.toDate(), lastDayOfMonth.toDate()).fetch();
		for (PlanningEcheance planningEcheance : planningEcheances) {
			if (planningEcheance.echeance.type == ETypeEcheance.CREDIT) {
				ligneBudget.montantEcheance = ligneBudget.montantEcheance + planningEcheance.echeance.montant;
			} else {
				ligneBudget.montantEcheance = ligneBudget.montantEcheance - planningEcheance.echeance.montant;
			}
		}

		// Calcul du montant Actuel
		ligneBudget.montantActuel = 0F;
		List<Operation> operations = JPA.em().createNativeQuery("SELECT o.* FROM operation o INNER JOIN operation_tags ot ON o.id=ot.operation_id WHERE ot.tag_id=? AND o.date BETWEEN ? AND ?", Operation.class).setParameter(1, ligneBudget.tag.id)
				.setParameter(2, firstDayOfMonth.toDate()).setParameter(3, lastDayOfMonth.toDate()).getResultList();
		for (Operation operation : operations) {
			if (operation.type == ETypeOperation.CREDIT) {
				ligneBudget.montantActuel = ligneBudget.montantActuel + operation.montant;
			} else {
				ligneBudget.montantActuel = ligneBudget.montantActuel - operation.montant;
			}
		}

		ligneBudget.save();
	}
}
