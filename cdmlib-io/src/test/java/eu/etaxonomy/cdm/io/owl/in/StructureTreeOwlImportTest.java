/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.owl.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.descriptive.owl.in.StructureTreeOwlImportConfigurator;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 *
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class StructureTreeOwlImportTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByName
    private CdmApplicationAwareDefaultImport<?> defaultImport;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IFeatureTreeService featureTreeService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @Before
    public void setUp() {
    }

    @Test
    public void testInit() {
        assertNotNull("import should not be null", defaultImport);
    }

    @Test
    @DataSet(value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testImportStructureTree() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/test_structures.owl");
        URI uri = url.toURI();
        assertNotNull(url);
        StructureTreeOwlImportConfigurator configurator = StructureTreeOwlImportConfigurator.NewInstance(uri);

        boolean result = defaultImport.invoke(configurator).isSuccess();
        assertTrue("Return value for import.invoke should be true", result);
        this.setComplete();
        this.endTransaction();

        String treeLabel = "test_structures";
        List<FeatureTree> trees = featureTreeService.listByTitle(FeatureTree.class, treeLabel, MatchMode.EXACT, null, null, null, null, null);
        List<String> nodeProperties = new ArrayList<>();
        nodeProperties.add("term");
        nodeProperties.add("term.media");
        FeatureTree tree = featureTreeService.loadWithNodes(trees.iterator().next().getUuid(), null, nodeProperties);
        assertNotNull("featureTree should not be null", tree);

        assertEquals("Tree has wrong term type", TermType.Structure, tree.getTermType());
        assertEquals("Wrong number of distinct features", 4, tree.getDistinctFeatures().size());
        List rootChildren = tree.getRootChildren();
        assertEquals("Wrong number of root children", 1, rootChildren.size());
        Object entirePlant = rootChildren.iterator().next();
        assertTrue("Root is no feature node", entirePlant instanceof FeatureNode);
        assertEquals("Root node has wrong term type", TermType.Structure, ((FeatureNode)entirePlant).getTermType());
        FeatureNode<DefinedTerm> entirePlantNode = (FeatureNode<DefinedTerm>) entirePlant;
        List<FeatureNode<DefinedTerm>> childNodes = entirePlantNode.getChildNodes();
        assertEquals("Wrong number of children", 2, childNodes.size());

        String inflorescenceLabel = "inflorescence";
        String inflorescenceDescription = " the part of the plant that bears the flowers, including all its bracts  branches and flowers  but excluding unmodified leaves               ";
        List<DefinedTerm> records = termService.findByRepresentationText(inflorescenceDescription, DefinedTerm.class, null, null).getRecords();
        assertEquals("wrong number of terms found for \"inflorescence\"", 1, records.size());
        DefinedTerm inflorescence = records.iterator().next();
        assertEquals(inflorescenceLabel, inflorescence.getLabel(Language.ENGLISH()));

        for (FeatureNode<DefinedTerm> featureNode : childNodes) {
            assertTrue("Child node not found. Found node with term: "+featureNode.getTerm().getLabel(),
                    featureNode.getTerm().getUuid().equals(inflorescence.getUuid())
                    || featureNode.getTerm().getLabel(Language.ENGLISH()).equals("Flower"));
            if(featureNode.getTerm().getUuid().equals(inflorescence.getUuid())){
                assertEquals("Term mismatch", inflorescence, featureNode.getTerm());
                inflorescence = featureNode.getTerm();

                assertEquals("wrong id in vocabulary", "inflorescence", inflorescence.getIdInVocabulary());
                assertEquals("wrong symbol", "infloSymbol", inflorescence.getSymbol());
                assertEquals("wrong symbol2", "infloSymbol2", inflorescence.getSymbol2());

                Set<Media> mediaSet = inflorescence.getMedia();
                assertEquals("wrong number of media", 1, mediaSet.size());
                Media media = mediaSet.iterator().next();
                MediaRepresentationPart part = MediaUtils.getFirstMediaRepresentationPart(media);
                assertNotNull("media part not found", part);
                assertEquals("incorrect URI", URI.create("https://upload.wikimedia.org/wikipedia/commons/8/82/Aloe_hereroensis_Auob_C15.JPG"), part.getUri());
                assertEquals("incorrect title", "Aloe hereroensis", media.getTitle(Language.DEFAULT()).getText());

                Representation englishRepresentation = inflorescence.getRepresentation(Language.ENGLISH());
                assertTrue("Description not found", CdmUtils.isNotBlank(englishRepresentation.getDescription()));
                assertEquals("Description wrong", inflorescenceDescription, englishRepresentation.getDescription());
                assertEquals("wrong plural", "inflorescences", englishRepresentation.getPlural());
                assertEquals("wrong label abbrev", "inflo", englishRepresentation.getAbbreviatedLabel());

                // german representation
                assertEquals("wrong number of representations", 2, inflorescence.getRepresentations().size());
                Representation germanRepresentation = inflorescence.getRepresentation(Language.GERMAN());
                assertNotNull("Representation is null for "+Language.GERMAN(), germanRepresentation);
                assertEquals("wrong description", "Der Teil der Pflanze, der die Bluete traegt", germanRepresentation.getDescription());
                assertEquals("wrong label", "Infloreszenz", germanRepresentation.getLabel());
            }
        }
        assertNotNull("term is null", inflorescence);
        assertEquals("Wrong term type", TermType.Structure, inflorescence.getTermType());

        String vocLabel = "03 Generative Structures";
        List<TermVocabulary> vocs = vocabularyService.findByTitle(TermVocabulary.class, vocLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("terms")).getRecords();
        assertEquals("wrong number of vocabularies", 1, vocs.size());
        TermVocabulary termVoc = vocs.iterator().next();
        assertEquals("Wrong vocabulary label", vocLabel, termVoc.getTitleCache());

    }


    @Test
    @DataSet(value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testImportPropertyTreeAndVocabulary() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/properties.owl");
        URI uri = url.toURI();
        assertNotNull(url);
        StructureTreeOwlImportConfigurator configurator = StructureTreeOwlImportConfigurator.NewInstance(uri);

        boolean result = defaultImport.invoke(configurator).isSuccess();
        assertTrue("Return value for import.invoke should be true", result);
        this.setComplete();
        this.endTransaction();

        String treeLabel = "properties 1.0";
        List<FeatureTree> trees = featureTreeService.listByTitle(FeatureTree.class, treeLabel, MatchMode.EXACT, null, null, null, null, null);
        List<String> nodeProperties = new ArrayList<>();
        nodeProperties.add("term");
        FeatureTree tree = featureTreeService.loadWithNodes(trees.iterator().next().getUuid(), null, nodeProperties);
        assertNotNull("featureTree should not be null", tree);

        assertEquals("Tree has wrong term type", TermType.Property, tree.getTermType());
        assertEquals("Wrong number of distinct features", 12, tree.getDistinctFeatures().size());
        List rootChildren = tree.getRootChildren();

        String vocLabel = "Plant Glossary Properties";
        List<TermVocabulary> vocs = vocabularyService.findByTitle(TermVocabulary.class, vocLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("terms")).getRecords();
        assertEquals("wrong number of vocabularies", 1, vocs.size());
        TermVocabulary termVoc = vocs.iterator().next();
        assertEquals("Wrong vocabulary label", vocLabel, termVoc.getTitleCache());
        assertEquals(82, termVoc.getTerms().size());

        Set<DefinedTermBase> terms = termVoc.getTerms();
        for (DefinedTermBase definedTermBase : terms) {
            List<String> termProperties = new ArrayList<>();
            termProperties.add("sources");
            definedTermBase = termService.load(definedTermBase.getUuid(), termProperties);
            Set<IdentifiableSource> sources = definedTermBase.getSources();
            assertTrue("Import source is missing for term: "+definedTermBase, !sources.isEmpty());
            assertTrue("import source type not found", sources.stream().anyMatch(source->OriginalSourceType.Import.getKey().equals(source.getType().getKey())));
        }

    }
    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
