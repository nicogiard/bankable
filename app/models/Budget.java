package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "BUDGET")
public class Budget extends Model {

	@Required
	@ManyToOne
	public Compte compte;

	@OneToMany(mappedBy = "budget", fetch = FetchType.LAZY)
	public List<LigneBudget> lignes;
}
