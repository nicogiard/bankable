package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "OPERATIONIMPORT")
public class OperationImport extends Model {

	@Required
	public Date date;

	@Required
	public String libelle;

	@Required
	@Enumerated(EnumType.STRING)
	public ETypeOperation type;

	@Required
	public Float montant;

	public String numero;

	public String detail;

	@Required
	@ManyToOne
	public Compte compte;
}
