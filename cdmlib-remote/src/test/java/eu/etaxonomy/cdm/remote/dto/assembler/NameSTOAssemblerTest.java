package eu.etaxonomy.cdm.remote.dto.assembler;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NameSTOAssemblerTest {

	private NameAssembler ass;
	@Before
	public void setUp() throws Exception {
		ass = new NameAssembler();
	}

	@Test
	public void testGetRandom() {
		ass.getRandom();
	}

}
