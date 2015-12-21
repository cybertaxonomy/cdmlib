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
import java.util.Collection;
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

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNaturalComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByNameComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeByRankAndNameComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
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

    private final Comparator<? super TaxonNode> taxonNodeComparator = new TaxonNodeByNameComparator();

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IClassificationService classService;

    @Autowired
    private IDefinedTermDao termDao;

    @Override
    public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
            List<String> propertyPaths, boolean recursive, NodeSortMode sortMode) {
        taxonNode = dao.load(taxonNode.getUuid());
        List<TaxonNode> childNodes;
        if (recursive == true){
        	childNodes  = dao.listChildrenOf(taxonNode, null, null, null, recursive);
        }else{
        	childNodes = new ArrayList<TaxonNode>(taxonNode.getChildNodes());
        }
        if (sortMode != null){
            Comparator<TaxonNode> comparator = null;
            if (sortMode.equals(NodeSortMode.NaturalOrder)){
                comparator = new TaxonNaturalComparator();
            } else if (sortMode.equals(NodeSortMode.AlphabeticalOrder)){
            	comparator = new TaxonNodeByNameComparator();
            } else if (sortMode.equals(NodeSortMode.RankAndAlphabeticalOrder)){
            	comparator = new TaxonNodeByRankAndNameComparator();
            }
        	Collections.sort(childNodes, comparator);
        }
        defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
        return childNodes;
    }

    @Override
    @Autowired
    protected void setDao(ITaxonNodeDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymRelationshipType synonymRelationshipType, Reference citation, String citationMicroReference)  {


        // TODO at the moment this method only moves synonym-, concept relations and descriptions to the new accepted taxon
        // in a future version we also want to move cdm data like annotations, marker, so., but we will need a policy for that
        if (oldTaxonNode == null || newAcceptedTaxonNode == null || oldTaxonNode.getTaxon().getName() == null){
            throw new IllegalArgumentException("A mandatory parameter was null.");
        }

        if(oldTaxonNode.equals(newAcceptedTaxonNode)){
            throw new IllegalArgumentException("Taxon can not be made synonym of its own.");
        }


        Classification classification = oldTaxonNode.getClassification();
        Taxon oldTaxon = (Taxon) HibernateProxyHelper.deproxy(oldTaxonNode.getTaxon());
        Taxon newAcceptedTaxon = (Taxon)this.taxonService.load(newAcceptedTaxonNode.getTaxon().getUuid());
        // Move oldTaxon to newTaxon
        //TaxonNameBase<?,?> synonymName = oldTaxon.getName();
        TaxonNameBase<?,?> synonymName = (TaxonNameBase)HibernateProxyHelper.deproxy(oldTaxon.getName());
        HomotypicalGroup group = synonymName.getHomotypicalGroup();
        group = HibernateProxyHelper.deproxy(group, HomotypicalGroup.class);
        if (synonymRelationshipType == null){
            if (synonymName.isHomotypic(newAcceptedTaxon.getName())){
                synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
            }else{
                synonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            }
        }

        //set homotypic group
        HomotypicalGroup newAcceptedTaxonHomotypicalgroup = newAcceptedTaxon.getHomotypicGroup();
        newAcceptedTaxonHomotypicalgroup = HibernateProxyHelper.deproxy(newAcceptedTaxonHomotypicalgroup, HomotypicalGroup.class);
        TaxonNameBase newAcceptedTaxonName = HibernateProxyHelper.deproxy(newAcceptedTaxon.getName(), TaxonNameBase.class);
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
                    && synRelation.getSynonym().getName().getHomotypicalGroup().equals(newAcceptedTaxonName.getHomotypicalGroup())) {
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
        	List<TaxonNode> childNodes = new ArrayList<TaxonNode>();
        	for (TaxonNode childNode : oldTaxonNode.getChildNodes()){
        		childNodes.add(childNode);
        	}
            for(TaxonNode childNode :childNodes){
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
            //oldTaxon.removeDescription(description, false);
            newAcceptedTaxon.addDescription(description);
        }
        oldTaxon.clearDescriptions();

        taxonService.update(newAcceptedTaxon);

        taxonService.update(oldTaxon);

        TaxonDeletionConfigurator conf = new TaxonDeletionConfigurator();
        conf.setDeleteSynonymsIfPossible(false);
        DeleteResult result = taxonService.isDeletable(oldTaxon, conf);
        conf.setDeleteNameIfPossible(false);

        if (result.isOk()){
        	 result = taxonService.deleteTaxon(oldTaxon.getUuid(), conf, classification.getUuid());
        }else{
        	result.setStatus(Status.OK);
        	TaxonNodeDeletionConfigurator config = new TaxonNodeDeletionConfigurator();
        	config.setDeleteElement(false);
        	conf.setTaxonNodeConfig(config);
        	result.includeResult(deleteTaxonNode(oldTaxonNode, conf));
        }
        result.addUpdatedObject(newAcceptedTaxon);
        result.addUpdatedObject(oldTaxon);

        //oldTaxonNode.delete();
        return result;
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#makeTaxonNodeASynonymOfAnotherTaxonNode(java.util.UUID, java.util.UUID, java.util.UUID, java.util.UUID, java.lang.String)
     */
    @Override
    @Transactional(readOnly = false)
    public UpdateResult makeTaxonNodeASynonymOfAnotherTaxonNode(UUID oldTaxonNodeUuid,
            UUID newAcceptedTaxonNodeUUID,
            SynonymRelationshipType synonymRelationshipType,
            Reference citation,
            String citationMicroReference) {

        TaxonNode oldTaxonNode = dao.load(oldTaxonNodeUuid);
        TaxonNode oldTaxonParentNode = oldTaxonNode.getParent();
        TaxonNode newTaxonNode = dao.load(newAcceptedTaxonNodeUUID);

        UpdateResult result = makeTaxonNodeASynonymOfAnotherTaxonNode(oldTaxonNode,
                newTaxonNode,
                synonymRelationshipType,
                citation,
                citationMicroReference);
        result.addUpdatedCdmId(new CdmEntityIdentifier(oldTaxonParentNode.getId(), TaxonNode.class));
        result.addUpdatedCdmId(new CdmEntityIdentifier(newTaxonNode.getId(), TaxonNode.class));
        result.setCdmEntity(oldTaxonParentNode);
        return result;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#deleteTaxonNodes(java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNodes(List<TaxonNode> list, TaxonDeletionConfigurator config) {

        if (config == null){
        	config = new TaxonDeletionConfigurator();
        }
        DeleteResult result = new DeleteResult();
        List<UUID> deletedUUIDs = new ArrayList<UUID>();
        Classification classification = null;
        List<TaxonNode> taxonNodes = new ArrayList<TaxonNode>(list);
        for (TaxonNode treeNode:taxonNodes){
        	if (treeNode != null){

        		TaxonNode taxonNode;
	            taxonNode = HibernateProxyHelper.deproxy(treeNode, TaxonNode.class);
	            TaxonNode parent = taxonNode.getParent();
	            	//check whether the node has children or the children are already deleted
	            if(taxonNode.hasChildNodes()) {
            		List<TaxonNode> children = new ArrayList<TaxonNode> ();
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
            			DeleteResult resultNodes = deleteTaxonNodes(children, config);
            			if (!resultNodes.isOk()){
                            result.addExceptions(resultNodes.getExceptions());
                            result.setStatus(resultNodes.getStatus());
                        }
            			if (changeDeleteTaxon){
            				config.getTaxonNodeConfig().setDeleteTaxon(false);
            			}

            		} else {
            			//move the children to the parent

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
	            		    taxonService.saveOrUpdate(taxon);
	            		    saveOrUpdate(taxonNode);

			            	TaxonDeletionConfigurator configNew = new TaxonDeletionConfigurator();
			            	DeleteResult resultTaxon = taxonService.deleteTaxon(taxon.getUuid(), configNew, classification.getUuid());
			            	if (!resultTaxon.isOk()){
                                result.addExceptions(resultTaxon.getExceptions());
                                result.setStatus(resultTaxon.getStatus());
                            }

		            	}
	            	}
            		classification = null;

	            } else {
	            	classification = null;
	            	Taxon taxon = taxonNode.getTaxon();
	            	taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
	            	if (taxon != null){
	            		taxon.removeTaxonNode(taxonNode);
	            		if (config.getTaxonNodeConfig().isDeleteTaxon()){
			            	TaxonDeletionConfigurator configNew = new TaxonDeletionConfigurator();
			            	saveOrUpdate(taxonNode);
			            	taxonService.saveOrUpdate(taxon);
			            	DeleteResult resultTaxon = taxonService.deleteTaxon(taxon.getUuid(), configNew, null);

                            if (!resultTaxon.isOk()){
                                result.addExceptions(resultTaxon.getExceptions());
                                result.setStatus(resultTaxon.getStatus());
                            }
		            	}
	            	}

	            }

	            result.addUpdatedObject(parent);
	            if(result.getCdmEntity() == null){
	                result.setCdmEntity(taxonNode);
                }
	            UUID uuid = dao.delete(taxonNode);
	            logger.debug("Deleted node " +uuid.toString());

	        }
        }
        /*if (classification != null){
            result.addUpdatedObject(classification);
        	DeleteResult resultClassification = classService.delete(classification);
        	 if (!resultClassification.isOk()){
                 result.addExceptions(resultClassification.getExceptions());
                 result.setStatus(resultClassification.getStatus());
             }
        }*/
        return result;

    }


    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNodes(Collection<UUID> nodeUuids, TaxonDeletionConfigurator config) {
        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
        for(UUID nodeUuid : nodeUuids) {
            nodes.add(dao.load(nodeUuid));
        }
        return deleteTaxonNodes(nodes, config);
    }



    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNode(UUID nodeUUID, TaxonDeletionConfigurator config) {

    	TaxonNode node = HibernateProxyHelper.deproxy(dao.load(nodeUUID), TaxonNode.class);
    	return deleteTaxonNode(node, config);
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNode(TaxonNode node, TaxonDeletionConfigurator config) {
        DeleteResult result = new DeleteResult();
        if (node == null){
            result.setAbort();
            result.addException(new Exception("The TaxonNode was already deleted."));
            return result;
        }
        Taxon taxon = null;
        try{
            taxon = (Taxon)HibernateProxyHelper.deproxy(node.getTaxon());
        }catch(NullPointerException e){
            result.setAbort();
            result.addException(new Exception("The Taxon was already deleted."));

        }
    	TaxonNode parent = HibernateProxyHelper.deproxy(node.getParent(), TaxonNode.class);
    	if (config == null){
    		config = new TaxonDeletionConfigurator();
    	}



    	if (config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.MOVE_TO_PARENT)){
    	   Object[] children = node.getChildNodes().toArray();
    	   TaxonNode childNode;
    	   for (Object child: children){
    	       childNode = (TaxonNode) child;
    	       parent.addChildNode(childNode, childNode.getReference(), childNode.getMicroReference());
    	   }
    	}else{
    	    deleteTaxonNodes(node.getChildNodes(), config);
    	}

    	if (taxon != null){
        	if (config.getTaxonNodeConfig().isDeleteTaxon() && (config.isDeleteInAllClassifications() || taxon.getTaxonNodes().size() == 1)){
        		result = taxonService.deleteTaxon(taxon.getUuid(), config, node.getClassification().getUuid());
        		result.addUpdatedObject(parent);
        		if (result.isOk()){
        			return result;
        		}
        	} else {
        	    result.addUpdatedObject(taxon);
        	}
    	}
    	result.setCdmEntity(node);
    	boolean success = taxon.removeTaxonNode(node);
    	dao.save(parent);
    	taxonService.saveOrUpdate(taxon);
    	result.addUpdatedObject(parent);

    	if (success){
			result.setStatus(Status.OK);
			parent = HibernateProxyHelper.deproxy(parent, TaxonNode.class);
			int index = parent.getChildNodes().indexOf(node);
			if (index > -1){
			    parent.removeChild(index);
			}
    		if (!dao.delete(node, config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.DELETE)).equals(null)){
    			return result;
    		} else {
    			result.setError();
    			return result;
    		}
    	}else{
    	    if (dao.findByUuid(node.getUuid()) != null){
        		result.setError();
        		result.addException(new Exception("The node can not be removed from the taxon."));
    		}
    		return result;
    	}



    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonNodeService#listAllNodesForClassification(eu.etaxonomy.cdm.model.taxon.Classification, int, int)
     */
    @Override
    public List<TaxonNode> listAllNodesForClassification(Classification classification, Integer start, Integer end) {
        return dao.getTaxonOfAcceptedTaxaByClassification(classification, start, end);
    }

    @Override
    public int countAllNodesForClassification(Classification classification) {
        return dao.countTaxonOfAcceptedTaxaByClassification(classification);
    }

    @Override
    @Transactional
    public UpdateResult moveTaxonNode(UUID taxonNodeUuid, UUID targetNodeUuid, boolean isParent){
        TaxonNode taxonNode = dao.load(taxonNodeUuid);
    	TaxonNode targetNode = dao.load(targetNodeUuid);
    	return moveTaxonNode(taxonNode, targetNode, isParent);
    }

    @Override
    @Transactional
    public UpdateResult moveTaxonNode(TaxonNode taxonNode, TaxonNode newParent, boolean isParent){
        UpdateResult result = new UpdateResult();

        Integer sortIndex;
        if (isParent){

            sortIndex = newParent.getChildNodes().size();
        }else{
            sortIndex = newParent.getSortIndex() +1;
            newParent = newParent.getParent();
        }
        result.addUpdatedObject(newParent);
        result.addUpdatedObject(taxonNode.getParent());
        result.setCdmEntity(taxonNode);
        newParent.addChildNode(taxonNode, sortIndex, taxonNode.getReference(),  taxonNode.getMicroReference());
        dao.saveOrUpdate(newParent);

        return result;
    }



    @Override
    @Transactional
    public UpdateResult moveTaxonNodes(Set<UUID> taxonNodeUuids, UUID newParentNodeUuid, boolean isParent){
        UpdateResult result = new UpdateResult();
        TaxonNode targetNode = dao.load(newParentNodeUuid);
        for (UUID taxonNodeUuid: taxonNodeUuids){
            TaxonNode taxonNode = dao.load(taxonNodeUuid);
            result.includeResult(moveTaxonNode(taxonNode,targetNode, isParent));
        }
        return result;
    }

    @Override
    public Pager<TaxonNodeAgentRelation> pageTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer pageSize, Integer pageIndex, List<String> propertyPaths) {


        List<TaxonNodeAgentRelation> records = null;

        long count = dao.countTaxonNodeAgentRelations(taxonUuid, classificationUuid, agentUuid, rankUuid, relTypeUuid);
        if(PagerUtils.hasResultsInRange(count, pageIndex, pageSize)) {
            records = dao.listTaxonNodeAgentRelations(taxonUuid, classificationUuid,
                    agentUuid, rankUuid, relTypeUuid, PagerUtils.startFor(pageSize, pageIndex), PagerUtils.limitFor(pageSize), propertyPaths);
        }

        Pager<TaxonNodeAgentRelation> pager = new DefaultPagerImpl<TaxonNodeAgentRelation>(pageIndex, count, pageSize, records);
        return pager;
    }

}
