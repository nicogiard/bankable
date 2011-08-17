package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "OPERATION")
public class Operation extends Model {

	@Required
	public Date date;

	@Required
	public String libelle;

	@Required
	@Enumerated(EnumType.STRING)
	public ETypeOperation type;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float montant;

	public String numero;

	public String detail;

	@Column(columnDefinition = "Decimal(10,2)")
	public Float solde;

	@Required
	@Enumerated(EnumType.STRING)
	public EEtatOperation etat;

	@ManyToMany
	@JoinTable(name = "OPERATION_TAGS", joinColumns = @JoinColumn(name = "OPERATION_ID"), inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
	public List<Tag> tags = new ArrayList<Tag>();

	@Required
	@ManyToOne
	public Compte compte;
}
