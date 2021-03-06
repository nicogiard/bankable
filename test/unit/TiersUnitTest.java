package unit;

import models.Tiers;

import org.junit.Test;

import play.data.validation.Validation;
import play.data.validation.Validation.ValidationResult;
import play.test.UnitTest;

/**
 * 
 * @author f.meurisse
 */
public class TiersUnitTest extends UnitTest {

	@Test
	public void testCreeTiersValide() throws Exception {
		Tiers nouveauTiers = new Tiers();
		nouveauTiers.setDesignation("Tiers1");
		nouveauTiers.civilite = null;

		ValidationResult result = Validation.valid("tiers", nouveauTiers);
		assertTrue(result.ok);

		nouveauTiers.save();
	}

	@Test
	public void testCreeTiersSansDesignation() throws Exception {
		Tiers nouveauTiers = new Tiers();
		nouveauTiers.setDesignation(null);
		nouveauTiers.civilite = null;

		ValidationResult result = Validation.valid("tiers", nouveauTiers);
		assertFalse(result.ok);
	}
}
