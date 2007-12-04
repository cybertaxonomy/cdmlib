package eu.etaxonomy.cdm.database.init;


import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.ConceptRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;


public class TermLoaderTest extends CdmUnitTestBase{
	@Autowired
	private TermLoader loader;

	@Test
	public void loadTerms() {
		try {
			loader.loadDefaultTerms(WaterbodyOrCountry.class);
			loader.loadDefaultTerms(Language.class);
			loader.loadDefaultTerms(Continent.class);
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
