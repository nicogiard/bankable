package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "COMPTE")
public class Compte extends Model {

	@Required
	public String nom;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float solde;

	public String numero;

	public String etablissement;

	@Required
	@ManyToOne
	public Devise devise;

	@OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
	@OrderBy("id DESC")
	public List<Operation> operations;

	@OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
	public List<Echeance> echances;
}
