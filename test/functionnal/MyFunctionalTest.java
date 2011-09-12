package functionnal;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

public abstract class MyFunctionalTest extends FunctionalTest {

	Response login(String username, String password) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("password", password);
		Response loginResponse = POST("/login", params);
		assertStatus(302, loginResponse);
		return loginResponse;
	}

	Request requestAfterLogin(String url, Response loginResponse) {
		Request request = newRequest();
		request.cookies = loginResponse.cookies;
		request.url = url;
		request.path = url;
		request.body = new ByteArrayInputStream(new byte[0]);
		return request;
	}

	void logHeaders(Response response) {
		for (String key : response.headers.keySet()) {
			Logger.info(key + " : " + response.headers.get(key));
		}
	}

	void logCookies(Response response) {
		for (String key : response.cookies.keySet()) {
			Logger.info(key + " : " + response.cookies.get(key).name + " = " + response.cookies.get(key).value);
		}
	}

	void logContent(Response response) {
		Logger.info(response.out.toString());
	}
}
