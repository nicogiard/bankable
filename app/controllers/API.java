package controllers;

import java.util.Calendar;
import java.util.List;

import models.Compte;
import models.ETypeOperation;
import models.Operation;
import models.User;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import utils.OperationUtils;
import utils.TimeRequestLogger;
import controllers.utils.APIRenderJSon;

public class API extends Controller {
	protected static long timeRequest;

	@Before
	public static void basicAuth() {
		if (StringUtils.isBlank(request.user) && StringUtils.isBlank(request.password)) {
			unauthorized("Veuillez saisir vos identifiants");
		} else {
			User user = User.find("login=? and password=?", request.user, request.password).first();
			if (user == null) {
				unauthorized("Identifiants inconnus");
			}
		}
	}

	@Before
	public static void initTimeRequest() {
		timeRequest = Calendar.getInstance().getTimeInMillis();
	}

	@After
	public static void logTimeRequest() {
		TimeRequestLogger.log(request.action, Calendar.getInstance().getTimeInMillis() - timeRequest);
	}

	public static void comptes() {
		List<Compte> comptes = Compte.findAll();
		renderJSON(comptes);
	}

	public static void compte(Long compteId) {
		notFoundIfNull(compteId);
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);
		renderJSON(compte);
	}

	public static void operations(Long compteId) {
		notFoundIfNull(compteId);
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);
		List<Operation> operations = Operation.find("compte.id=? ORDER BY date DESC, id DESC", compte.id).fetch();
		renderJSON(operations);
	}

	public static void enregistrerOperation(@Required @Valid Operation operation, String tags) {
		if (validation.hasErrors()) {
			renderJSON(validation.errorsMap());
		}

		if (!request.user.equals(operation.compte.user.login)) {
			forbidden("Vous n'êtes pas le propriétaire de ce compte");
		}

		if (operation.type == ETypeOperation.DEBIT) {
			operation.compte.solde = operation.compte.solde - operation.montant;
		} else {
			operation.compte.solde = operation.compte.solde + operation.montant;
		}

		operation.tags.clear();
		OperationUtils.extractTags(operation, tags);

		operation.compte.save();
		operation.save();

		renderJSON(operation);
	}

	protected static void renderJSON(Object o) {
		throw new APIRenderJSon(o);
	}
}
