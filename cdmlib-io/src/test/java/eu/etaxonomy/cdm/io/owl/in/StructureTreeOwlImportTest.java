/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.owl.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.descriptive.owl.in.StructureTreeOwlImportConfigurator;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 */
public class StructureTreeOwlImportTest extends CdmIoTransactionalSpringIntegrationTest {

    @Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

    @Autowired
    private ITermService termService;

    @Autowired
    private ITermTreeService termTreeServcie;

    @Autowired
    private IVocabularyService vocabularyService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testInit() {
        assertNotNull(defaultImport, "import should not be null");
    }

    @Test
    @CdmDataSet("/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testImportStructureTree() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/test_structures.owl");
        URI uri = URI.fromUrl(url);
        assertNotNull(url);
        StructureTreeOwlImportConfigurator importConfig = StructureTreeOwlImportConfigurator.NewInstance(uri);

        boolean result = defaultImport.invoke(importConfig).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        this.setComplete();
        this.endTransaction();

        String treeLabel = "test_structures";
        List<TermTree> trees = termTreeServcie.listByTitle(TermTree.class, treeLabel, MatchMode.EXACT, null, null, null, null, null);
        List<String> nodePath = Arrays.asList(new String[] {"term", "term.media"});
        @SuppressWarnings({ "unchecked", "rawtypes" })
        TermTree<Feature> tree = (TermTree)termTreeServcie.loadWithNodes(trees.get(0).getUuid(), null, nodePath);
        assertNotNull(tree, "featureTree should not be null");

        assertEquals(TermType.Structure, tree.getTermType(), "Tree has wrong term type");
        assertEquals(4, tree.getDistinctTerms().size(), "Wrong number of distinct features");
        List<TermNode<Feature>> rootChildren = tree.getRootChildren();
        assertEquals(1, rootChildren.size(), "Wrong number of root children");
        TermNode<Feature> entirePlant = rootChildren.iterator().next();
        assertEquals(TermType.Structure, ((TermNode<?>)entirePlant).getTermType(), "Root node has wrong term type");

        TermNode<DefinedTerm> entirePlantNode = (TermNode) entirePlant;
        List<TermNode<DefinedTerm>> childNodes = entirePlantNode.getChildNodes();
        assertEquals(2, childNodes.size(), "Wrong number of children");

        String inflorescenceLabel = "inflorescence";
        String inflorescenceDescription = " the part of the plant that bears the flowers, including all its bracts  branches and flowers  but excluding unmodified leaves               ";
        List<DefinedTerm> records = termService.listByTitle(DefinedTerm.class, inflorescenceLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("representations", "media"));
        assertEquals(1, records.size(), "wrong number of terms found for \"inflorescence\"");
        DefinedTerm inflorescence = records.iterator().next();
        assertEquals(inflorescenceDescription, inflorescence.getDescription(Language.ENGLISH()));

