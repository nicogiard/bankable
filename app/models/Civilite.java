package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "CIVILITE", uniqueConstraints = @UniqueConstraint(columnNames = { "CODE" }))
public class Civilite extends Model {

    @Column(name = "CODE")
    @Required
    public String code;

    @Column(name = "NOM")
    @Required
    public String nom;

    @Column(name = "ABBR")
    @Required
    public String abreviation;

    @Override
    public String toString() {
        return this.nom;
    }
}
