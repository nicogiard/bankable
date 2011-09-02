package functionnal;

import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;

public class TagsTest extends MyFunctionalTest {

	@Test
	public void testThatTagsPageWorks() {
		String url = "/tags";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertStatus(302, response);
	}

	@Test
	public void testThatCompteTagsPageNotFound() {
		String url = "/tags/compte/99";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound() {
		String url = "/tag/99/compte/1";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound2() {
		String url = "/tag/1/compte/99";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageWorks() {
		String url = "/tag/1/compte/1";
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
	public void testThatTagShowInGraphPageWorks() {
		String url = "/tag/1/graph/true";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
	}
}
