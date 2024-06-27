/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author muellera
 * @since 27.06.2024
 */
public class NameTypeDesignationGroupFormatterTest extends TermTestBase {

    private static final String DASH_W = UTF8.EN_DASH_SPATIUM.toString();

    private static final boolean WITH_CITATION = true;
    private static final boolean WITH_NAME = true;
    private static final boolean WITH_TYPE_LABEL = true;
    private static final boolean WITH_PRECEDING_MAIN_TYPE = true;
    private static final boolean WITH_ACCESSION_NO_TYPE = true;

    //variables and setup were copied from TypeDesignationGroupContainerTest
    //not all of them are in use yet
    private NameTypeDesignation ntd;
    private NameTypeDesignation ntd_LT;
    private Reference book;

    @Before
    public void setUp() throws Exception {
        Person person1 = Person.NewInstance("DC", "Decandolle", "A.", null);
        Person person2 = Person.NewInstance("Hab.", "Haber", "M.", null);
        Person person3 = Person.NewInstance("Moler", "Moler", "A.P.", null);
        Team team = Team.NewInstance(person1, person2, person3);

        book = ReferenceFactory.newBook();
        book.setAuthorship(team);
        book.setTitle("My interesting book");
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("11 Apr 1962"));

        ntd = NameTypeDesignation.NewInstance();
        ntd.setId(1);
        TaxonName typeName = TaxonNameFactory.PARSED_BOTANICAL("Prionus coriatius L.");
        ntd.setTypeName(typeName);

//      Reference citation = ReferenceFactory.newGeneric();
//      citation.setTitleCache("Species Plantarum", true);
//      ntd.setCitation(citation);
//    ntd.addPrimaryTaxonomicSource(citation, null);

        ntd_LT = NameTypeDesignation.NewInstance();
        ntd_LT.setTypeStatus(NameTypeDesignationStatus.LECTOTYPE());
        TaxonName typeName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        typeName2.setTitleCache("Prionus arealus L.", true);
        ntd_LT.setTypeName(typeName2);
        ntd_LT.setCitation(book);
    }

    @Test
    public void testNameTypeDesignationTaggedText() throws TypeDesignationSetException {

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> tds = new ArrayList<>();
        tds.add(ntd);

        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);

        typifiedName.addTypeDesignation(ntd, false);

        TypeDesignationGroupContainer manager = TypeDesignationGroupContainer.NewDefaultInstance(tds);
        TypeDesignationGroupContainerFormatter formatter =
                new TypeDesignationGroupContainerFormatter(WITH_CITATION, WITH_TYPE_LABEL,
                        WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE);
        String text = formatter.format(manager);
        Assert.assertEquals("Prionus L."+DASH_W+"Type: Prionus coriatius L.", text);

        List<TaggedText> taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("first entry should be the typified name",
                new TaggedText(TagEnum.name, "Prionus L.",TypedEntityReferenceFactory.fromEntity(typifiedName, false)), taggedText.get(0));
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "Prionus"), taggedText.get(4));  //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fourth entry should be the name type nameCache",
                new TaggedText(TagEnum.name, "coriatius"), taggedText.get(5)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("fifth entry should be the name type authorship cache",
                new TaggedText(TagEnum.authors, "L."), taggedText.get(6));

        //protected titleCache
        ntd.getTypeName().setTitleCache("Prionus coriatius L.", true);
        taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("fourth entry should be the name type titleCache",
                new TaggedText(TagEnum.name, "Prionus coriatius L."), taggedText.get(4)); //maybe in future the entityReference should be TypedEntityReference.fromEntity(ntd.getTypeName(), false)
        Assert.assertEquals("there should be 5 tags only", 5, taggedText.size());
        ntd.getTypeName().setTitleCache(null, false);

        //with status
        ntd.getTypeName().addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
        formatter.withNameIfAvailable(false); //to simplify the evaluation
        text = formatter.format(manager);
        Assert.assertEquals("Type: Prionus coriatius L., nom. illeg.", text);

        taggedText = formatter.toTaggedText(manager);
        Assert.assertEquals("sixth entry should be the status separator",
                new TaggedText(TagEnum.separator, ", "), taggedText.get(5));
        Assert.assertEquals("seventh entry should be the abbreviated status",
                "nom. illeg.", taggedText.get(6).getText());
    }

    //see #9262, see also similar test in SpecimenTypeDesignationGroupFormatterTest
    @Test
    public void test_desigby_fide(){

        Reference citation = ReferenceFactory.newBook();
        Reference inRef = ReferenceFactory.newBookSection();
        inRef.setInBook(citation);
        citation.setDatePublished(TimePeriodParser.parseStringVerbatim("1989"));
        inRef.setAuthorship(Team.NewTitledInstance("Miller", "Mill."));

        //name types
        TaxonName typifiedName = TaxonNameFactory.NewBacterialInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);
        TypeDesignationGroupContainer typeDesignationContainer = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationContainer.addTypeDesigations(ntd_LT);
        ntd_LT.addPrimaryTaxonomicSource(inRef, "66");
        assertEquals("Prionus L."+DASH_W+"Lectotype (designated by Decandolle & al. 1962): Prionus arealus L. [fide Miller 1989: 66]",
                typeDesignationContainer.print(WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Lectotype: Prionus arealus L.",
                typeDesignationContainer.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
    }

    @Test
    public void test_withMissingStatus(){
        //name types
        TaxonName typifiedName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        typifiedName.setTitleCache("Prionus L.", true);
        TypeDesignationGroupContainer typeDesignationContainer = new TypeDesignationGroupContainer(typifiedName);
        typeDesignationContainer.addTypeDesigations(ntd);
        assertEquals("Prionus L."+DASH_W+"Type: Prionus coriatius L.",
                typeDesignationContainer.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Type: Prionus coriatius L.",
                typeDesignationContainer.print(WITH_CITATION, WITH_TYPE_LABEL, WITH_NAME, WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));
        assertEquals("Prionus L."+DASH_W+"Prionus coriatius L.",
                typeDesignationContainer.print(!WITH_CITATION, !WITH_TYPE_LABEL, WITH_NAME, !WITH_PRECEDING_MAIN_TYPE, !WITH_ACCESSION_NO_TYPE));

    }
}