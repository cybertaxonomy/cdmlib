/**
 * 
 */
package eu.etaxonomy.cdm.model.reference;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * @author a.mueller
 * @since 6.6.2013
 *
 */
public class OriginalSourceTypeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	
	@Test
	public void testKindOf(){
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Import.getKindOf());
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Transformation.getKindOf());
		assertSame(OriginalSourceType.Lineage, OriginalSourceType.Aggregation.getKindOf());
	}

}
