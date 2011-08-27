package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "PLANNINGECHEANCE")
public class PlanningEcheance extends Model {

    @Required
    @ManyToOne
    public Echeance echeance;

    @Required
    public Date dateEcheance;

    public Date date;

    public ETypeEcheance type;

    public Float montant;
}
