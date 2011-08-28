package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

public class ComptesTest extends MyFunctionalTest {

	@Test
	public void testThatIndexPageWorks() {
		Response response = GET("/");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatResumeComptePageNotFound() {
		Response response = GET("/resume/compte/99");
		assertIsNotFound(response);
	}

	@Test
	public void testThatResumeComptePageWorks() {
		Response response = GET("/resume/compte/1");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatHomeComptePageWorks() {
		Response response = GET("/compte");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatComptePageWorks() {
		Response response = GET("/compte/1");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatCompteAjouterPageWorks() {
		Response response = GET("/compte/ajouter");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatEditerComptePageWorks() {
		Response response = GET("/compte/1/editer");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}
}
