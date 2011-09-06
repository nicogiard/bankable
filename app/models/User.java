package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
@Table(name = "USER")
public class User extends Model {
	@Required
	private String login;

	@Required
	private String password;

	@Required
	private String firstName;

	@Required
	private String lastName;

	@Required
	@Email
	private String email;

	@Required
	public Boolean active;

	public Date dateCreation;

	public Date dateModification;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	@OrderBy("id DESC")
	public List<Compte> comptes;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[User]");
		sb.append(" login : ").append(getLogin());
		return sb.toString();
	}

	public String getLogin() {
		return Crypto.decryptAES(login);
	}

	public void setLogin(String login) {
		this.login = Crypto.encryptAES(login);
	}

	public String getPassword() {
		return Crypto.decryptAES(password);
	}

	public void setPassword(String password) {
		this.password = Crypto.encryptAES(password);
	}

	public String getFirstName() {
		return Crypto.decryptAES(firstName);
	}

	public void setFirstName(String firstName) {
		this.firstName = Crypto.encryptAES(firstName);
	}

	public String getLastName() {
		return Crypto.decryptAES(lastName);
	}

	public void setLastName(String lastName) {
		this.lastName = Crypto.encryptAES(lastName);
	}

	public String getEmail() {
		return Crypto.decryptAES(email);
	}

	public void setEmail(String email) {
		this.email = Crypto.encryptAES(email);
	}
}
