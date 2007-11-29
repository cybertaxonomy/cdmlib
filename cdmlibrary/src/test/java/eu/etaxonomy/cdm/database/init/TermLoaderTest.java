package eu.etaxonomy.cdm.database.init;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;


public class TermLoaderTest {
	private TermLoader loader;
	@Before
	// generate enumeration for every test to play with
	public void onSetUp(){
		loader = new TermLoader();
	}

	@Test
	public void loadTerms() {
		try {
			loader.loadDefaultTerms(Rank.class);
			loader.loadDefaultTerms(TypeDesignationStatus.class);
			loader.loadDefaultTerms(NomenclaturalStatusType.class);
			loader.loadDefaultTerms(SynonymRelationshipType.class);
			loader.loadDefaultTerms(HybridRelationshipType.class);
			loader.loadDefaultTerms(NameRelationshipType.class);
			loader.loadDefaultTerms(ConceptRelationshipType.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
