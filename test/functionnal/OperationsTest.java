package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

public class OperationsTest extends MyFunctionalTest {

	@Test
	public void testThatAjouterOperationPageWorks() {
		Response response = GET("/compte/1/operation/ajouter");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatEditerOperationPageNotFound() {
		Response response = GET("/compte/1/operation/99/editer");
		assertIsNotFound(response);
	}

	@Test
	public void testThatEditerOperationPageWorks() {
		Response response = GET("/compte/1/operation/1/editer");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatImporterOperationPageWorks() {
		Response response = GET("/compte/1/operation/importer");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}
}
