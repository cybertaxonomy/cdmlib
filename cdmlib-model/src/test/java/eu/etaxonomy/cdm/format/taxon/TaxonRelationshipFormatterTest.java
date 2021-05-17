/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.taxon;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Test to test the {@link TaxonRelationshipFormatter}.
 *
 * @author a.mueller
 * @since 14.08.2018
 */
public class TaxonRelationshipFormatterTest extends TermTestBase{

    private static boolean WITHOUT_NAME = true;

    private TaxonRelationship taxonRel;
    private Reference relSec;

    private Taxon fromTaxon;
    private TaxonName fromName;
    private Reference fromSec;

    private Taxon toTaxon;
    private TaxonName toName;
    private Reference toSec;

    private TaxonRelationshipFormatter formatter;
    private boolean reverse;

    private Person toNameAuthor;
    private Person macFarlane;
    private Person cheek;
    private Person toSecAuthor;
    private List<Language> languages;

    @Before
    public void setUp() throws Exception {
        fromName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        fromName.setGenusOrUninomial("Abies");
        fromName.setSpecificEpithet("alba");

        toName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        toName.setGenusOrUninomial("Pinus");
        toName.setSpecificEpithet("pinova");
        toNameAuthor = Person.NewInstance("Mill.", "Miller", "A.", "Andrew");
        toName.setCombinationAuthorship(toNameAuthor);

        fromSec = ReferenceFactory.newGeneric();
        fromSec.setTitle("From Sec");
        String initials = "J.M.";
        macFarlane = Person.NewInstance(null, "Macfarlane", initials, null);
        fromSec.setAuthorship(macFarlane);
        fromSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1918));

        relSec = ReferenceFactory.newGeneric();
        relSec.setTitle("From rel reference");
        initials = null; //"M.R.";
        cheek = Person.NewInstance(null, "Cheek", initials, null);
        relSec.setAuthorship(cheek);
        relSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1919));

        toSec = ReferenceFactory.newGeneric();
        toSec.setTitle("To Sec");
        toSecAuthor = Person.NewTitledInstance("ToSecAuthor");
        toSec.setAuthorship(toSecAuthor);
        toSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1928));

        fromTaxon = Taxon.NewInstance(fromName, fromSec);
        toTaxon = Taxon.NewInstance(toName, toSec);

        TaxonRelationshipType type = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
        taxonRel = fromTaxon.addTaxonRelation(toTaxon, type, relSec, "123");
        reverse = false;

        formatter = TaxonRelationshipFormatter.INSTANCE();

        languages = null;
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter#getTaggedText(eu.etaxonomy.cdm.model.taxon.TaxonRelationship, boolean, java.util.List)}.
     */
    @Test
    public void testGetTaggedTextMisappliedName() {

        reverse = true;
        String inverseSymbol = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getInverseSymbol();
        String symbol = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getSymbol();

        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " \"Abies alba\" sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);

        //reverse
        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        //auctores
        fromTaxon.setAppendedPhrase("auctores");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " \"Abies alba\" auctores sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);

        fromTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " \"Abies alba\" auctores, err. sec. Cheek 1919: 123", str);

        fromTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " \"Abies alba\" auct., err. sec. Cheek 1919: 123", str);

        fromTaxon.setDoubtful(true);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(inverseSymbol + " ?\u202F\"Abies alba\" auct., err. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetTaggedTextConceptRelations() {

        reverse = false;

        TaxonRelationshipType relType = TaxonRelationshipType.INCLUDES();

        final String SYMBOL = relType.getSymbol();

        taxonRel.setType(relType);
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " Pinus pinova Mill. sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(relType.getInverseSymbol() + " Abies alba sec. Macfarlane 1918, rel. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("sensu stricto");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " Pinus pinova Mill. sensu stricto sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);


        toTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " Pinus pinova Mill. sensu stricto, rel. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " Pinus pinova Mill. sec. ???, rel. sec. Cheek 1919: 123", str);

        taxonRel.setDoubtful(true);
        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals("?" + SYMBOL + " Pinus pinova Mill. sec. ???, rel. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetTaggedTextSynonymRelations() {

        reverse = false;

        TaxonRelationshipType type = TaxonRelationshipType.PRO_PARTE_SYNONYM_FOR();
        final String symbol = type.getSymbol();


        taxonRel.setType(type);
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sec. ToSecAuthor 1928, syn. sec. Cheek 1919: 123", str);

        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(type.getInverseSymbol() + " Abies alba sec. Macfarlane 1918, syn. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("sensu lato");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sensu lato sec. ToSecAuthor 1928, syn. sec. Cheek 1919: 123", str);


        toTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sensu lato, syn. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sec. ???, syn. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetFamilyNames() {

        //Test start condition with single person
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertFalse("Formatted text should not contain the team correctly formatted", str.contains("Macfarlane & Cheek"));

        //use team
        Team secRelTeam = Team.NewInstance();
        secRelTeam.addTeamMember(macFarlane);
        secRelTeam.addTeamMember(cheek);
        relSec.setAuthorship(secRelTeam);

        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertTrue(str.contains("rel. sec. Macfarlane & Cheek 1919"));

        //add third member
        secRelTeam.addTeamMember(toSecAuthor);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertTrue(str.contains("rel. sec. Macfarlane & al. 1919"));

        //add et al.
        secRelTeam.setHasMoreMembers(true);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertTrue(str.contains("rel. sec. Macfarlane & al. 1919"));

    }

    @Test
    public void testGetTaggedTextMisappliedNameWithoutName() {

        reverse = true;
        String inverseSymbol = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getInverseSymbol();
        String symbol = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getSymbol();

        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);

        //reverse
        tags = formatter.getTaggedText(taxonRel, !reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(symbol + " sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        //auctores
        fromTaxon.setAppendedPhrase("auctores");
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " auctores sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);

        fromTaxon.setSec(null);
        fromTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(inverseSymbol + " auct., err. sec. Cheek 1919: 123", str);

        fromTaxon.setDoubtful(true);
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(inverseSymbol + " ?\u202F auct., err. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetTaggedTextConceptRelationsWithoutName() {

        reverse = false;

        TaxonRelationshipType relType = TaxonRelationshipType.INCLUDES();

        final String SYMBOL = relType.getSymbol();

        taxonRel.setType(relType);
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        String str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        tags = formatter.getTaggedText(taxonRel, !reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(relType.getInverseSymbol() + " sec. Macfarlane 1918, rel. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("sensu stricto");
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " sensu stricto sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        toTaxon.setSec(null);
        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals(SYMBOL + " sec. ???, rel. sec. Cheek 1919: 123", str);

        taxonRel.setDoubtful(true);
        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages, WITHOUT_NAME);
        str = TaggedCacheHelper.createString(tags);
        Assert.assertEquals("?" + SYMBOL + " sec. ???, rel. sec. Cheek 1919: 123", str);

    }

}
