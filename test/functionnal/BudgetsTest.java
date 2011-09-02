package functionnal;

import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;

public class BudgetsTest extends MyFunctionalTest {

	@Test
	public void testThatBudgetPageWorks() {
		String url = "/budgets";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteBudgetPageNotFound() {
		String url = "/budgets/compte/99";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatCompteBudgetPageWorks() {
		String url = "/budgets/compte/1";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}
}
