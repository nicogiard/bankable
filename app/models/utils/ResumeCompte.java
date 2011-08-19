package models.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.Operation;

public class ResumeCompte {
	public Map<Date, Float> soldes = new TreeMap<Date, Float>();
	public List<Operation> operations = new ArrayList<Operation>();
	public BigInteger countNoTag;
	public List tagsCredit;
	public List tagsDebit;

	public Long todayCredits;
	public Long todayDebits;
	public Long todayEcheances;

	public Long weekCredits;
	public Long weekDebits;
	public Long weekEcheances;
}
