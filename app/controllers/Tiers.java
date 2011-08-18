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
import controllers.utils.Pagination;

/**
 * 
 * @author f.meurisse
 */
public class Tiers extends Controller {

	private static final Pagination tiersPagination = new Pagination();

	@Before
	public static void before() {
		// Récupération de la valeur du filter sur les tiers
		// Dans l'ordre params puis session
		String filtre = params.get("filtreTiers");
		if (filtre == null) {
			filtre = session.get("filtreTiers");
		}
		session.put("filtreTiers", filtre);
		renderArgs.put("filtreTiers", filtre);

		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();
		renderArgs.put("allTags", allTags);

		// Récupération de la pagination
		// Dans l'ordre params puis session
		String pageParam = params.get("page");
		if (pageParam == null) {
			pageParam = session.get("tiersPagination.page");
		}
		if (pageParam == null) {
			pageParam = "1";
		}
		tiersPagination.setPage(Integer.valueOf(pageParam));
		session.put("tiersPagination.page", pageParam);
	}

	public static void supprimerFiltre() {
		session.remove("filtreTiers");
		index();
	}

	public static void index() {
		String filtreTiers = session.get("filtreTiers");
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
