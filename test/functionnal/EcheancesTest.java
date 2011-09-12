package functionnal;

import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;

public class EcheancesTest extends MyFunctionalTest {

	@Test
	public void testThatEcheancePageWorks() {
		String url = "/echeances";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteEcheancePageNotFound() {
		String url = "/compte/99/echeances";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatCompteEcheancePageWorks() {
		String url = "/compte/1/echeances";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatAjouterEcheancePageWorks() {
		String url = "/compte/1/echeance/ajouter";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatEditerEcheancePageNotFound() {
		String url = "/compte/1/echeance/99/editer";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatEditerEcheancePageWorks() {
		String url = "/compte/1/echeance/1/editer";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatCalendrierEcheancePageWorks() {
		String url = "/compte/1/echeance/date/2011-08-17";
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
