package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 *
 * @author f.meurisse
 */
@Entity
@Table(name = "TIERS")
public class Tiers extends Model {
    
    @Column(name = "DESIGNATION")
    @Required
    public String designation;

    @ManyToOne(targetEntity = Civilite.class)
    @JoinColumn(name = "CIVILITE_ID")
    public Civilite civilite;

    @Column(name = "NOM")
    public String nom;

    @Column(name = "PRENOM")
    public String prenom;

    @Column(name = "COMMENTAIRES", length = 2500)
    public String commentaires;
    
}
