package utils;

import java.util.Date;
import java.util.List;

import models.Calendrier;
import models.Echeance;
import models.Jour;
import models.Semaine;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import play.Logger;

public class PlanningEcheance {

	public static void compute(Date actualMonth, List<Echeance> echeances) {
		for (Echeance echeance : echeances) {
			Logger.debug("echeance : [%s]", echeance.toString());

			switch (echeance.typeFrequence) {
			case JOURNALIERE:
				echeanceJournaliere(actualMonth, echeance);
				break;
			case HEBDOMADAIRE:
				echeanceHebdomadaire(actualMonth, echeance);
				break;
			case MENSUELLE:
				echeanceMensuelle(actualMonth, echeance);
				break;
			case ANNUELLE:
				echeanceAnnuelle(actualMonth, echeance);
				break;
			default:
				break;
			}
		}
	}

	public static Calendrier buildCalendrier(Date date) {
		Calendrier calendrier = new Calendrier();
		calendrier.actualMonth = date == null ? new Date() : date;
		DateTime previousMonth = new DateTime(calendrier.actualMonth).minusMonths(1).withDayOfMonth(1);
		calendrier.previousMonth = previousMonth.toDate();
		DateTime nextMonth = new DateTime(calendrier.actualMonth).plusMonths(1).withDayOfMonth(1);
		calendrier.nextMonth = nextMonth.toDate();

		MutableDateTime currentDate = new MutableDateTime(calendrier.actualMonth);
		currentDate.setDayOfMonth(1);
		currentDate.setMillisOfDay(0);

		Semaine week = new Semaine();
		int indexMonth = currentDate.monthOfYear().get();
		int indexDay = currentDate.dayOfWeek().get();
		int indexWeek = currentDate.weekOfWeekyear().get();

		// Les jours du mois précédent
		if (indexDay > 0) {
			indexDay--;
			currentDate.addDays(-indexDay);
			for (int i = 0; i < indexDay; i++) {
				Jour day = new Jour(currentDate);
				day.previousMonth = true;

				week.jours.add(day);
				currentDate.addDays(1);
			}
		}
		while (currentDate.monthOfYear().get() == indexMonth) {
			while (currentDate.weekOfWeekyear().get() == indexWeek && currentDate.monthOfYear().get() == indexMonth) {
				Jour day = new Jour(currentDate);
				DateTime today = new DateTime(new Date()).withMillisOfDay(0);
				if (currentDate.isEqual(today)) {
					day.today = true;
				}
				week.jours.add(day);
				currentDate.addDays(1);
			}
			if (currentDate.monthOfYear().get() == indexMonth) {
				calendrier.semaines.add(week);
				week = new Semaine();
				indexWeek = currentDate.weekOfWeekyear().get();
			}
		}

		// Les jours du mois suivants
		while (currentDate.weekOfWeekyear().get() == indexWeek) {
			Jour day = new Jour(currentDate);
			day.nextMonth = true;
			week.jours.add(day);
			currentDate.addDays(1);
		}
		calendrier.semaines.add(week);

		return calendrier;
	}

	public static boolean isBetween(MutableDateTime date, MutableDateTime from, MutableDateTime to) {
		return date.isAfter(from) && date.isBefore(to);
	}

	public static boolean isEqual(DateTime date, Date compare) {
		return isEqual(date, new DateTime(compare));
	}

	public static boolean isEqual(DateTime date, DateTime compare) {
		return isEqual(date, new MutableDateTime(compare));
	}

	public static boolean isEqual(DateTime date, MutableDateTime compare) {
		return isEqual(new MutableDateTime(date), compare);
	}

	public static boolean isEqual(MutableDateTime date, MutableDateTime compare) {
		date.setTime(0);
		compare.setTime(0);
		return date.isEqual(compare);
	}

	private static void echeanceJournaliere(Date actualMonth, Echeance echeance) {
		// TODO : echeance journaliere
	}

	private static void echeanceHebdomadaire(Date actualMonth, Echeance echeance) {
		// TODO : echeance hebdomadaire
	}

