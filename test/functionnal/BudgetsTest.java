package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

public class BudgetsTest extends MyFunctionalTest {

	@Test
	public void testThatBudgetPageWorks() {
		Response response = GET("/budgets");
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteBudgetPageNotFound() {
		Response response = GET("/budgets/compte/99");
		assertIsNotFound(response);
	}

	@Test
	public void testThatCompteBudgetPageWorks() {
		Response response = GET("/budgets/compte/1");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}
}
