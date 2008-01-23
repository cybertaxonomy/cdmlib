package eu.etaxonomy.cdm.database.init;

 import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;


public class TermLoaderTest extends CdmUnitTestBase{
	@Autowired
	private TermLoader loader;

	@Test
	public void insertTerms() {
		try {
			loader.loadAllDefaultTerms();
			Rank genus = Rank.GENUS();
			assertEquals(genus.getUuid(), Rank.GENUS().getUuid());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
