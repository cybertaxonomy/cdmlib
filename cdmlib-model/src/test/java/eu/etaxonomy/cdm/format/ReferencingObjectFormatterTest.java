/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Testclass for {@link ReferencingObjectFormatter}
 * <BR> #9999
 *
 * @author a.mueller
 * @date 24.03.2022
 */
public class ReferencingObjectFormatterTest extends TermTestBase {

    private Person person1;
    private TaxonName name1;
    private Taxon taxon1;
    private TaxonDescription taxonDescription1;
    private Reference reference1;

    @Before
    public void setUp() throws Exception {
        person1 = Person.NewTitledInstance("Linne");
        name1 = NonViralNameParserImpl.NewInstance().parseReferencedName("Abies alba Mill., Sp. Pl.: 123. 1753");
        reference1 = ReferenceFactory.newBook();
        reference1.setAuthorship(person1);
        reference1.setTitle("Species plantarum");
        reference1.setDatePublished(TimePeriodParser.parseStringVerbatim("1753"));
        taxon1 = Taxon.NewInstance(name1, reference1);

        taxonDescription1 = TaxonDescription.NewInstance(taxon1);
        taxonDescription1.setTitleCache("Description for taxon1", true);
    }


    @Test
    public void testFormat_IdentifiableEntities() {
        //default is to return titleCache

        //Person
        Assert.assertEquals("Linne", defaultFormat(person1));

        //TaxonName
        Assert.assertEquals("Abies alba Mill.", defaultFormat(name1));

        //Taxon
        Assert.assertEquals("Abies alba Mill. sec. Linne 1753", defaultFormat(taxon1));

        //Reference
        Assert.assertEquals("Linne 1753: Species plantarum", defaultFormat(reference1));

        //TaxonDescription
        Assert.assertEquals("Description for taxon1", defaultFormat(taxonDescription1));

    }

    @Test
    public void testFormat_Descriptive() {

        //TextData
        TextData textData = TextData.NewInstance(Feature.DESCRIPTION());
        textData.putText(Language.ENGLISH(), "My text data");
        Assert.assertEquals("My text data", defaultFormat(textData));

        taxonDescription1.addElement(textData);
        Assert.assertEquals("My text data (Abies alba Mill. sec. Linne 1753)", defaultFormat(textData));

        //CategoricalData
        CategoricalData catData = CategoricalData.NewInstance(Feature.DESCRIPTION());
        //TODO
        Assert.assertEquals("", defaultFormat(catData));

        State state1 = State.NewInstance("State1", "State1", null);
        StateData stateData1 = catData.addStateData(state1);
        catData.addStateData(State.NewInstance("State2", "State2", null));
        Assert.assertEquals("State1", defaultFormat(stateData1));
        Assert.assertEquals("State1, State2", defaultFormat(catData));

        taxonDescription1.addElement(catData);
        Assert.assertEquals("State1, State2 (Abies alba Mill. sec. Linne 1753)", defaultFormat(catData));
        Assert.assertEquals("State1 (Abies alba Mill. sec. Linne 1753)", defaultFormat(stateData1));


    }

//    @Test
//    public void testFormatCdmBaseStringLanguage() {
//        fail("Not yet implemented");
//    }

    private String defaultFormat(CdmBase cdmBase) {
        return ReferencingObjectFormatter.format(cdmBase, Language.ENGLISH());
    }

}
