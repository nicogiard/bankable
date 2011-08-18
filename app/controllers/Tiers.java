package controllers;

import java.util.List;

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
    }

    public static void supprimerFiltre() {
        session.remove("filtreTiers");
        index();
    }

    public static void index() {
        String filtreTiers = session.get("filtreTiers");
        Logger.debug("Tiers - index: filtre = %s", filtreTiers);

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

        render(filtreTiers, tiersListe);
    }

    public static void ajouter() {
        List<models.Tiers> tiersListe = models.Tiers.all().fetch();
        models.Tiers tiers = new models.Tiers();
        render("Tiers/index.html", tiersListe, tiers);
    }

    public static void editer(Long tiersId) {
        List<models.Tiers> tiersListe = models.Tiers.all().fetch();
        models.Tiers tiers = models.Tiers.findById(tiersId);
        notFoundIfNull(tiers);
        render("Tiers/index.html", tiersListe, tiers);
    }

    public static void enregistrer(@Required @Valid models.Tiers tiers) {
        if (validation.hasErrors()) {
            List<models.Tiers> tiersListe = models.Tiers.all().fetch();
            render("Tiers/index.html", tiersListe, tiers);
        }

        tiers.save();
        flash.success("Le tiers a été enregistrée avec succès");

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
        flash.success("Le tiers a été supprimé avec succès");
        index();
    }
}
