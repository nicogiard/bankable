package functionnal;

import org.junit.Test;

import play.mvc.Http.Response;

/**
 * 
 * @author f.meurisse
 */
public class TiersTest extends MyFunctionalTest {

    @Test
    public void testThatTiersPageWorks() {
        Response response = GET("/tiers");
        assertStatus(200, response);
    }

}
