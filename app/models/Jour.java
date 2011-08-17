package models;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

public class Jour {
	public boolean previousMonth = false;
	public DateTime date;
	public boolean today;
	public boolean nextMonth = false;

	public List<Echeance> echeances = new ArrayList<Echeance>();

	public Jour(MutableDateTime date) {
		this.date = date.toDateTime();
	}
}
