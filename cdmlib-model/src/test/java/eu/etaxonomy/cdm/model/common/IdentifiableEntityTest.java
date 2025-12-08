/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.babadshanjan
 * @since 02.02.2009
 */
//NOTE: tests on credits were moved to CreditableEntity, not much remaining here for now.
public class IdentifiableEntityTest extends EntityTestBase {

	private TaxonName abies;
	private TaxonName abiesMill;
	private TaxonName abiesAlba;
	private TaxonName abiesAlbaMichx;
	private TaxonName abiesAlbaMill;
	private TaxonName abiesAlbaxPinusBeta;
	private TaxonName pinusBeta;

	private Taxon abiesTaxon;
	private Taxon abiesMillTaxon;

	private TaxonName abiesAutonym;
	private Taxon abiesAutonymTaxon;

	private TaxonName abiesBalsamea;
	private Taxon abiesBalsameaTaxon;
//	private Taxon abiesAlbaxPinusBetaTaxon;

	@Before
	public void setUp() throws Exception {

		abies = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
		abies.setNameCache("Abies");
		abies.setTitleCache("Abies", true);
		Reference sec = ReferenceFactory.newArticle();
		sec.setTitle("Abies alba Ref");

		abiesTaxon = Taxon.NewInstance(abies, sec);

		abiesMill = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
		abiesMill.setNameCache("Abies");
		abiesMill.setTitleCache("Abies Mill.", true);
		abiesMillTaxon = Taxon.NewInstance(abiesMill, sec);

		abiesAlba = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlba.setNameCache("Abies alba");
		abiesAlba.setTitleCache("Abies alba", true);

		abiesAlbaMichx = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMichx.setNameCache("Abies alba");
		abiesAlbaMichx.setTitleCache("Abies alba Michx.", true);

		abiesAlbaMill = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMill.setNameCache("Abies alba");
		abiesAlbaMill.setTitleCache("Abies alba Mill.", true);

		abiesAutonym  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesAutonym.setGenusOrUninomial("Abies");
		abiesAutonym.setInfraGenericEpithet("Abies");

		abiesAutonym.setTitleCache("Abies Mill. sect. Abies", true);
		abiesAutonym.getNameCache();
		abiesAutonymTaxon = Taxon.NewInstance(abiesAutonym, sec);

		abiesBalsamea  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesBalsamea.setGenusOrUninomial("Abies");
		abiesBalsamea.setInfraGenericEpithet("Balsamea");
		abiesBalsamea.getNameCache();
		abiesBalsamea.setTitleCache("Abies sect. Balsamea L.", true);
		abiesBalsameaTaxon = Taxon.NewInstance(abiesBalsamea, sec);

		abiesAlbaxPinusBeta = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		pinusBeta = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		pinusBeta.setGenusOrUninomial("Pinus");
		pinusBeta.setSpecificEpithet("beta");
		abiesAlbaxPinusBeta.setHybridFormula(true);
		abiesAlbaxPinusBeta.addHybridParent(abiesAlba, HybridRelationshipType.FIRST_PARENT(), null);
		abiesAlbaxPinusBeta.addHybridParent(pinusBeta, HybridRelationshipType.SECOND_PARENT(), null);
	}

	@Test
	public void testClone(){
		IdentifiableEntity<?> clone = abies.clone();
		assertNotNull(clone);
		assertEquals(clone.annotations, abies.annotations);
		assertEquals(clone.markers, abies.markers);
		assertFalse(clone.uuid.equals(abies.uuid));
	}
}