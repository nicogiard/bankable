package functionnal;

import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;

/**
 * 
 * @author f.meurisse
 */
public class TiersTest extends MyFunctionalTest {

	@Test
	public void testThatTiersPageWorks() {
		String url = "/tiers";
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
