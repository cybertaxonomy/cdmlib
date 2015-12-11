package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@Ignore //FIXME Remove @Ignore once maximes code is completely comitted
public class NaturalLanguageGeneratorTest extends CdmIntegrationTest {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NaturalLanguageGeneratorTest.class);
	
	@SpringBeanByType
	private INaturalLanguageGenerator generator;
	
    private FeatureTree featureTree;
    private TaxonDescription description;
    Set<Feature> featureSet  = new HashSet<Feature>();

	@Before
	public void setUp() throws Exception {
        // set up your test objects here
		
		NonViralName tnb = NonViralName.NewInstance(null);
		Taxon taxon = Taxon.NewInstance(tnb, null);
		description = TaxonDescription.NewInstance(taxon);
		
		featureTree= FeatureTree.NewInstance();
		FeatureNode root = FeatureNode.NewInstance();
		featureTree.setRoot(root);
		String[][][] tableStrings = { { {"a","b"} } , { { "a1" , "a2"  } , { "b1" } } };
		buildBranches(root,tableStrings,0,2,0);
		for (Iterator<Feature> f = featureSet.iterator() ; f.hasNext() ;){
			Feature feature = f.next();
			CategoricalData cg = CategoricalData.NewInstance();
			cg.setFeature(feature);
			State state = State.NewInstance(null, feature.getLabel()+"state", null);
			StateData stateData = StateData.NewInstance();
			stateData.setState(state);
			cg.addStateData(stateData);
			description.addElement(cg);
		}
		Feature qFeature = Feature.NewInstance(null, "c", null);
		QuantitativeData qd = QuantitativeData.NewInstance();
		MeasurementUnit munit = MeasurementUnit.NewInstance(null, "mm", null);
		StatisticalMeasurementValue smv = StatisticalMeasurementValue.NewInstance();
		smv.setType(StatisticalMeasure.AVERAGE());
		smv.setValue(12);
		qd.addStatisticalValue(smv);
		qd.setUnit(munit);
		qd.setFeature(qFeature);
		description.addElement(qd);
		FeatureNode qNode = FeatureNode.NewInstance(qFeature);
		root.addChild(qNode);
	}
	
	@Test
	public void testGenerateNaturalLanguageDescription() {
		assertNotNull("FeatureTree should exist", featureTree);
		assertNotNull("TaxonDescription should exist", description);
		StringBuilder stringBuilder = new StringBuilder();
		List<TextData> result = generator.generateNaturalLanguageDescription(featureTree,description, Language.DEFAULT());
		for (Iterator<TextData> td = result.iterator() ; td.hasNext();) {
			TextData textD = td.next();
			stringBuilder.append(textD.getText(Language.DEFAULT()));
		}
		assertTrue("Empty text",!stringBuilder.equals(""));
		System.out.println(stringBuilder.toString());
	}
	
	public void buildBranches(FeatureNode parent, String[][][] children, int level, int depth, int nodeNumber) {
		int i = nodeNumber;
		int j;
				for (j=0; j<children[level][i].length ; j++) {
					Feature feature = Feature.NewInstance(null, children[level][i][j], null);
					featureSet.add(feature);
					FeatureNode child = FeatureNode.NewInstance(feature);
					parent.addChild(child);
					if (level<depth-1) {
						buildBranches(child, children,level+1,depth, j);
					}
			}
		
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }
	
}
