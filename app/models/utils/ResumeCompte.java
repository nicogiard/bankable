package models.utils;

import java.math.BigInteger;
import java.util.List;

import models.Operation;

public class ResumeCompte {
	public List<Operation> operations;
	public BigInteger countNoTag;
	public List tagsCredit;
	public List tagsDebit;

	public Integer todayCredits;
	public Integer todayDebits;
	public Integer todayEcheances;

	public Integer yesterdayCredits;
	public Integer yesterdayDebits;
	public Integer yesterdayEcheances;
}
