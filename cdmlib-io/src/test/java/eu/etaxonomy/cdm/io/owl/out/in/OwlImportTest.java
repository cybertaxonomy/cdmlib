/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.owl.out.in;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.descriptive.owl.in.OwlImport;
import eu.etaxonomy.cdm.io.descriptive.owl.in.OwlImportConfigurator;
import eu.etaxonomy.cdm.io.descriptive.owl.in.OwlImportState;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 *
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class OwlImportTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private OwlImport owlImport;

    private OwlImportConfigurator configurator;

    @Before
    public void setUp() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/test_structures.owl");
		URI uri = url.toURI();
		assertNotNull(url);
		configurator = OwlImportConfigurator.NewInstance(uri);
    }

    @Test
    public void testInit() {
        assertNotNull("owlImport should not be null", owlImport);
    }

    @Test
    @DataSet(value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
	public void testDoInvoke() {
        OwlImportState state = new OwlImportState(configurator);
        owlImport.doInvoke(state);
        this.setComplete();
        this.endTransaction();

        Collection<FeatureTree> featureTrees = state.getFeatureTrees();
        assertTrue("Wrong number of feature trees imported", featureTrees.size()==1);
        FeatureTree tree = featureTrees.iterator().next();
        assertTrue("Tree has wrong term type", tree.getTermType().equals(TermType.Structure));
        assertTrue("Wrong number of distinct features", tree.getDistinctFeatures().size()==4);
        List rootChildren = tree.getRootChildren();
        assertTrue("Wrong number of root children", rootChildren.size()==1);
        Object root = rootChildren.iterator().next();
        assertTrue("Root is no feature node", root instanceof FeatureNode);
        assertTrue("Root node has wrong term type", ((FeatureNode)root).getTermType().equals(TermType.Structure));
        FeatureNode<DefinedTerm> rootNode = (FeatureNode<DefinedTerm>) root;
        List<FeatureNode<DefinedTerm>> childNodes = rootNode.getChildNodes();
        assertTrue("Wrong number of children", childNodes.size()==2);
        for (FeatureNode<DefinedTerm> featureNode : childNodes) {
            assertTrue("Child node not found",
                    featureNode.getTerm().getLabel().equals("inflorescence")
                    || featureNode.getTerm().getLabel().equals("Flower"));
            if(featureNode.getTerm().getLabel().equals("inflorescence")){
                assertTrue("Description not found", CdmUtils.isNotBlank(featureNode.getTerm().getDescription()));
                assertTrue("Description wrong", featureNode.getTerm().getDescription().equals(" the part of the plant that bears the flowers, including all its bracts  branches and flowers  but excluding unmodified leaves               "));
            }
        }

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
