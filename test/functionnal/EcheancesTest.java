package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

public class EcheancesTest extends MyFunctionalTest {

	@Test
	public void testThatEcheancePageWorks() {
		Response response = GET("/echeance");
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteEcheancePageNotFound() {
		Response response = GET("/echeance/compte/99");
		assertIsNotFound(response);
	}

	@Test
	public void testThatCompteEcheancePageWorks() {
		Response response = GET("/echeance/compte/1");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatAjouterEcheancePageWorks() {
		Response response = GET("/echeance/ajouter");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatEditerEcheancePageNotFound() {
		Response response = GET("/echeance/99/editer");
		assertIsNotFound(response);
	}

	@Test
	public void testThatEditerEcheancePageWorks() {
		Response response = GET("/echeance/1/editer");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatCalendrierEcheancePageWorks() {
		Response response = GET("/echeance/compte/1/date/2011-08-17");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}
}
