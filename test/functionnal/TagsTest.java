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
		String url = "/compte/99/tags";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound() {
		String url = "/compte/1/tag/99";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageNotFound2() {
		String url = "/compte/99/tag/1";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsNotFound(response);
	}

	@Test
	public void testThatTagPageWorks() {
		String url = "/compte/1/tag/1";
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
		String url = "/compte/1/tag/1/graph/true";
		Response response = GET(url);
		assertStatus(302, response);

		Response loginResponse = login("nicogiard", "nico");

		Request request = requestAfterLogin(url, loginResponse);
		response = makeRequest(request);
		assertIsOk(response);
	}
}
