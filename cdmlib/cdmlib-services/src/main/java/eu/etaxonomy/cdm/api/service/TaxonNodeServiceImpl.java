// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class TaxonNodeServiceImpl extends AnnotatableServiceBase<TaxonNode, ITaxonNodeDao> implements ITaxonNodeService{
    private static final Logger logger = Logger.getLogger(TaxonNodeServiceImpl.class);

    @Autowired
    private IBeanInitializer defaultBeanInitializer;

    private Comparator<? super TaxonNode> taxonNodeComparator;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IClassificationService classService;

    @Autowired
    public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
        this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
    }

    @Override
    public TaxonNode getTaxonNodeByUuid(UUID uuid) {
        return dao.findByUuid(uuid);
    }

    @Override
    public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
            List<String> propertyPaths) {
        taxonNode = dao.load(taxonNode.getUuid());
        List<TaxonNode> childNodes = new ArrayList<TaxonNode>(taxonNode.getChildNodes());
        Collections.sort(childNodes, taxonNodeComparator);
        defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
        return childNodes;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
     */
    @Override
    @Autowired
    protected void setDao(ITaxonNodeDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#makeTaxonSynonym(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    @Override
    @Transactional(readOnly = false)
    public Synonym makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymRelationshipType synonymRelationshipType, Reference citation, String citationMicroReference)  {

        // TODO at the moment this method only moves synonym-, concept relations and descriptions to the new accepted taxon
        // in a future version we also want to move cdm data like annotations, marker, so., but we will need a policy for that
        if (oldTaxonNode == null || newAcceptedTaxonNode == null || oldTaxonNode.getTaxon().getName() == null){
            throw new IllegalArgumentException("A mandatory parameter was null.");
        }

        if(oldTaxonNode.equals(newAcceptedTaxonNode)){
            throw new IllegalArgumentException("Taxon can not be made synonym of its own.");
        }
        
        
       
        Taxon oldTaxon = (Taxon) HibernateProxyHelper.deproxy(oldTaxonNode.getTaxon());
        Taxon newAcceptedTaxon = (Taxon) HibernateProxyHelper.deproxy(newAcceptedTaxonNode.getTaxon());
        
        // Move oldTaxon to newTaxon
        //TaxonNameBase<?,?> synonymName = oldTaxon.getName();
        TaxonNameBase<?,?> synonymName = (TaxonNameBase)HibernateProxyHelper.deproxy(oldTaxon.getName());
        HomotypicalGroup group = synonymName.getHomotypicalGroup();
        if (synonymRelationshipType == null){
            if (synonymName.isHomotypic(newAcceptedTaxon.getName())){
                synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
            }else{
                synonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            }
        }

        //set homotypic group
        HomotypicalGroup newAcceptedTaxonHomotypicalgroup = newAcceptedTaxon.getHomotypicGroup();
       HibernateProxyHelper.deproxy(newAcceptedTaxonHomotypicalgroup);
       HibernateProxyHelper.deproxy(newAcceptedTaxon.getName());
        // Move Synonym Relations to new Taxon
        SynonymRelationship synonmyRelationship = newAcceptedTaxon.addSynonymName(synonymName,
                synonymRelationshipType, citation, citationMicroReference);
         HomotypicalGroup homotypicalGroupAcceptedTaxon = synonmyRelationship.getSynonym().getHomotypicGroup();
        // Move Synonym Relations to new Taxon
        // From ticket 3163 we can move taxon with accepted name having homotypic synonyms
        List<Synonym> synonymsInHomotypicalGroup = null;

        //the synonyms of the homotypical group of the old taxon
        if (synonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
        	synonymsInHomotypicalGroup = oldTaxon.getSynonymsInGroup(group);
        }

        for(SynonymRelationship synRelation : oldTaxon.getSynonymRelations()){
            SynonymRelationshipType srt;
            if(synRelation.getSynonym().getName().getHomotypicalGroup()!= null
                    && synRelation.getSynonym().getName().getHomotypicalGroup().equals(newAcceptedTaxon.getName().getHomotypicalGroup())) {
                srt = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
            } else if(synRelation.getType() != null && synRelation.getType().equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {
            	if (synonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
            		srt = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
            	} else{
            		srt = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            	}
            } else {
                srt = synRelation.getType();

            }

            newAcceptedTaxon.addSynonym(synRelation.getSynonym(),
                    srt,
                    synRelation.getCitation(),
                    synRelation.getCitationMicroReference());

            /*if (synonymsInHomotypicalGroup.contains(synRelation.getSynonym()) && srt.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())){
            	homotypicalGroupAcceptedTaxon.addTypifiedName(synRelation.getSynonym().getName());
            }*/

        }





        // CHILD NODES
        if(oldTaxonNode.getChildNodes() != null && oldTaxonNode.getChildNodes().size() != 0){
            for(TaxonNode childNode : oldTaxonNode.getChildNodes()){
                newAcceptedTaxonNode.addChildNode(childNode, childNode.getReference(), childNode.getMicroReference()); // childNode.getSynonymToBeUsed()
            }
        }

        //Move Taxon RelationShips to new Taxon
        Set<TaxonRelationship> obsoleteTaxonRelationships = new HashSet<TaxonRelationship>();
        for(TaxonRelationship taxonRelationship : oldTaxon.getTaxonRelations()){
            Taxon fromTaxon = (Taxon) HibernateProxyHelper.deproxy(taxonRelationship.getFromTaxon());
            Taxon toTaxon = (Taxon) HibernateProxyHelper.deproxy(taxonRelationship.getToTaxon());
            if (fromTaxon == oldTaxon){
                newAcceptedTaxon.addTaxonRelation(taxonRelationship.getToTaxon(), taxonRelationship.getType(),
                        taxonRelationship.getCitation(), taxonRelationship.getCitationMicroReference());

            }else if(toTaxon == oldTaxon){
               fromTaxon.addTaxonRelation(newAcceptedTaxon, taxonRelationship.getType(),
                        taxonRelationship.getCitation(), taxonRelationship.getCitationMicroReference());
               taxonService.saveOrUpdate(fromTaxon);

            }else{
                logger.warn("Taxon is not part of its own Taxonrelationship");
            }
            // Remove old relationships

            fromTaxon.removeTaxonRelation(taxonRelationship);
            toTaxon.removeTaxonRelation(taxonRelationship);
            taxonRelationship.setToTaxon(null);
            taxonRelationship.setFromTaxon(null);
        }


        //Move descriptions to new taxon
        List<TaxonDescription> descriptions = new ArrayList<TaxonDescription>( oldTaxon.getDescriptions()); //to avoid concurrent modification errors (newAcceptedTaxon.addDescription() modifies also oldtaxon.descritpions())
        for(TaxonDescription description : descriptions){
            String message = "Description copied from former accepted taxon: %s (Old title: %s)";
            message = String.format(message, oldTaxon.getTitleCache(), description.getTitleCache());
            description.setTitleCache(message, true);
            newAcceptedTaxon.addDescription(description);
        }
        taxonService.update(newAcceptedTaxon);
        TaxonDeletionConfigurator conf = new TaxonDeletionConfigurator();
        conf.setDeleteSynonymsIfPossible(false);
        List<String> deleteMessages = taxonService.isDeletable(oldTaxon, conf);
//        conf.setDeleteNameIfPossible(false);
        if (deleteMessages.isEmpty()){
        	String uuidString = taxonService.deleteTaxon(oldTaxon, conf, null);
        	 logger.debug(uuidString);
        }else{
        	TaxonNodeDeletionConfigurator config = new TaxonNodeDeletionConfigurator();
        	config.setDeleteTaxon(false);
        	conf.setTaxonNodeConfig(config);
        	deleteTaxonNode(oldTaxonNode, conf);
        }
       
        //oldTaxonNode.delete();
        return synonmyRelationship.getSynonym();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#deleteTaxonNodes(java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public List<UUID> deleteTaxonNodes(Set<ITaxonTreeNode> nodes, TaxonDeletionConfigurator config) {
        if (config == null){
        	config = new TaxonDeletionConfigurator();
        }
        List<UUID> deletedUUIDs = new ArrayList<UUID>();
        Classification classification = null;
        for (ITaxonTreeNode treeNode:nodes){
        	if (treeNode != null){
	        	if (treeNode instanceof TaxonNode){
	        		TaxonNode taxonNode;
		            taxonNode = HibernateProxyHelper.deproxy(treeNode, TaxonNode.class);

		            	//check whether the node has children or the children are already deleted
		            if(taxonNode.hasChildNodes()){
	            		Set<ITaxonTreeNode> children = new HashSet<ITaxonTreeNode> ();
	            		List<TaxonNode> childNodesList = taxonNode.getChildNodes();
	        			children.addAll(childNodesList);
	        			int compare = config.getTaxonNodeConfig().getChildHandling().compareTo(ChildHandling.DELETE);
	        			boolean childHandling = (compare == 0)? true: false;
	            		if (childHandling){
	            			boolean changeDeleteTaxon = false;
	            			if (!config.getTaxonNodeConfig().isDeleteTaxon()){
	            				config.getTaxonNodeConfig().setDeleteTaxon(true);
	            				changeDeleteTaxon = true;
	            			}
	            			deleteTaxonNodes(children, config);
	            			if (changeDeleteTaxon){
	            				config.getTaxonNodeConfig().setDeleteTaxon(false);
	            			}

	            		} else {
	            			//move the children to the parent
	            			TaxonNode parent = taxonNode.getParent();
	            			for (TaxonNode child: childNodesList){
	            				parent.addChildNode(child, child.getReference(), child.getMicroReference());
	            			}

	            		}
	            	}

		            classification = taxonNode.getClassification();

		            if (classification.getRootNode().equals(taxonNode)){
		            	classification.removeRootNode();
		            	classification = null;
		            }else if (classification.getChildNodes().contains(taxonNode)){
	            		Taxon taxon = taxonNode.getTaxon();
	            		classification.deleteChildNode(taxonNode);
		            	//node is rootNode
		            	if (taxon != null){

		            		if (config.getTaxonNodeConfig().isDeleteTaxon()){
				            	TaxonDeletionConfigurator configNew = new TaxonDeletionConfigurator();
				            	configNew.setDeleteTaxonNodes(false);
				            	taxonService.deleteTaxon(taxon, configNew, classification);
			            	}
		            	}
	            		classification = null;

		            }else {
		            	classification = null;
		            	Taxon taxon = taxonNode.getTaxon();
		            	//node is rootNode
		            	if (taxon != null){
		            		taxonNode.getTaxon().removeTaxonNode(taxonNode);
		            		if (config.getTaxonNodeConfig().isDeleteTaxon()){
				            	TaxonDeletionConfigurator configNew = new TaxonDeletionConfigurator();
				            	configNew.setDeleteTaxonNodes(false);
				            	taxonService.deleteTaxon(taxon, configNew, classification);
			            	}
		            	}

		            }

		            UUID uuid = dao.delete(taxonNode);
		            logger.debug("Deleted node " +uuid.toString());
	        	}else {
	        		classification = (Classification) treeNode;

	        	}

	            deletedUUIDs.add(treeNode.getUuid());

	        }
        }
        if (classification != null){
        	classService.delete(classification);
        }
        return deletedUUIDs;

    }
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#deleteTaxonNode(java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public String deleteTaxonNode(TaxonNode node, TaxonDeletionConfigurator config) {
    	Taxon taxon = (Taxon)HibernateProxyHelper.deproxy(node.getTaxon());
    	if (config == null){
    		config = new TaxonDeletionConfigurator();
    	}
    	if (config.getTaxonNodeConfig().isDeleteTaxon()){
    		return taxonService.deleteTaxon(taxon, config, node.getClassification());
    	} else{
    		taxon.removeTaxonNode(node);
    		dao.delete(node);
    		return node.getUuid().toString();
    	}
    	
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#listAllNodesForClassification(eu.etaxonomy.cdm.model.taxon.Classification, int, int)
     */
    @Override
    public List<TaxonNode> listAllNodesForClassification(Classification classification, Integer start, Integer end) {
        return dao.getTaxonOfAcceptedTaxaByClassification(classification, start, end);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#countAllNodesForClassification(eu.etaxonomy.cdm.model.taxon.Classification)
     */
    @Override
    public int countAllNodesForClassification(Classification classification) {
        return dao.countTaxonOfAcceptedTaxaByClassification(classification);
    }



}