        for (TermNode<DefinedTerm> termNode : childNodes) {
            assertTrue(termNode.getTerm().getUuid().equals(inflorescence.getUuid())
                    || termNode.getTerm().getLabel(Language.ENGLISH()).equals("Flower"), "Child node not found. Found node with term: "+termNode.getTerm().getLabel());
            if(termNode.getTerm().getUuid().equals(inflorescence.getUuid())){
                assertEquals(inflorescence, termNode.getTerm(), "Term mismatch");
                inflorescence = termNode.getTerm();

                assertEquals("inflorescence", inflorescence.getIdInVocabulary(), "wrong id in vocabulary");
                assertEquals("infloSymbol", inflorescence.getSymbol(), "wrong symbol");
                assertEquals("infloSymbol2", inflorescence.getSymbol2(), "wrong symbol2");

                Set<Media> mediaSet = inflorescence.getMedia();
                assertEquals(1, mediaSet.size(), "wrong number of media");
                Media media = mediaSet.iterator().next();
                MediaRepresentation rep = MediaUtils.getFirstMediaRepresentationPart(media);
                assertNotNull(rep, "media part not found");
                assertEquals(URI.create("https://upload.wikimedia.org/wikipedia/commons/8/82/Aloe_hereroensis_Auob_C15.JPG"), rep.getUri(), "incorrect URI");
                assertEquals("Aloe hereroensis", media.getTitle(Language.DEFAULT()).getText(), "incorrect title");

                Representation englishRepresentation = inflorescence.getRepresentation(Language.ENGLISH());
                assertTrue(CdmUtils.isNotBlank(englishRepresentation.getDescription()), "Description not found");
                assertEquals(inflorescenceDescription, englishRepresentation.getDescription(), "Description wrong");
                assertEquals("inflorescences", englishRepresentation.getPlural(), "wrong plural");
                assertEquals("inflo", englishRepresentation.getAbbreviatedLabel(), "wrong label abbrev");

                // german representation
                assertEquals(2, inflorescence.getRepresentations().size(), "wrong number of representations");
                Representation germanRepresentation = inflorescence.getRepresentation(Language.GERMAN());
                assertNotNull(germanRepresentation, "Representation is null for "+Language.GERMAN());
                assertEquals("Der Teil der Pflanze, der die Bluete traegt", germanRepresentation.getDescription(), "wrong description");
                assertEquals("Infloreszenz", germanRepresentation.getLabel(), "wrong label");
            }
        }
        assertNotNull(inflorescence, "term is null");
        assertEquals(TermType.Structure, inflorescence.getTermType(), "Wrong term type");

        String vocLabel = "03 Generative Structures";
        List<TermVocabulary> vocs = vocabularyService.findByTitle(TermVocabulary.class, vocLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("terms")).getRecords();
        assertEquals(1, vocs.size(), "wrong number of vocabularies");
        TermVocabulary<?> termVoc = vocs.iterator().next();
        assertEquals(vocLabel, termVoc.getTitleCache(), "Wrong vocabulary label");
        Collection<TermDto> topLevelTerms = vocabularyService.getTopLevelTerms(termVoc.getUuid());
        assertEquals(4, topLevelTerms.size(), "wrong number of top level terms");

    }

    @Test
    @CdmDataSet("/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testImportPropertyTreeAndVocabulary() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/properties.owl");
        URI uri = URI.fromUrl(url);
        assertNotNull(url);
        StructureTreeOwlImportConfigurator configurator = StructureTreeOwlImportConfigurator.NewInstance(uri);

        boolean result = defaultImport.invoke(configurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        this.setComplete();
        this.endTransaction();

        String treeLabel = "properties 1.0";
        List<TermTree<?>> trees = (List)termTreeServcie.listByTitle(TermTree.class, treeLabel, MatchMode.EXACT, null, null, null, null, null);
        List<String> nodeProperties = new ArrayList<>();
        nodeProperties.add("term");
        TermTree<?> tree = termTreeServcie.loadWithNodes(trees.iterator().next().getUuid(), null, nodeProperties);
        assertNotNull(tree, "featureTree should not be null");

        assertEquals(TermType.Property, tree.getTermType(), "Tree has wrong term type");
        assertEquals(12, tree.getDistinctTerms().size(), "Wrong number of distinct features");

        String vocLabel = "Plant Glossary Properties";
        List<TermVocabulary> vocs = vocabularyService.findByTitle(TermVocabulary.class, vocLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("terms")).getRecords();
        assertEquals(1, vocs.size(), "wrong number of vocabularies");
        TermVocabulary termVoc = vocs.iterator().next();
        assertEquals(vocLabel, termVoc.getTitleCache(), "Wrong vocabulary label");
        assertEquals(82, termVoc.getTerms().size());

        Set<DefinedTermBase> terms = termVoc.getTerms();
        for (DefinedTermBase definedTermBase : terms) {
            List<String> termProperties = new ArrayList<>();
            termProperties.add("sources");
            definedTermBase = termService.load(definedTermBase.getUuid(), termProperties);
            Set<IdentifiableSource> sources = definedTermBase.getSources();
            assertTrue(!sources.isEmpty(), "Import source is missing for term: "+definedTermBase);
            assertTrue(sources.stream().anyMatch(source->OriginalSourceType.Import.getKey().equals(source.getType().getKey())), "import source type not found");
        }

    }
    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}