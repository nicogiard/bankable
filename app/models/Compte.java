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
import play.libs.Crypto;

@Entity
@Table(name = "COMPTE")
public class Compte extends Model {

	@Required
	@ManyToOne
	public User user;

	@Required
	private String nom;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float solde;

	@Required
	@Column(columnDefinition = "Decimal(10,2)")
	public Float soldeRapproche;

	private String numero;

	private String etablissement;

	@OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
	@OrderBy("id DESC")
	public List<Operation> operations;

	@OneToMany(mappedBy = "compte", fetch = FetchType.LAZY)
	public List<Echeance> echances;

	public String getNom() {
		return Crypto.decryptAES(nom);
	}

	public void setNom(String nom) {
		this.nom = Crypto.encryptAES(nom);
	}

	public String getNumero() {
		return Crypto.decryptAES(numero);
	}

	public void setNumero(String numero) {
		this.numero = Crypto.encryptAES(numero);
	}

	public String getEtablissement() {
		return Crypto.decryptAES(etablissement);
	}

	public void setEtablissement(String etablissement) {
		this.etablissement = Crypto.encryptAES(etablissement);
	}
}
