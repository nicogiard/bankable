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

@Entity
@Table(name = "USER")
public class User extends Model {
	@Required
	public String login;

	@Required
	public String password;

	@Required
	public String firstName;

	@Required
	public String lastName;

	@Required
	@Email
	public String email;

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
		sb.append(" login : ").append(login);
		return sb.toString();
	}
}
