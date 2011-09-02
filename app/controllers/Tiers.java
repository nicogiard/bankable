package controllers;

import java.util.List;

import models.Civilite;
import models.Tag;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import controllers.utils.Pagination;
import controllers.utils.SessionUtil;

/**
 * 
 * @author f.meurisse
 */
@With(Secure.class)
public class Tiers extends Controller {

	private static final Pagination tiersPagination = new Pagination();

	@Before
	public static void before() {
		Logger.debug(">> Tiers.before >>");

		// Récupération de la valeur du filter sur les tiers
		// Dans l'ordre params puis session
		String filtre = SessionUtil.getSessionParam(SessionUtil.SESSION_PARAM_TIERS_FILTRE, true, "");
		Logger.debug("Filre = %s", filtre);
		renderArgs.put("Tiers_filtre", filtre);

		// Récupération de la pagination
		// Dans l'ordre params puis session
		String pageParam = SessionUtil.getSessionParam(SessionUtil.SESSION_PARAM_TIERS_PAGE, true, "1");
		Logger.debug("Page = %s", pageParam);
		tiersPagination.setPage(Integer.valueOf(pageParam));

		// Chargement des tags.
		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
		renderArgs.put("allTags", allTags);

		Logger.debug("<< Tiers.before <<");
	}

	public static void supprimerFiltre() {
		session.remove(SessionUtil.SESSION_PARAM_TIERS_FILTRE);
		index();
	}

	public static void index() {
		String filtreTiers = session.get(SessionUtil.SESSION_PARAM_TIERS_FILTRE);
		Logger.debug("Tiers - index: filtre = %s", filtreTiers);
		if (filtreTiers != null) {
			filtreTiers += "%";
		}

		String requeteListTiers = null;
		if (StringUtils.isNotBlank(filtreTiers)) {
			requeteListTiers = "designation like ? OR nom like ? OR prenom like ?";
		}

		// Dénombrement des tiers
		long nombreTiers = 0;
		if (requeteListTiers != null) {
			nombreTiers = models.Tiers.count(requeteListTiers, filtreTiers, filtreTiers, filtreTiers);
		} else {
			nombreTiers = models.Tiers.count();
		}
		tiersPagination.setElementCount(nombreTiers);

		// Chargement de la liste des tiers.
		List<models.Tiers> tiersListe = null;
		if (StringUtils.isBlank(filtreTiers)) {
			tiersListe = models.Tiers.all().fetch(tiersPagination.getPage(), tiersPagination.getPageSize());
		} else {
			tiersListe = models.Tiers.find(requeteListTiers, filtreTiers, filtreTiers, filtreTiers).fetch(tiersPagination.getPage(), tiersPagination.getPageSize());
		}

		Pagination pagination = tiersPagination;
		render(tiersListe, pagination);
	}

	public static void ajouter() {
		String titre = "Ajouter";
		List<Civilite> civilities = Civilite.find("ORDER BY nom").fetch();
		render("Tiers/editer.html", titre, civilities);
	}

	public static void editer(Long tiersId) {
		models.Tiers tiers = models.Tiers.findById(tiersId);
		notFoundIfNull(tiers);
		String titre = "Editer";
		List<Civilite> civilities = Civilite.find("ORDER BY nom").fetch();
		render(titre, tiers, civilities);
	}

	public static void enregistrer(@Required @Valid models.Tiers tiers, String tags) {
		if (validation.hasErrors()) {
			if (tiers.id != null && tiers.id > 0) {
				String titre = "Editer";
				List<Civilite> civilities = Civilite.find("ORDER BY nom").fetch();
				render("Tiers/editer.html", titre, tiers, civilities);
			} else {
				String titre = "Ajouter";
				List<Civilite> civilities = Civilite.find("ORDER BY nom").fetch();
				render("Tiers/editer.html", titre, tiers, civilities);
			}
		}

		tiers.tags.clear();
		if (StringUtils.isNotBlank(tags)) {
			String[] tabTags = tags.split(",");
			for (String stringTag : tabTags) {
				Tag tag = Tag.find("nom=?", stringTag.trim()).first();
				if (tag == null) {
					tag = new Tag();
					tag.nom = stringTag.trim();
					tag.save();
				}
				tiers.tags.add(tag);
			}
		}

		tiers.save();
		flash.success("Le tiers '%s' a été enregistrée avec succès", tiers.designation);

		if (params._contains("valid")) {
			ajouter();
		}
		if (params._contains("validQuit")) {
			index();
		}
	}

	public static void supprimer(Long tiersId) {
		models.Tiers tiers = models.Tiers.findById(tiersId);
		notFoundIfNull(tiers);
		tiers.delete();
		flash.success("Le tiers '%s' a été supprimé avec succès", tiers.designation);
		index();
	}
}
