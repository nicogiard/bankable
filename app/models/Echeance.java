package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "ECHEANCE")
public class Echeance extends Model {

	@Required
	public String description;

	@Required
	public Date date;

	@Required
	@Enumerated(EnumType.STRING)
	public ETypeEcheance type;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float montant;

	@Required
	@ManyToOne
	public Compte compte;

	@Required
	@Enumerated(EnumType.STRING)
	public ETypeFrequence typeFrequence;

	public Date dateFin;

	@Required
	@ManyToOne
	public Tag tag;

	@OneToMany(mappedBy = "echeance", fetch = FetchType.LAZY)
	public List<PlanningEcheance> planning = new ArrayList<PlanningEcheance>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[[Echeance] : ");
		sb.append("description : ").append(description);
		sb.append(" | date : ").append(date);
		sb.append(" | type : ").append(type);
		sb.append(" | montant : ").append(montant);
		sb.append(" | typeFrequence : ").append(typeFrequence);
		sb.append("]");
		return sb.toString();
	}
}
