package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "LIGNEBUDGET")
public class LigneBudget extends Model {
	@Required
	@ManyToOne
	public Budget budget;

	@Required
	@ManyToOne
	public Tag tag;

	@Column(columnDefinition = "Decimal(10,2)")
	public Float montantEcheance;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float montantManuel;

	@Column(columnDefinition = "Decimal(10,2)")
	public Float montantActuel;
}
