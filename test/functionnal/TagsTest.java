package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

public class TagsTest extends MyFunctionalTest {

	@Test
	public void testThatTagsPageWorks() {
		Response response = GET("/tags");
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteTagsPageNotFound() {
		Response response = GET("/tags/compte/99");
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound() {
		Response response = GET("/tag/99/compte/1");
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound2() {
		Response response = GET("/tag/1/compte/99");
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageWorks() {
		Response response = GET("/tag/1/compte/1");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset("utf-8", response);
	}

	@Test
	public void testThatTagShowInGraphPageWorks() {
		Response response = GET("/tag/1/graph/true");
		assertIsOk(response);
	}
}
