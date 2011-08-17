package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Calendrier {
	public Date actualMonth;
	public Date previousMonth;
	public Date nextMonth;

	public List<Semaine> semaines = new ArrayList<Semaine>();

	public Calendrier() {
		semaines = new ArrayList<Semaine>();
	}
}
