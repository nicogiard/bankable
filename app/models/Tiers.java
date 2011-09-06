package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

/**
 * 
 * @author f.meurisse
 */
@Entity
@Table(name = "TIERS")
public class Tiers extends Model {

	@ManyToOne(targetEntity = Civilite.class)
	@JoinColumn(name = "CIVILITE_ID")
	public Civilite civilite;

	@Column(name = "DESIGNATION")
	@Required
	private String designation;

	@Column(name = "COMMENTAIRES", length = 2500)
	public String commentaires;

	@ManyToMany
	@JoinTable(name = "TIERS_TAGS", joinColumns = @JoinColumn(name = "TIERS_ID"), inverseJoinColumns = @JoinColumn(name = "TAGS_ID"))
	public List<Tag> tags = new ArrayList<Tag>();

	public String getDesignation() {
		return Crypto.decryptAES(designation);
	}

	public void setDesignation(String designation) {
		this.designation = Crypto.encryptAES(designation);
	}
}
