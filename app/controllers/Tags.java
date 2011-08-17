package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.Compte;
import models.ETypeOperation;
import models.Operation;
import models.Tag;

import org.joda.time.MutableDateTime;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;

public class Tags extends Controller {

	public static final Long OPERATION_PAR_PAGE = 15L;

	@Before
	static void defaultData() {
		List<Compte> allComptes = Compte.findAll();
		List<Tag> allTags = Tag.find("ORDER BY nom ASC").fetch();

		renderArgs.put("allComptes", allComptes);
		renderArgs.put("allTags", allTags);
	}

	public static void index(Long compteId) {
		Compte compte = null;
		if (compteId != null) {
			compte = Compte.findById(compteId);
		} else {
			compte = Compte.find("").first();
			index(compte.id);
		}

		List tagsCredit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, sum(o.montant) as count from tag t inner join operation_tags ot on t.id = ot.tag_id inner join operation o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.CREDIT).getResultList();
		List tagsDebit = JPA
				.em()
				.createNativeQuery(
						"select t.id as id, t.nom as nom, t.showOnGraph as showOnGraph, sum(o.montant) as count from tag t inner join operation_tags ot on t.id = ot.tag_id inner join operation o on ot.operation_id = o.id where o.compte_id=? and o.type=? and t.showOnGraph=true group by ot.tag_id",
						"TagWithCount").setParameter(1, compte.id).setParameter(2, ETypeOperation.DEBIT).getResultList();
		render(compte, tagsCredit, tagsDebit);
	}

	public static void detail(Long compteId, Long tagId, Long pageNumber) {
		Compte compte = Compte.findById(compteId);
		notFoundIfNull(compte);

		Tag currentTag = Tag.findById(tagId);
		notFoundIfNull(currentTag);

		Long page = (pageNumber == null || pageNumber == 0) ? 1L : pageNumber;
		Long countOperation = Compte.find("select count(o) from Operation o join o.tags t where t.id=?", currentTag.id).first();
		Double tempNumberOfPage = countOperation.doubleValue() / OPERATION_PAR_PAGE;
		Long numberOfPage = tempNumberOfPage < 1 ? 1L : Math.round(tempNumberOfPage);

		Logger.info("page : %s | count : %s | numberOfPage : %s", page.toString(), countOperation.toString(), numberOfPage.toString());

		List<Operation> operations = Operation.find("select o from Operation o join o.tags t where o.compte.id=? AND t.id=? ORDER BY date DESC", compte.id, currentTag.id).fetch(page.intValue(), OPERATION_PAR_PAGE.intValue());

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

		render(compte, currentTag, operations, countOperation, numberOfPage, page, datas, color);
	}

	public static void showTagOnGraph(Long tagId, boolean show) {
		if (request.isAjax()) {
			Tag currentTag = Tag.findById(tagId);
			notFoundIfNull(currentTag);

			currentTag.showOnGraph = show;
			currentTag.save();
		}
	}
}
