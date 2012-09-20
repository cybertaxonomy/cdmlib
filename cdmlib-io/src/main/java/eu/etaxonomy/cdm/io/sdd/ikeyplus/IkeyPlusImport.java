// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.ikeyplus;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import fr.lis.ikeyplus.IO.SDDSaxParser;
import fr.lis.ikeyplus.model.DataSet;
import fr.lis.ikeyplus.model.ICharacter;
import fr.lis.ikeyplus.model.QuantitativeCharacter;
import fr.lis.ikeyplus.model.QuantitativeMeasure;
import fr.lis.ikeyplus.model.SingleAccessKeyNode;
import fr.lis.ikeyplus.model.SingleAccessKeyTree;
import fr.lis.ikeyplus.model.Taxon;
import fr.lis.ikeyplus.services.IdentificationKeyGenerator;
import fr.lis.ikeyplus.utils.Utils;

/**
 * @author andreas
 * @date Sep 18, 2012
 *
 */
@Component
public class IkeyPlusImport extends CdmImportBase<IkeyPlusImportConfigurator, IkeyPlusImportState>{

    public static final Logger logger = Logger.getLogger(IkeyPlusImport.class);

    private TermVocabulary<Feature> termVoc;

    private PolytomousKey cdmKey;

    public PolytomousKey getCdmKey() {
        return cdmKey;
    }

    public void setCdmKey(PolytomousKey cdmKey) {
        this.cdmKey = cdmKey;
    }

    private Map<String, Feature>featureMap = new HashMap<String, Feature>();

    public IkeyPlusImport() {

    }

    /**
     * @param sddUri
     * @param utils
     * @return
     * @throws Exception
     */
    /**
     * @param sddUri
     * @param utils
     * @return
     * @throws Exception
     */
    public PolytomousKey getKey(URI sddUri, Utils utils) throws Exception {

        //TODO move below into configurator
        utils = new Utils();
//        utils.setErrorMessage("foobar"); // Don't do it !!!! risk of NPE
        utils.setFewStatesCharacterFirst(false);
        utils.setMergeCharacterStatesIfSameDiscrimination(false);
        utils.setPruning(false);
        utils.setWeightContext("");
        utils.setWeightType(Utils.GLOBAL_CHARACTER_WEIGHT);

        SDDSaxParser sDDSaxParser = new SDDSaxParser(sddUri.toString(), utils);


        DataSet data = sDDSaxParser.getDataset();

        IdentificationKeyGenerator IkeyGenerator;
        try {
            IkeyGenerator = new IdentificationKeyGenerator(data, utils);
        } catch (Exception e) {
            logger.error("could not generate key", e);
            throw new RuntimeException(e);
        }
        try {
            IkeyGenerator.createIdentificationKey();
        } catch (Exception e) {
            //* IGNORE THIS TIME TO PREVENT FROM CREATING AN ERROR FILE */
        }
        SingleAccessKeyTree singleAccessKey = IkeyGenerator.getSingleAccessKeyTree();

        // TODO idInSource for any cdm entity

        cdmKey = PolytomousKey.NewTitledInstance(singleAccessKey.getLabel());

        termVoc = TermVocabulary.NewInstance(singleAccessKey.getLabel(), singleAccessKey.getLabel(), null, null);

        Set<PolytomousKeyNode> rootNode = recursivlyCreateKeyNodes(singleAccessKey.getRoot());
        Assert.assertEquals(1, rootNode.size());
        cdmKey.setRoot(rootNode.iterator().next());

        persistNewEntities();



        return null;


    }

    private void persistNewEntities() {


        // persist features
        Collection features = featureMap.values();
        getTermService().saveOrUpdate(features);
        getVocabularyService().saveOrUpdate(termVoc);

        // persist the rest
        getPolytomousKeyService().saveOrUpdate(cdmKey);
    }

    /**
     * @param node
     * @return
     */
    private Set<PolytomousKeyNode> recursivlyCreateKeyNodes(SingleAccessKeyNode node) {


        Set<PolytomousKeyNode> pkNodeSet = new HashSet<PolytomousKeyNode>();
        if(node == null){
            return pkNodeSet;
        }

        Feature feature = null;
        if(node.getCharacter() != null){
            feature = getFeatureFrom(node.getCharacter());
        }
        KeyStatement statement = getKeyStatementFrom(node);


        //node.getNodeDescription(); // not needed here, contains warnings etc

        List<SingleAccessKeyNode> childNodes = node.getChildren();
        PolytomousKeyNode pkNode;
        if(childNodes == null || childNodes.size() == 0 ){
            // do the taxa
            List<Taxon> taxa = node.getRemainingTaxa();
            for(Taxon taxon : taxa){

                pkNode = createPkNode(feature, statement);

                NonViralName nonViralName = NonViralName.NewInstance(null);
                nonViralName.setTitleCache(taxon.getName(), true);
                eu.etaxonomy.cdm.model.taxon.Taxon cdmTaxon = eu.etaxonomy.cdm.model.taxon.Taxon.NewInstance(
                        nonViralName, null); //FIXME !!!!!!
                // TODO add taxon to covered taxa
                // TODO media

                pkNode.setTaxon(cdmTaxon);
                pkNodeSet.add(pkNode);
            }
        } else {
            // do the children
            pkNode = createPkNode(feature, statement);
            for(SingleAccessKeyNode childNode : childNodes){

                Set<PolytomousKeyNode> nodesToAdd = recursivlyCreateKeyNodes(childNode);
                for(PolytomousKeyNode nodeToAdd : nodesToAdd){
                    pkNode.addChild(nodeToAdd);
                }

            }
            pkNodeSet.add(pkNode);
        }


        return pkNodeSet;
    }

    /**
     * @param feature
     * @param statement
     * @return
     */
    public PolytomousKeyNode createPkNode(Feature feature, KeyStatement statement) {
        PolytomousKeyNode pkNode;
        pkNode = PolytomousKeyNode.NewInstance();
        pkNode.setKey(cdmKey);
        pkNode.setFeature(feature);
        pkNode.setStatement(statement);
        return pkNode;
    }

    /**
     * @param node
     * @return
     */
    public KeyStatement getKeyStatementFrom(SingleAccessKeyNode node) {
        String label;
        if(node.getCharacterState() instanceof QuantitativeMeasure){
            label = ((QuantitativeMeasure) node.getCharacterState())
                    .toStringInterval(((QuantitativeCharacter) node.getCharacter())
                            .getMeasurementUnit());
        } else {
            label = node.getStringStates();
        }
        if (StringUtils.isBlank(label)){
            return null;
        }else{
            return KeyStatement.NewInstance(label);
        }
    }

    /**
     * @param character
     * @return
     */
    private Feature getFeatureFrom(ICharacter character) {


        if(!featureMap.containsKey(character.getId())){

            String featureLabel = character.getName();

            Feature newFeature = Feature.NewInstance(featureLabel, featureLabel, null);
            termVoc.addTerm(newFeature);
            featureMap.put(character.getId(),
                    newFeature);

        }
        return featureMap.get(character.getId());
    }

    @Override
    protected void doInvoke(IkeyPlusImportState state) {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean doCheck(IkeyPlusImportState state) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean isIgnore(IkeyPlusImportState state) {
        // TODO Auto-generated method stub
        return false;
    }

}