	private static void echeanceMensuelle(Date actualMonth, Echeance echeance) {
		MutableDateTime firstDayOfMonth = new MutableDateTime(actualMonth);
		firstDayOfMonth.setDayOfMonth(1);
		Logger.debug("firstDayOfMonth : [%s]", firstDayOfMonth);

		MutableDateTime lastDayOfMonth = new MutableDateTime(actualMonth);
		lastDayOfMonth.setDayOfMonth(1);
		lastDayOfMonth.addMonths(1);
		lastDayOfMonth.addDays(-1);
		Logger.debug("lastDayOfMonth : [%s]", lastDayOfMonth);

		MutableDateTime dateEcheance = new MutableDateTime(echeance.date);
		dateEcheance.setTime(0);

		boolean isInMonth = isBetween(dateEcheance, firstDayOfMonth, lastDayOfMonth);
		Logger.debug("isInMonth : [%s]", isInMonth);

		if (isInMonth) {
			models.PlanningEcheance planningEcheance = models.PlanningEcheance.find("echeance.id=? AND date=?", echeance.id, dateEcheance.toDate()).first();
			if (planningEcheance == null) {
				newPlanningEcheance(echeance, dateEcheance.toDate());
				Logger.debug("ajout de l'écheance [%s] au jour [%s]", echeance, echeance.date);
			}
		} else {
			if (dateEcheance.isBefore(firstDayOfMonth)) {
				dateEcheance.setMonthOfYear(new DateTime(actualMonth).getMonthOfYear());

				MutableDateTime dateFin = new MutableDateTime(echeance.dateFin);
				if (echeance.dateFin == null || dateEcheance.isEqual(dateFin) || (dateEcheance.isBefore(dateFin))) {
					models.PlanningEcheance planningEcheance = models.PlanningEcheance.find("echeance.id=? AND date=?", echeance.id, dateEcheance.toDate()).first();
					if (planningEcheance == null) {
						newPlanningEcheance(echeance, dateEcheance.toDate());
						Logger.debug("ajout de l'écheance [%s] au jour [%s]", echeance, dateEcheance);
					}
				} else {
					Logger.debug("dateEcheance [%s] est apres echeance.dateFin [%s]", dateEcheance, dateFin);
				}
			} else {
				Logger.debug("dateEcheance [%s] est apres firstDayOfMonth [%s]", dateEcheance, firstDayOfMonth);
			}
		}
	}

	private static void echeanceAnnuelle(Date actualMonth, Echeance echeance) {
		MutableDateTime firstDayOfYear = new MutableDateTime(actualMonth);
		firstDayOfYear.setDayOfYear(1);
		Logger.debug("firstDayOfYear : [%s]", firstDayOfYear);

		MutableDateTime lastDayOfYear = new MutableDateTime(actualMonth);
		lastDayOfYear.setDayOfYear(1);
		lastDayOfYear.addYears(1);
		lastDayOfYear.addDays(-1);
		Logger.debug("lastDayOfYear : [%s]", lastDayOfYear);

		MutableDateTime dateEcheance = new MutableDateTime(echeance.date);
		dateEcheance.setTime(0);

		boolean isInYear = isBetween(dateEcheance, firstDayOfYear, lastDayOfYear);
		Logger.debug("isInYear : [%s]", isInYear);

		if (isInYear) {
			models.PlanningEcheance planningEcheance = models.PlanningEcheance.find("echeance.id=? AND date=?", echeance.id, dateEcheance.toDate()).first();
			if (planningEcheance == null) {
				newPlanningEcheance(echeance, dateEcheance.toDate());
				Logger.debug("ajout de l'écheance [%s] au jour [%s]", echeance, echeance.date);
			}
		} else {
			if (dateEcheance.isBefore(firstDayOfYear)) {
				dateEcheance.setYear(new DateTime(actualMonth).getYear());

				MutableDateTime dateFin = new MutableDateTime(echeance.dateFin);
				if (echeance.dateFin == null || dateEcheance.isEqual(dateFin) || (dateEcheance.isBefore(dateFin))) {
					models.PlanningEcheance planningEcheance = models.PlanningEcheance.find("echeance.id=? AND date=?", echeance.id, dateEcheance.toDate()).first();
					if (planningEcheance == null) {
						newPlanningEcheance(echeance, dateEcheance.toDate());
						Logger.debug("ajout de l'écheance [%s] au jour [%s]", echeance, dateEcheance);
					}
				} else {
					Logger.debug("dateEcheance [%s] est apres echeance.dateFin [%s]", dateEcheance, dateFin);
				}
			} else {
				Logger.debug("dateEcheance [%s] est apres firstDayOfYear [%s]", dateEcheance, firstDayOfYear);
			}
		}
	}

	private static void newPlanningEcheance(Echeance echeance, Date dateEcheance) {
		models.PlanningEcheance planningEcheance = new models.PlanningEcheance();
		planningEcheance.echeance = echeance;
		planningEcheance.date = dateEcheance;
		planningEcheance.save();
	}
}
