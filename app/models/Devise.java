package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "DEVISE")
public class Devise extends Model {
	@Required
	public String nom;

	@Required
	public String symbole;
}
