/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
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

/**
 * Test to test the {@link TaxonRelationshipFormatter}.
 *
 * @author a.mueller
 * @since 14.08.2018
 */
public class TaxonRelationshipFormatterTest {

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

    Person toNameAuthor;
    private List<Language> languages;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }
    }

    /**
     * @throws java.lang.Exception
     */
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
        fromSec.setAuthorship(Person.NewInstance(null, "Macfarlane", initials, null));
        fromSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1918));

        relSec = ReferenceFactory.newGeneric();
        relSec.setTitle("From rel reference");
        initials = null; //"M.R.";
        relSec.setAuthorship(Person.NewInstance(null, "Cheek", initials, null));
        relSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1919));

        toSec = ReferenceFactory.newGeneric();
        toSec.setTitle("To Sec");
        toSec.setAuthorship(Person.NewTitledInstance("ToSecAuthor"));
        toSec.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1928));

        fromTaxon = Taxon.NewInstance(fromName, fromSec);
        toTaxon = Taxon.NewInstance(toName, toSec);

        TaxonRelationshipType type = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
        taxonRel = fromTaxon.addTaxonRelation(toTaxon, type, relSec, "123");
        reverse = false;

        formatter = new TaxonRelationshipFormatter();

        languages = null;
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter#getTaggedText(eu.etaxonomy.cdm.model.taxon.TaxonRelationship, boolean, java.util.List)}.
     */
    @Test
    public void testGetTaggedTextMisappliedName() {

        reverse = true;
        String symbol = TaxonRelationshipType.MISAPPLIED_NAME_FOR().getInverseSymbol();
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        //TODO no initials
        Assert.assertEquals(symbol + " \"Abies alba\" sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);

        //reverse
        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals("– Pinus pinova Mill. sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);
        //FIXME symbol


        //auctores
        fromTaxon.setAppendedPhrase("auctores");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " \"Abies alba\" auctores sensu Macfarlane 1918, err. sec. Cheek 1919: 123", str);



        fromTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " \"Abies alba\" auctores, err. sec. Cheek 1919: 123", str);

        fromTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " \"Abies alba\" auct., err. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetTaggedTextConceptRelations() {

        reverse = false;

        final String CONGRUENT = TaxonRelationshipType.CONGRUENT_TO().getSymbol();

        taxonRel.setType(TaxonRelationshipType.CONGRUENT_TO());
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        //TODO no initials
        Assert.assertEquals(CONGRUENT + " Pinus pinova Mill. sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);

        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        //FIXME symbol

        toTaxon.setAppendedPhrase("sensu stricto");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(CONGRUENT + " Pinus pinova Mill. sensu stricto sec. ToSecAuthor 1928, rel. sec. Cheek 1919: 123", str);


        toTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(CONGRUENT + " Pinus pinova Mill. sensu stricto, rel. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(CONGRUENT + " Pinus pinova Mill. sec. ???, rel. sec. Cheek 1919: 123", str);

    }

    @Test
    public void testGetTaggedTextSynonymRelations() {

        reverse = false;

        TaxonRelationshipType type = TaxonRelationshipType.PRO_PARTE_SYNONYM_FOR();
        final String symbol = type.getSymbol();


        taxonRel.setType(type);
        List<TaggedText> tags = formatter.getTaggedText(taxonRel, reverse, languages);
        String str = TaggedCacheHelper.createString(tags);
        //TODO no initials
        Assert.assertEquals(symbol + " Pinus pinova Mill. sec. ToSecAuthor 1928, syn. sec. Cheek 1919: 123", str);

        tags = formatter.getTaggedText(taxonRel, !reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        //FIXME symbol
        Assert.assertEquals(type.getInverseSymbol() + " Abies alba sec. Macfarlane 1918, syn. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("sensu lato");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sensu lato sec. ToSecAuthor 1928, syn. sec. Cheek 1919: 123", str);


        toTaxon.setSec(null);
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sensu lato, syn. sec. Cheek 1919: 123", str);

        toTaxon.setAppendedPhrase("");
        tags = formatter.getTaggedText(taxonRel, reverse, languages);
        str = TaggedCacheHelper.createString(tags);
        System.out.println(str);
        Assert.assertEquals(symbol + " Pinus pinova Mill. sec. ???, syn. sec. Cheek 1919: 123", str);

    }

}
