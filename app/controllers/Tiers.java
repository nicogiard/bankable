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

/**
 * 
 * @author f.meurisse
 */
public class Tiers extends Controller {

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
    }

    public static void supprimerFiltre() {
        session.remove("filtreTiers");
        index();
    }

    public static void index() {
        afficherListeTiers(null);
    }

    public static void ajouter() {
        models.Tiers tiers = new models.Tiers();
        afficherListeTiers(tiers);
    }

    public static void editer(Long tiersId) {
        models.Tiers tiers = models.Tiers.findById(tiersId);
        notFoundIfNull(tiers);
        afficherListeTiers(tiers);
    }

    public static void enregistrer(@Required @Valid models.Tiers tiers,
            String tags) {
        if (validation.hasErrors()) {
            afficherListeTiers(tiers);
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
        flash.success("Le tiers '%s' a été enregistrée avec succès",
                tiers.designation);

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
        flash.success("Le tiers '%s' a été supprimé avec succès",
                tiers.designation);
        index();
    }

    /**
     * Affiche la liste des tiers.
     * 
     * @param tiers
     *            Le tiers en cours d'edition.
     */
    private static void afficherListeTiers(models.Tiers tiers) {
        String filtreTiers = session.get("filtreTiers");
        Logger.debug("Tiers - index: filtre = %s", filtreTiers);

        // Chargement de la liste des tiers.
        List<models.Tiers> tiersListe = null;
        if (StringUtils.isBlank(filtreTiers)) {
            tiersListe = models.Tiers.all().fetch();
        }
        else {
            String filtre = filtreTiers + "%";
            tiersListe = models.Tiers.find(
                    "designation like ? OR nom like ? OR prenom like ?",
                    filtre, filtre, filtre).fetch();
        }

        // Chargement de la liste des civilités
        List<Civilite> civilities = Civilite.find("ORDER BY nom").fetch();

        render("Tiers/index.html", civilities, tiersListe, tiers);

    }
}
