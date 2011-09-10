package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.Compte;
import models.ETypeOperation;
import models.Echeance;
import models.Operation;
import models.Tag;
import models.User;

import org.joda.time.MutableDateTime;

import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import controllers.utils.Pagination;

@With(Secure.class)
public class TagsController extends Controller {

	private static final Pagination tagsPagination = new Pagination();

	@Before
	static void defaultData() {
		User connectedUser = Security.connectedUser();

		List<Compte> allComptes = Compte.find("user=?", connectedUser).fetch();
		renderArgs.put("allComptes", allComptes);

		List<Tag> allTags = Tag.find("user=? ORDER BY nom ASC", connectedUser).fetch();
		renderArgs.put("allTags", allTags);

		// Récupération de la pagination
		// Dans l'ordre params puis session
		String pageParam = params.get("page");
		if (pageParam == null) {
			pageParam = session.get("tagsPagination.page");
		}
		if (pageParam == null) {
			pageParam = "1";
		}
		tagsPagination.setPage(Integer.valueOf(pageParam));
		session.put("tagsPagination.page", pageParam);
	}

	public static void index(Long compteId) {
		User connectedUser = Security.connectedUser();

		Compte compte = null;
		if (compteId != null) {
			compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
			notFoundIfNull(compte);
		} else {
			compte = Compte.find("user=?", connectedUser).first();
			notFoundIfNull(compte);
			index(compte.id);
		}

		List tagsCredit = JPA
				.em()
				.createNativeQuery(
						"SELECT t.id AS id, t.nom AS nom, t.showOnGraph AS showOnGraph, sum(o.montant) AS count FROM TAG t INNER JOIN OPERATION_TAGS ot ON t.id = ot.tag_id INNER JOIN OPERATION o ON ot.operation_id = o.id WHERE o.compte_id=? AND o.type=? AND t.showOnGraph=true GROUP BY ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.CREDIT.toString()).getResultList();
		List tagsDebit = JPA
				.em()
				.createNativeQuery(
						"SELECT t.id AS id, t.nom AS nom, t.showOnGraph AS showOnGraph, sum(o.montant) AS count FROM TAG t INNER JOIN OPERATION_TAGS ot ON t.id = ot.tag_id INNER JOIN OPERATION o ON ot.operation_id = o.id WHERE o.compte_id=? AND o.type=? AND t.showOnGraph=true GROUP BY ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.DEBIT.toString()).getResultList();
		render(compte, tagsCredit, tagsDebit);
	}

	public static void detail(Long compteId, Long tagId) {
		User connectedUser = Security.connectedUser();

		Compte compte = Compte.find("id=? AND user=?", compteId, connectedUser).first();
		notFoundIfNull(compte);

		Tag currentTag = Tag.find("id=? AND user=?", tagId, connectedUser).first();
		notFoundIfNull(currentTag);

		Long countOperation = Operation.find("SELECT count(o) FROM Operation o JOIN o.tags t WHERE t.id=?", currentTag.id).first();
		tagsPagination.setElementCount(countOperation);

		List<Operation> operations = Operation.find("SELECT o FROM Operation o JOIN o.tags t WHERE o.compte.id=? AND t.id=? ORDER BY date DESC", compte.id, currentTag.id).fetch(tagsPagination.getPage(), tagsPagination.getPageSize());

		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		MutableDateTime dateTime = new MutableDateTime(new Date());
		Map<String, Float> datas = new TreeMap<String, Float>();
		for (int i = 1; i <= 12; i++) {
			dateTime.setMonthOfYear(i);
			datas.put(sdf.format(dateTime.toDate()), 0F);
		}
		boolean isDebit = false;
		boolean isCredit = false;
		for (Operation operation : operations) {
			if (operation.type == ETypeOperation.CREDIT) {
				isCredit = true;
			} else {
				isDebit = true;
			}
			String key = sdf.format(operation.date);
			Float montant = datas.get(key) + operation.montant;
			datas.put(key, montant);
		}

		String color = "";
		if (isCredit || isDebit) {
			if (isCredit && !isDebit) {
				color = ", colors: ['green']";
			}
			if (!isCredit && isDebit) {
				color = ", colors: ['red']";
			}
		}

		Pagination pagination = tagsPagination;
		render(compte, currentTag, operations, pagination, datas, color);
	}

	public static void supprimer(Long compteId, Long tagId) {
		User connectedUser = Security.connectedUser();

		Tag currentTag = Tag.find("id=? AND user=?", tagId, connectedUser).first();
		notFoundIfNull(currentTag);

		JPA.em().createNativeQuery("DELETE FROM OPERATION_TAGS WHERE tag_id=?").setParameter(1, currentTag.id).executeUpdate();
		Echeance.delete("tag=?", currentTag);
		currentTag.delete();

		index(compteId);
	}

	public static void showTagOnGraph(Long tagId, boolean show) {
		if (request.isAjax()) {
			User connectedUser = Security.connectedUser();

			Tag currentTag = Tag.find("id=? AND user=?", tagId, connectedUser).first();
			notFoundIfNull(currentTag);

			currentTag.showOnGraph = show;
			currentTag.save();
		}
	}
}
