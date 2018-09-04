/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.dto.TaxonRelationshipsDTO.TaxonRelationDTO;
import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 15.08.2018
 *
 */
public class TaxonRelationshipsDTOTest {
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

    @Test
    public void test() {
        //2
        Reference sec2 = ReferenceFactory.newGeneric();
        sec2.setAuthorship(Person.NewInstance("Mue.", "Mueller", "I.", "Inger"));
        sec2.setDatePublished(TimePeriodParser.parseStringVerbatim("1987"));
        Taxon from2 = Taxon.NewInstance(fromName, sec2);
        TaxonRelationship rel2 = toTaxon.addMisappliedName(from2, sec2, "333");
        //3
        //same as rel1 except for sec
        TaxonRelationship rel3 = toTaxon.addMisappliedName(from2, taxonRel.getCitation(), taxonRel.getCitationMicroReference());

        Taxon taxon2 = Taxon.NewInstance(fromName, sec2);
        Taxon taxon2Dupl = Taxon.NewInstance(fromName, toSec);

        //4
        TaxonRelationship rel4 = toTaxon.addMisappliedName(taxon2, null, null);
        TaxonRelationship rel5 = toTaxon.addMisappliedName(taxon2Dupl, null, null);


        TaxonRelationshipsDTO dto = new TaxonRelationshipsDTO();

        dto.addRelation(taxonRel, Direction.relatedFrom, languages);
        dto.addRelation(rel2, Direction.relatedFrom, languages);
        TaxonRelationDTO relToDuplicate = dto.addRelation(rel3, Direction.relatedFrom, languages);
        dto.addRelation(rel4, Direction.relatedFrom, languages);
        TaxonRelationDTO duplicateWithoutRelSec2 = dto.addRelation(rel5, Direction.relatedFrom, languages);

        dto.createMisapplicationString();

        List<List<TaggedText>> misapplications = dto.getMisapplications();
        Assert.assertEquals("2 of 5 misapplications must be deduplicated", 3, misapplications.size());  //2/5 were deduplicated

        List<TaggedText> deduplicated = misapplications.get(0);
        Assert.assertEquals(13, deduplicated.size());
        Assert.assertSame(relToDuplicate.getTaggedText().get(6), deduplicated.get(8));

        Assert.assertEquals(11, misapplications.get(1).size());

        deduplicated = misapplications.get(2);
        Assert.assertEquals(9, deduplicated.size());
        Assert.assertSame(duplicateWithoutRelSec2.getTaggedText().get(6), deduplicated.get(8));


    }

}
