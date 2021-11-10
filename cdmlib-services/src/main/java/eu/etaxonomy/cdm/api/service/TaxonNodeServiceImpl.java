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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.ForSubtreeConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.PublishForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SubtreeCloneConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.CdmEntityIdentifier;
import eu.etaxonomy.cdm.api.service.dto.CreateTaxonDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonDistributionDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingEnum;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IOriginalSourceDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;
import eu.etaxonomy.cdm.persistence.dto.HomotypicGroupDto;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @since Apr 9, 2010
 */
@Service
@Transactional(readOnly = true)
public class TaxonNodeServiceImpl
           extends AnnotatableServiceBase<TaxonNode, ITaxonNodeDao>
           implements ITaxonNodeService{

    private static final Logger logger = Logger.getLogger(TaxonNodeServiceImpl.class);

    @Autowired
    private IBeanInitializer defaultBeanInitializer;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IReferenceService referenceService;

    @Autowired
    private IDescriptiveDataSetService dataSetService;

    @Autowired
    private IAgentService agentService;

    @Autowired
    private INameService nameService;

    @Autowired
    private IOriginalSourceDao sourceDao;

    @Autowired
    private ITaxonNodeFilterDao nodeFilterDao;

    @Autowired
    private IReferenceDao referenceDao;

    @Autowired
    private IClassificationDao classificationDao;

    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;

    @Autowired
    IProgressMonitorService progressMonitorService;

    @Autowired
    private ICdmPermissionEvaluator permissionEvaluator;

    @Override
    public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode,
            List<String> propertyPaths, boolean recursive,  boolean includeUnpublished,
            TaxonNodeSortMode sortMode) {

        taxonNode = load(taxonNode.getUuid());
        List<TaxonNode> childNodes;
        if (recursive == true){
            Comparator<TaxonNode> comparator = sortMode == null? null : sortMode.comparator();
            childNodes = dao.listChildrenOf(taxonNode, null, null, recursive, includeUnpublished, propertyPaths, comparator);
        }else if (includeUnpublished){
            childNodes = new ArrayList<>(taxonNode.getChildNodes());
        }else{
            childNodes = new ArrayList<>();
            for (TaxonNode node:taxonNode.getChildNodes()){
                if (node.getTaxon().isPublish()){
                    childNodes.add(node);
                }
            }
        }

        HHH_9751_Util.removeAllNull(childNodes);

        if (recursive == false && sortMode != null){
            Comparator<TaxonNode> comparator = sortMode.comparator();
        	Collections.sort(childNodes, comparator);
        }
        defaultBeanInitializer.initializeAll(childNodes, propertyPaths);
        return childNodes;
    }

    @Override
    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex,
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths){
        return dao.listChildrenOf(node, pageSize, pageIndex, recursive, includeUnpublished, propertyPaths, null);
    }

    @Override
    public TaxonNodeDto getParentUuidAndTitleCache(ITaxonTreeNode child) {
        UUID uuid = child.getUuid();
        int id = child.getId();
        TaxonNodeDto uuidAndTitleCache = new TaxonNodeDto(uuid, id, null);
        return getParentUuidAndTitleCache(uuidAndTitleCache);
    }

    @Override
    public TaxonNodeDto getParentUuidAndTitleCache(TaxonNodeDto child) {
        return dao.getParentUuidAndTitleCache(child);
    }

    @Override
    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(TaxonNodeDto parent) {
        return dao.listChildNodesAsTaxonNodeDto(parent);
    }

    @Override
    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid) {
        return dao.getUuidAndTitleCache(limit, pattern, classificationUuid, true);
    }

    @Override
    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(ITaxonTreeNode parent) {
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("parent");
        parent = dao.load(parent.getId(), propertyPaths);
        TaxonNodeDto uuidAndTitleCache = new TaxonNodeDto(parent);
        return listChildNodesAsTaxonNodeDto(uuidAndTitleCache);
    }

    @Override
    public List<TaxonNodeDto> taxonNodeDtoParentRank(Classification classification, Rank rank, TaxonName name) {
    	return dao.getParentTaxonNodeDtoForRank(classification, rank, name);
    }

    @Override
    public List<TaxonNodeDto> taxonNodeDtoParentRank(Classification classification, Rank rank, TaxonBase<?> taxonBase) {
        return dao.getParentTaxonNodeDtoForRank(classification, rank, taxonBase);
    }

    @Override
    public Pager<TaxonNodeDto> pageChildNodesDTOs(UUID taxonNodeUuid, boolean recursive,  boolean includeUnpublished,
            boolean doSynonyms, TaxonNodeSortMode sortMode,
            Integer pageSize, Integer pageIndex) {

        TaxonNode parentNode = dao.load(taxonNodeUuid);

        List<CdmBase> allRecords = new ArrayList<>();

        //acceptedTaxa
        List<TaxonNode> childNodes = loadChildNodesOfTaxonNode(parentNode, null, recursive, includeUnpublished, sortMode);
        allRecords.addAll(childNodes);

        //add synonyms if pager is not yet full synonyms
        if (doSynonyms){
            List<Synonym> synList = new ArrayList<>(parentNode.getTaxon().getSynonyms());
            Collections.sort(synList, new HomotypicGroupTaxonComparator(null));
            //TODO: test sorting

            allRecords.addAll(synList);
        }

        List<TaxonNodeDto> dtos = new ArrayList<>(pageSize==null?25:pageSize);
        long totalCount = Long.valueOf(allRecords.size());

        TaxonName parentName = null;

        for(CdmBase item : PagerUtils.pageList(allRecords, pageIndex, pageSize)) {
            if (item.isInstanceOf(TaxonNode.class)){
                dtos.add(new TaxonNodeDto(CdmBase.deproxy(item, TaxonNode.class)));
            }else if (item.isInstanceOf(Synonym.class)){
                Synonym synonym = CdmBase.deproxy(item, Synonym.class);
                parentName = parentName == null? parentNode.getTaxon().getName(): parentName;
                boolean isHomotypic = synonym.getName().isHomotypic(parentName);
                dtos.add(new TaxonNodeDto(synonym, isHomotypic));
            }
        }
        return new DefaultPagerImpl<>(pageIndex, totalCount, pageSize , dtos);
    }

    @Override
    public TaxonNodeDto parentDto(UUID taxonNodeUuid) {
        if (taxonNodeUuid == null){
            return null;
        }
        TaxonNode taxonNode = dao.load(taxonNodeUuid);
        if(taxonNode.getParent() != null) {
            return new TaxonNodeDto(taxonNode.getParent());
        }
        return null;
    }

    @Override
    public TaxonNodeDto dto(UUID taxonNodeUuid) {
        if (taxonNodeUuid == null){
            return null;
        }
        TaxonNode taxonNode = dao.load(taxonNodeUuid);
        if (taxonNode != null){
            return new TaxonNodeDto(taxonNode);
        }
        return null;
    }

    @Override
    public TaxonNodeDto dto(UUID taxonUuid, UUID classificationUuid) {
        if (taxonUuid == null){
            return null;
        }
        List<TaxonNodeDto> taxonNodes = dao.getTaxonNodeForTaxonInClassificationDto(taxonUuid, classificationUuid);
        if (!taxonNodes.isEmpty()){
            return taxonNodes.get(0);
        }
        return null;
    }

    @Override
    @Autowired
    protected void setDao(ITaxonNodeDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode,
            SynonymType synonymType, Reference citation, String microReference, SecReferenceHandlingEnum secHandling, boolean setNameInSource)  {

        // TODO at the moment this method only moves synonym-, concept relations and descriptions to the new accepted taxon
        // in a future version we also want to move cdm data like annotations, marker, so., but we will need a policy for that
        if (oldTaxonNode == null || newAcceptedTaxonNode == null || oldTaxonNode.getTaxon().getName() == null){
            throw new IllegalArgumentException("A mandatory parameter was null.");
        }

        if(oldTaxonNode.equals(newAcceptedTaxonNode)){
            throw new IllegalArgumentException("Taxon can not be made synonym of its own.");
        }

        Classification classification = oldTaxonNode.getClassification();
        Taxon oldTaxon = HibernateProxyHelper.deproxy(oldTaxonNode.getTaxon());
        Taxon newAcceptedTaxon = (Taxon)this.taxonService.find(newAcceptedTaxonNode.getTaxon().getUuid());
        newAcceptedTaxon = HibernateProxyHelper.deproxy(newAcceptedTaxon);
        // Move oldTaxon to newTaxon
        //TaxonName synonymName = oldTaxon.getName();
        TaxonName newSynonymName = CdmBase.deproxy(oldTaxon.getName());
        HomotypicalGroup group = CdmBase.deproxy(newSynonymName.getHomotypicalGroup());
        if (synonymType == null){
            if (newSynonymName.isHomotypic(newAcceptedTaxon.getName())){
                synonymType = SynonymType.HOMOTYPIC_SYNONYM_OF();
            }else{
                synonymType = SynonymType.HETEROTYPIC_SYNONYM_OF();
            }
        }

        //set homotypic group
        TaxonName newAcceptedTaxonName = HibernateProxyHelper.deproxy(newAcceptedTaxon.getName(), TaxonName.class);
        newAcceptedTaxon.setName(newAcceptedTaxonName);
        Reference secNewAccepted = newAcceptedTaxon.getSec();
        Reference secOldAccepted = oldTaxon.getSec();
        boolean uuidsEqual = (secNewAccepted != null && secOldAccepted != null && secNewAccepted.equals(secOldAccepted)) || (secNewAccepted == null && secOldAccepted == null);
        Reference newSec = citation;
        //keep when same only warns in ui, the sec still
        if (secHandling != null &&  secHandling.equals(SecReferenceHandlingEnum.KeepOrWarn) ){
            newSec = oldTaxon.getSec();
        }
        if (secHandling != null && secHandling.equals(SecReferenceHandlingEnum.AlwaysDelete)){
            newSec = null;
        }

        Synonym newSyn = newAcceptedTaxon.addSynonymName(newSynonymName, newSec, microReference, synonymType);
        if (newSec == null){
            newSyn.setSec(newSec);
        }
        newSyn.setPublish(oldTaxon.isPublish());

        // Move Synonyms to new Taxon
        // From ticket 3163 we can move taxon with accepted name having homotypic synonyms
        List<Synonym> synonymsInHomotypicalGroup = null;

        //the synonyms of the homotypical group of the old taxon
        if (synonymType.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
        	synonymsInHomotypicalGroup = oldTaxon.getSynonymsInGroup(group);
        }

        Set<Synonym> syns = new HashSet<>(oldTaxon.getSynonyms());
        for(Synonym synonym : syns){
            SynonymType srt;
            if(synonym.getHomotypicGroup()!= null
                    && synonym.getHomotypicGroup().equals(newAcceptedTaxonName.getHomotypicalGroup())) {
                srt = SynonymType.HOMOTYPIC_SYNONYM_OF();
            } else if(synonym.getType() != null && synonym.getType().equals(SynonymType.HOMOTYPIC_SYNONYM_OF())) {
            	if (synonymType.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
            		srt = SynonymType.HOMOTYPIC_SYNONYM_OF();
            	} else{
            		srt = SynonymType.HETEROTYPIC_SYNONYM_OF();
            	}
            } else {
                if (synonymsInHomotypicalGroup != null && synonymsInHomotypicalGroup.contains(synonym)){
                    srt = SynonymType.HOMOTYPIC_SYNONYM_OF();
                }else{
                    srt = synonym.getType();
                }

            }
            if (secHandling != null &&  !secHandling.equals(SecReferenceHandlingEnum.KeepOrWarn)){
                synonym.setSec(newSec);
            }
            newAcceptedTaxon.addSynonym(synonym, srt);

        }


        // CHILD NODES
        if(oldTaxonNode.getChildNodes() != null && oldTaxonNode.getChildNodes().size() != 0){
        	List<TaxonNode> childNodes = new ArrayList<>();
        	for (TaxonNode childNode : oldTaxonNode.getChildNodes()){
        		childNodes.add(childNode);
        	}
            for(TaxonNode childNode :childNodes){
                newAcceptedTaxonNode.addChildNode(childNode, childNode.getReference(), childNode.getMicroReference()); // childNode.getSynonymToBeUsed()
            }
        }

        //Move Taxon RelationShips to new Taxon
        for(TaxonRelationship taxonRelationship : oldTaxon.getTaxonRelations()){
            Taxon fromTaxon = HibernateProxyHelper.deproxy(taxonRelationship.getFromTaxon());
            Taxon toTaxon = HibernateProxyHelper.deproxy(taxonRelationship.getToTaxon());
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
            if (setNameInSource) {
                for (DescriptionElementBase element: description.getElements()){
                    for (DescriptionElementSource source: element.getSources()){
                        if (source.getNameUsedInSource() == null){
                            source.setNameUsedInSource(newSynonymName);
                        }
                    }
                }
            }
            //oldTaxon.removeDescription(description, false);
            newAcceptedTaxon.addDescription(description);
        }
        oldTaxon.clearDescriptions();

        taxonService.saveOrUpdate(newAcceptedTaxon);

        taxonService.saveOrUpdate(oldTaxon);
        taxonService.getSession().flush();

        TaxonDeletionConfigurator conf = new TaxonDeletionConfigurator();
        conf.setDeleteSynonymsIfPossible(false);
        conf.setDeleteNameIfPossible(false);
        DeleteResult result = taxonService.isDeletable(oldTaxon.getUuid(), conf);


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


        //oldTaxonNode.delete();
        return result;
    }
    @Override
    @Transactional(readOnly = false)
    public DeleteResult makeTaxonNodeSynonymsOfAnotherTaxonNode( Set<UUID> oldTaxonNodeUuids,
            UUID newAcceptedTaxonNodeUUIDs,
            SynonymType synonymType,
            UUID citation,
            String microReference,
            SecReferenceHandlingEnum secHandling,
            boolean setNameInSource) {
    	DeleteResult result = new DeleteResult();
    	for (UUID nodeUuid: oldTaxonNodeUuids) {
    		result.includeResult(makeTaxonNodeASynonymOfAnotherTaxonNode(nodeUuid, newAcceptedTaxonNodeUUIDs, synonymType, citation, microReference, secHandling, setNameInSource));
    	}
    	return result;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(UUID oldTaxonNodeUuid,
            UUID newAcceptedTaxonNodeUUID,
            SynonymType synonymType,
            UUID citationUuid,
            String microReference,
            SecReferenceHandlingEnum secHandling,
            boolean setNameInSource) {

        TaxonNode oldTaxonNode = dao.load(oldTaxonNodeUuid);
        TaxonNode oldTaxonParentNode = oldTaxonNode.getParent();
        TaxonNode newTaxonNode = dao.load(newAcceptedTaxonNodeUUID);
        Reference citation = referenceDao.load(citationUuid);

        switch (secHandling){
        case AlwaysDelete:
            citation = null;
            break;
        case UseNewParentSec:
            citation = newTaxonNode.getTaxon() != null? newTaxonNode.getTaxon().getSec(): null;
            break;
        case KeepOrWarn:

            Reference synSec = oldTaxonNode.getTaxon().getSec();
            if (synSec != null ){
                citation = CdmBase.deproxy(synSec);
            }
            break;
        case KeepOrSelect:

            break;
        default:
            break;
    }


        DeleteResult result = makeTaxonNodeASynonymOfAnotherTaxonNode(oldTaxonNode,
                newTaxonNode,
                synonymType,
                citation,
                microReference,
                secHandling, setNameInSource);

        result.addUpdatedCdmId(CdmEntityIdentifier.NewInstance(oldTaxonParentNode));
        result.addUpdatedCdmId(CdmEntityIdentifier.NewInstance(newTaxonNode));
        result.setCdmEntity(oldTaxonParentNode);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNodes(List<TaxonNode> list, TaxonDeletionConfigurator config) {

        if (config == null){
        	config = new TaxonDeletionConfigurator();
        }
        DeleteResult result = new DeleteResult();
        Classification classification = null;
        List<TaxonNode> taxonNodes = new ArrayList<>(list);

        for (TaxonNode treeNode:taxonNodes){
        	if (treeNode != null){

        		TaxonNode taxonNode;
	            taxonNode = CdmBase.deproxy(treeNode);
	            TaxonNode parent = taxonNode.getParent();
	            	//check whether the node has children or the children are already deleted
	            if(taxonNode.hasChildNodes()) {
            		List<TaxonNode> children = new ArrayList<> ();
            		List<TaxonNode> childNodesList = taxonNode.getChildNodes();
        			children.addAll(childNodesList);
        			//To avoid NPE when child is also in list of taxonNodes, remove it from the list
        			Iterator<TaxonNode> it = taxonNodes.iterator();
        			for (TaxonNode child: children) {
        				while (it.hasNext()) {
        					if (it.next().equals(child)) {
        						it.remove();
        					}
        				}
        			}
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
			            	configNew.setClassificationUuid(classification.getUuid());
			            	DeleteResult resultTaxon = taxonService.deleteTaxon(taxon.getUuid(), configNew, classification.getUuid());
			            	if (!resultTaxon.isOk()){
                                result.addExceptions(resultTaxon.getExceptions());
                                result.setStatus(resultTaxon.getStatus());
                            }

		            	}
	            	}
            		classification = null;

	            } else {
	            	//classification = null;
	            	Taxon taxon = taxonNode.getTaxon();
	            	taxon = CdmBase.deproxy(taxon);
	            	if (taxon != null){
	            		taxon.removeTaxonNode(taxonNode);
	            		if (config.getTaxonNodeConfig().isDeleteTaxon()){
			            	TaxonDeletionConfigurator configNew = new TaxonDeletionConfigurator();
			            	saveOrUpdate(taxonNode);
			            	taxonService.saveOrUpdate(taxon);
			            	DeleteResult resultTaxon = taxonService.deleteTaxon(taxon.getUuid(), configNew, classification.getUuid());

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
        List<TaxonNode> nodes = new ArrayList<>();
        for(UUID nodeUuid : nodeUuids) {
            nodes.add(dao.load(nodeUuid));
        }
        return deleteTaxonNodes(nodes, config);
    }


    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteTaxonNode(UUID nodeUUID, TaxonDeletionConfigurator config) {

    	TaxonNode node = CdmBase.deproxy(dao.load(nodeUUID));
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
            taxon = HibernateProxyHelper.deproxy(node.getTaxon());
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
    	    result.includeResult(deleteTaxonNodes(node.getChildNodes(), config));
    	}

    	//remove node from DescriptiveDataSet
        commonService.getReferencingObjects(node).stream()
        .filter(obj->obj instanceof DescriptiveDataSet)
        .forEach(dataSet->{
            ((DescriptiveDataSet)dataSet).removeTaxonSubtree(node);
            dataSetService.saveOrUpdate((DescriptiveDataSet) dataSet);
        });

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
    	boolean success = true;
    	if (taxon != null){
    	    success = taxon.removeTaxonNode(node);
    	    taxonService.saveOrUpdate(taxon);
    	}
    	dao.saveOrUpdate(parent);

    	result.addUpdatedObject(parent);

    	if (success){
			result.setStatus(Status.OK);
			if (parent != null){
    			parent = HibernateProxyHelper.deproxy(parent, TaxonNode.class);
    			int index = parent.getChildNodes().indexOf(node);
    			if (index > -1){
    			    parent.removeChild(index);
    			}
			}
    		if (!dao.delete(node, config.getTaxonNodeConfig().getChildHandling().equals(ChildHandling.DELETE)).equals(null)){
    		    result.getUpdatedObjects().remove(node);
    			result.addDeletedObject(node);
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
    public UpdateResult moveTaxonNode(UUID taxonNodeUuid, UUID targetNodeUuid, int movingType, SecReferenceHandlingEnum secHandling, UUID secUuid){
        TaxonNode taxonNode = HibernateProxyHelper.deproxy(dao.load(taxonNodeUuid));
    	TaxonNode targetNode = HibernateProxyHelper.deproxy(dao.load(targetNodeUuid));
    	Reference sec = null;
    	if (secUuid != null){
    	    sec = HibernateProxyHelper.deproxy(referenceDao.load(secUuid));
    	}
    	UpdateResult result = moveTaxonNode(taxonNode, targetNode, movingType, secHandling, sec);
    	return result;
    }

    @Override
    @Transactional
    public UpdateResult moveTaxonNode(TaxonNode taxonNode, TaxonNode newParent, int movingType, SecReferenceHandlingEnum secHandling, Reference sec){
        UpdateResult result = new UpdateResult();

        TaxonNode parentParent = HibernateProxyHelper.deproxy(newParent.getParent());
        Integer sortIndex = -1;
        if (movingType == 0){
            sortIndex = 0;
        }else if (movingType == 1){
            sortIndex = newParent.getSortIndex();
            newParent = parentParent;
        } else if (movingType == 2){
            sortIndex = newParent.getSortIndex() +1;
            newParent = parentParent;
        } else{
            result.setAbort();
            result.addException(new Exception("The moving type "+ movingType +" is not supported."));
        }

        if (secHandling.equals(SecReferenceHandlingEnum.AlwaysSelect) || (secHandling.equals(SecReferenceHandlingEnum.KeepOrSelect) && sec != null)){
            if (taxonNode.getTaxon() != null){
                taxonNode.getTaxon().setSec(sec);
            }
        }else if (secHandling.equals(SecReferenceHandlingEnum.AlwaysDelete)){
            if (taxonNode.getTaxon() != null){
                taxonNode.getTaxon().setSec(null);
            }
        }else if (secHandling.equals(SecReferenceHandlingEnum.UseNewParentSec)){
            if (taxonNode.getTaxon() != null && newParent.getTaxon()!= null){
                taxonNode.getTaxon().setSec(newParent.getTaxon().getSec());
            }
        }

        taxonNode = newParent.addChildNode(taxonNode, sortIndex, taxonNode.getReference(),  taxonNode.getMicroReference());
        result.addUpdatedObject(taxonNode);

        return result;
    }

    @Override
    @Transactional
    public UpdateResult moveTaxonNodes(Set<UUID> taxonNodeUuids, UUID newParentNodeUuid, int movingType, SecReferenceHandlingEnum secHandling, UUID secUuid, IProgressMonitor monitor){

        if (monitor == null){
            monitor = DefaultProgressMonitor.NewInstance();
        }
        UpdateResult result = new UpdateResult();
        List<String> taxonNodePropertyPath = new ArrayList<>();
        taxonNodePropertyPath.add("taxon.secSource.*");
        taxonNodePropertyPath.add("parent.taxon.secSource.*");
        TaxonNode targetNode = dao.load(newParentNodeUuid, taxonNodePropertyPath);
        List<TaxonNode> nodes = dao.list(taxonNodeUuids, null, null, null, null);
        Reference sec = referenceDao.load(secUuid);

        monitor.beginTask("Move Taxonnodes", nodes.size()*2);
        monitor.subTask("move taxon nodes");
        for (TaxonNode node: nodes){
            if (!monitor.isCanceled()){
                if (!nodes.contains(node.getParent())){
                    result.includeResult(moveTaxonNode(node, targetNode, movingType, secHandling, sec));
                }
                monitor.worked(1);
            }else{
                monitor.done();
                result.setAbort();
                break;
            }
        }
        if (!monitor.isCanceled()){
            monitor.subTask("saving and reindex");
            dao.saveOrUpdateAll(nodes);
        }else{
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        monitor.done();
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

        Pager<TaxonNodeAgentRelation> pager = new DefaultPagerImpl<>(pageIndex, count, pageSize, records);
        return pager;
    }

    @Override
    @Transactional
    public UpdateResult createNewTaxonNode(UUID parentNodeUuid, CreateTaxonDTO taxonDto,
            NamedSource source, String microref,
            TaxonNodeStatus status, Map<Language,LanguageString> statusNote){

        UpdateResult result = new UpdateResult();
        TaxonNode child = null;
        TaxonNode parent = null;
        try{
            TaxonName name = null;
            Taxon taxon = null;
            if (taxonDto.getTaxonUuid() != null){
                taxon = (Taxon) taxonService.load(taxonDto.getTaxonUuid());
                if (taxon == null){
                    throw new RuntimeException("Taxon for not found for id " + taxonDto.getTaxonUuid());
                }
            }else{
                if (taxonDto.getNameUuid() != null){
                    name = nameService.load(taxonDto.getNameUuid());
                    if (name == null){
                        throw new RuntimeException("Taxon name not found for id " + taxonDto.getTaxonUuid());
                    }
                } else {
                    UpdateResult tmpResult = nameService.parseName(taxonDto.getTaxonNameString(),
                            taxonDto.getCode(), taxonDto.getPreferredRank(),  true);
                    result.addUpdatedObjects(tmpResult.getUpdatedObjects());
                    name = (TaxonName)tmpResult.getCdmEntity();
                }
                Reference sec = null;
                if (taxonDto.getSecUuid() != null ){
                    sec = referenceService.load(taxonDto.getSecUuid());
                }
                if (name != null && !name.isPersited()){
                    for (HybridRelationship rel : name.getHybridChildRelations()){
                        if (!rel.getHybridName().isPersited()) {
                            nameService.save(rel.getHybridName());
                        }
                        if (!rel.getParentName().isPersited()) {
                            nameService.save(rel.getParentName());
                        }
                    }
                }
                taxon = Taxon.NewInstance(name, sec);
                taxon.setPublish(taxonDto.isPublish());
            }

            parent = dao.load(parentNodeUuid);
            if (source != null){
                if (source.isPersited()){
                    source = (NamedSource) sourceDao.load(source.getUuid());
                }
                if (source.getCitation() != null){
                    source.setCitation(referenceService.load(source.getCitation().getUuid()));
                }
                if (source.getNameUsedInSource() !=null){
                    source.setNameUsedInSource(nameService.load(source.getNameUsedInSource().getUuid()));
                }
            }

            child = parent.addChildTaxon(taxon, source);
            child.setStatus(status);

            if (statusNote != null){
                child.getStatusNote().putAll(statusNote);
            }

        }catch(Exception e){
            result.addException(e);
            result.setError();
            return result;
        }
        child = dao.save(child);

        result.addUpdatedObject(parent);
        if (child != null){
            result.setCdmEntity(child);
        }
        return result;
    }

    @Override
    @Transactional
    public UpdateResult addTaxonNodeAgentRelation(UUID taxonNodeUUID, UUID agentUUID, DefinedTerm relationshipType){
        UpdateResult result = new UpdateResult();
        TaxonNode node = dao.load(taxonNodeUUID);
        TeamOrPersonBase<?> agent = (TeamOrPersonBase<?>) agentService.load(agentUUID);
        node.addAgentRelation(relationshipType, agent);
        try{
            dao.merge(node, true);
        }catch (Exception e){
            result.setError();
            result.addException(e);
        }
        result.setCdmEntity(node);
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult setSecundumForSubtree(SecundumForSubtreeConfigurator config) {
        UpdateResult result = new UpdateResult();
        IProgressMonitor monitor = config.getMonitor();

        if (monitor == null){
            monitor = DefaultProgressMonitor.NewInstance();
        }
        monitor.beginTask("Update secundum reference for subtree", 100);
        monitor.subTask("Check start conditions");

        if (config.getSubtreeUuid() == null){
            result.setError();
            result.addException(new NullPointerException("No subtree given"));
            monitor.done();
            return result;
        }
        monitor.worked(1);
        TaxonNode subTree = load(config.getSubtreeUuid());
        if (subTree == null){
            result.setError();
            result.addException(new NullPointerException("Subtree does not exist"));
            monitor.done();
            return result;
        }
        monitor.worked(1);

        Reference newSec = null;
        if (config.getNewSecundum() != null){
            newSec = referenceService.load(config.getNewSecundum().getUuid());
            if (newSec == null){
                result.setError();
                result.addException(new NullPointerException("New secundum reference does not exist"));
                monitor.done();
                return result;
            }
        }
        monitor.worked(1);

        monitor.subTask("Count records");
        try {
            boolean includeRelatedTaxa = config.isIncludeProParteSynonyms() || config.isIncludeMisapplications();

            TreeIndex subTreeIndex = TreeIndex.NewInstance(subTree.treeIndex());
            int count = config.isIncludeAcceptedTaxa() ? dao.countSecundumForSubtreeAcceptedTaxa(subTreeIndex, newSec, config.isOverwriteExisting(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail()):0;
            monitor.worked(2);
            count += config.isIncludeSynonyms() ? dao.countSecundumForSubtreeSynonyms(subTreeIndex, newSec, config.isOverwriteExisting(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail()) :0;
            monitor.worked(3);
            count += includeRelatedTaxa ? dao.countSecundumForSubtreeRelations(subTreeIndex, newSec, config.isOverwriteExisting(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail()):0;
            monitor.worked(2);
            if (monitor.isCanceled()){
                return result;
            }

            SubProgressMonitor subMonitor = SubProgressMonitor.NewStarted(monitor, 90, "Updating secundum for subtree", count * 2);  //*2 1 tick for update and 1 tick for commit
            //Reference ref = config.getNewSecundum();
            if (config.isIncludeAcceptedTaxa()){
                monitor.subTask("Update Accepted Taxa");
                Set<CdmBase> updatedTaxa = dao.setSecundumForSubtreeAcceptedTaxa(subTreeIndex, newSec,
                        config.isOverwriteExisting(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail(), subMonitor);
                result.addUpdatedObjects(updatedTaxa);
                if (monitor.isCanceled()){
                    return result;
                }
            }
            if (config.isIncludeSynonyms()){
               monitor.subTask("Update Synonyms");
               Set<CdmBase> updatedSynonyms = dao.setSecundumForSubtreeSynonyms(subTreeIndex, newSec,
                       config.isOverwriteExisting(), config.isIncludeSharedTaxa() , config.isEmptySecundumDetail(), subMonitor);
               result.addUpdatedObjects(updatedSynonyms);
               if (monitor.isCanceled()){
                   return result;
               }
            }
            if (includeRelatedTaxa){
                monitor.subTask("Update Related Taxa");
                Set<UUID> relationTypes = getRelationTypesForSubtree(config);
                Set<CdmBase> updatedRels = dao.setSecundumForSubtreeRelations(subTreeIndex, newSec,
                        relationTypes, config.isOverwriteExisting(), config.isIncludeSharedTaxa() , config.isEmptySecundumDetail(), subMonitor);
                result.addUpdatedObjects(updatedRels);
                if (monitor.isCanceled()){
                    return result;
                }
            }
        } catch (Exception e) {
            result.setError();
            result.addException(e);
        }
        monitor.done();
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public UpdateResult setPublishForSubtree(PublishForSubtreeConfigurator config){
        UpdateResult result = new UpdateResult();
        IProgressMonitor monitor = config.getMonitor();
        if (monitor == null){
            monitor = DefaultProgressMonitor.NewInstance();
        }
        monitor.beginTask("Update publish flag for subtree", 100);
        monitor.subTask("Check start conditions");

        if (config.getSubtreeUuid() == null){
            result.setError();
            result.addException(new NullPointerException("No subtree given"));
            monitor.done();
            return result;
        }
        monitor.worked(1);

        TaxonNode subTree = find(config.getSubtreeUuid());
        if (subTree == null){
            result.setError();
            result.addException(new NullPointerException("Subtree does not exist"));
            monitor.done();
            return result;
        }
        monitor.worked(1);

        monitor.subTask("Count records");
        boolean includeAcceptedTaxa = config.isIncludeAcceptedTaxa();
        boolean publish = config.isPublish();
        boolean includeSynonyms = config.isIncludeSynonyms();
        boolean includeSharedTaxa = config.isIncludeSharedTaxa();
        boolean includeHybrids = config.isIncludeHybrids();
        boolean includeRelatedTaxa = config.isIncludeProParteSynonyms() || config.isIncludeMisapplications();
        try {
            TreeIndex subTreeIndex = TreeIndex.NewInstance(subTree.treeIndex());
            int count = includeAcceptedTaxa ? dao.countPublishForSubtreeAcceptedTaxa(subTreeIndex, publish, includeSharedTaxa, includeHybrids):0;
            monitor.worked(3);
            count += includeSynonyms ? dao.countPublishForSubtreeSynonyms(subTreeIndex, publish, includeSharedTaxa, includeHybrids):0;
            monitor.worked(3);
            count += includeRelatedTaxa ? dao.countPublishForSubtreeRelatedTaxa(subTreeIndex, publish, includeSharedTaxa, includeHybrids):0;
            monitor.worked(2);
            if (monitor.isCanceled()){
                return result;
            }

            SubProgressMonitor subMonitor = SubProgressMonitor.NewStarted(monitor, 90, "Updating secundum for subtree", count);
            if (includeAcceptedTaxa){
                monitor.subTask("Update Accepted Taxa");
                @SuppressWarnings("rawtypes")
                Set<TaxonBase> updatedTaxa = dao.setPublishForSubtreeAcceptedTaxa(subTreeIndex,
                        publish, includeSharedTaxa, includeHybrids, subMonitor);
                result.addUpdatedObjects(updatedTaxa);
                if (monitor.isCanceled()){
                    return result;
                }
            }
            if (includeSynonyms){
                monitor.subTask("Update Synonyms");
                @SuppressWarnings("rawtypes")
                Set<TaxonBase> updatedSynonyms = dao.setPublishForSubtreeSynonyms(subTreeIndex,
                        publish, includeSharedTaxa, includeHybrids, subMonitor);
                result.addUpdatedObjects(updatedSynonyms);
                if (monitor.isCanceled()){
                    return result;
                }
            }
            if (includeRelatedTaxa){
                monitor.subTask("Update Related Taxa");
                Set<UUID> relationTypes = getRelationTypesForSubtree(config);
                if (config.isIncludeMisapplications()){
                    relationTypes.addAll(TaxonRelationshipType.misappliedNameUuids());
                }
                if (config.isIncludeProParteSynonyms()){
                    relationTypes.addAll(TaxonRelationshipType.proParteOrPartialSynonymUuids());
                }
                @SuppressWarnings("rawtypes")
                Set<TaxonBase> updatedTaxa = dao.setPublishForSubtreeRelatedTaxa(subTreeIndex, publish,
                        relationTypes, includeSharedTaxa, includeHybrids, subMonitor);
                result.addUpdatedObjects(updatedTaxa);
                if (monitor.isCanceled()){
                    return result;
                }
            }
        } catch (Exception e) {
            result.setError();
            result.addException(e);
        }

        monitor.done();
        return result;
    }

    private Set<UUID> getRelationTypesForSubtree(ForSubtreeConfiguratorBase config) {
        Set<UUID> relationTypes = new HashSet<>();
        if (config.isIncludeMisapplications()){
            relationTypes.addAll(TaxonRelationshipType.misappliedNameUuids());
        }
        if (config.isIncludeProParteSynonyms()){
            relationTypes.addAll(TaxonRelationshipType.proParteOrPartialSynonymUuids());
        }
        return relationTypes;
    }

    @Override
    public long count(TaxonNodeFilter filter){
        return nodeFilterDao.count(filter);
    }

    @Override
    public List<UUID> uuidList(TaxonNodeFilter filter){
        return nodeFilterDao.listUuids(filter);
    }

    @Override
    public List<Integer> idList(TaxonNodeFilter filter){
        return nodeFilterDao.idList(filter);
    }

    @Override
    public TaxonNodeDto findCommonParentDto(Collection<TaxonNodeDto> nodes) {
        TaxonNodeDto commonParent = null;
        List<String> treePath = null;
        for (TaxonNodeDto nodeDto : nodes) {
            String nodeTreeIndex = nodeDto.getTreeIndex();
            nodeTreeIndex = nodeTreeIndex.replaceFirst("#", "");
            String[] split = nodeTreeIndex.split("#");
            if(treePath == null){
                treePath = Arrays.asList(split);
            }
            else{
                List<String> match = new ArrayList<>();
                for(int i=0;i<treePath.size();i++){
                    if(i>=split.length){
                        //current tree index is shorter so break
                        break;
                    }
                    else if(split[i].equals(treePath.get(i))){
                        //match found
                        match.add(treePath.get(i));
                    }
                    else{
                        //first mismatch found
                        break;
                    }
                }
                treePath = match;
                if(treePath.isEmpty()){
                    //no common parent found for at least two nodes
                    //-> they belong to a different classification
                    break;
                }
            }
        }
        if(treePath!=null && !treePath.isEmpty()) {
            //get last index
            int nodeId = Integer.parseInt(treePath.get(treePath.size()-1));
            TaxonNode taxonNode = dao.load(nodeId, null);
            commonParent = new TaxonNodeDto(taxonNode);
        }
        return commonParent;
    }

    @Override
    public List<TaxonDistributionDTO> getTaxonDistributionDTO(List<UUID> nodeUuids, List<String> propertyPaths,
            Authentication authentication, boolean openChildren, TaxonNodeSortMode sortMode){

        nodeUuids = nodeUuids.stream().distinct().collect(Collectors.toList());
        List<TaxonNode> nodes = new ArrayList<>();

        List<TaxonNode> parentNodes = load(nodeUuids, propertyPaths);
        if (sortMode != null){
            parentNodes.sort(sortMode.comparator());
        }
        if (openChildren){
            //TODO we could remove nodes which are children of other nodes in parentNodes list here as they are duplicates
            for (TaxonNode node: parentNodes){
                if (node == null || nodes.contains(node)){
                    continue;
                }
                nodes.add(node);
                List<TaxonNode> children = new ArrayList<>();
                children.addAll(loadChildNodesOfTaxonNode(node,
                        propertyPaths, true,  true, sortMode));
                for (TaxonNode child: children){
                    if (!nodes.contains(child)){
                        nodes.add(child);
                    }
                }
            }
        }else{
            nodes.addAll(nodes);
        }

        List<TaxonDistributionDTO> result = new ArrayList<>();
        boolean hasPermission = false;
        for(TaxonNode node: nodes){
            if (authentication != null ) {
                hasPermission = permissionEvaluator.hasPermission(authentication, node, Operation.UPDATE);
            }else {
                hasPermission = true;
            }
            if (node.getTaxon() != null && hasPermission){
                try{
                    TaxonDistributionDTO dto = new TaxonDistributionDTO(node);
                    result.add(dto);
                }catch(Exception e){
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    @Override
    public <S extends TaxonNode> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths) {
        return page(clazz, restrictions, pageSize, pageIndex, orderHints, propertyPaths, INCLUDE_UNPUBLISHED);
    }

    @Override
    public <S extends TaxonNode> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths, boolean includeUnpublished) {

        List<S> records;
        long resultSize = dao.count(clazz, restrictions);
        if(AbstractPagerImpl.hasResultsInRange(resultSize, pageIndex, pageSize)){
            records = dao.list(clazz, restrictions, pageSize, pageIndex, orderHints, propertyPaths, includeUnpublished);
        } else {
            records = new ArrayList<>();
        }
        Pager<S> pager = new DefaultPagerImpl<>(pageIndex, resultSize, pageSize, records);
        return pager;
    }

    @Override
    public List<TaxonDistributionDTO> getTaxonDistributionDTO(List<UUID> nodeUuids,
            List<String> propertyPaths, boolean openChildren) {
        return getTaxonDistributionDTO(nodeUuids, propertyPaths, null, openChildren, null);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult cloneSubtree(SubtreeCloneConfigurator config) {
        UpdateResult result = new UpdateResult();

        if (config.getSubTreeUuids().isEmpty()){
            return result;
        }

        //TODO error handling
        Reference taxonSecundum = config.isReuseTaxa() || config.isReuseTaxonSecundum() || config.getTaxonSecundumUuid() == null ?
                null : referenceDao.findByUuid(config.getTaxonSecundumUuid());
        config.setTaxonSecundum(taxonSecundum);

        Reference parentChildReference = config.isReuseParentChildReference() || config.getParentChildReferenceUuid() == null ?
                null : referenceDao.findByUuid(config.getParentChildReferenceUuid());
        config.setParentChildReference(parentChildReference);

        Reference taxonRelationshipReference = config.getRelationTypeToOldTaxon() == null ?
                null : referenceDao.findByUuid(config.getRelationshipReferenceUuid());
        config.setRelationshipReference(taxonRelationshipReference);

        Classification classificationClone = Classification.NewInstance(config.getNewClassificationName());

        if (config.isReuseClassificationReference()){
            TaxonNode anyNode = dao.findByUuid(config.getSubTreeUuids().iterator().next());
            if (anyNode != null){
                Reference oldClassificationRef = anyNode.getClassification().getReference();
                classificationClone.setReference(oldClassificationRef);
            }
        }else if (config.getClassificationReferenceUuid() != null) {
            Reference classificationReference = referenceDao.findByUuid(config.getClassificationReferenceUuid());
            classificationClone.setReference(classificationReference);
        }

        //clone taxa and taxon nodes
//      List<Integer> childNodeIds = taxonNodeService.idList(taxonNodeFilter);
//      List<TaxonNode> childNodes = taxonNodeService.loadByIds(childNodeIds, null);
        List<TaxonNode> rootNodes = this.find(config.getSubTreeUuids());
        for (TaxonNode taxonNode : rootNodes) {
            cloneTaxonRecursive(taxonNode, classificationClone.getRootNode(), config);
        }
        classificationDao.saveOrUpdate(classificationClone);
        result.setCdmEntity(classificationClone);
        return result;
    }

    private void cloneTaxonRecursive(TaxonNode originalParentNode, TaxonNode parentNodeClone,
            SubtreeCloneConfigurator config){

        Taxon originalTaxon = CdmBase.deproxy(originalParentNode.getTaxon());
        TaxonNode childNodeClone;
        if (originalTaxon != null){
            String microReference = null;
            if (config.isReuseTaxa()){
                childNodeClone = parentNodeClone.addChildTaxon(originalTaxon, config.getParentChildReference(), microReference);
            }else{
                Taxon cloneTaxon = originalTaxon.clone(config.isIncludeSynonymsIncludingManAndProParte(),
                        config.isIncludeTaxonRelationshipsExcludingManAndProParte(),
                        config.isIncludeDescriptiveData(), config.isIncludeMedia());

                //name
                if (!config.isReuseNames()){
                    cloneTaxon.setName(cloneTaxon.getName().clone());
                    //TODO needs further handling for name relationships etc., see #9349
                    cloneTaxon.getSynonyms().forEach(syn ->
                        syn.setName(syn.getName() == null ? null : syn.getName().clone()));
                }

                if (!config.isReuseTaxonSecundum()){
                    cloneTaxon.setSec(config.getTaxonSecundum());
                }

                //add relation between taxa
                if (config.getRelationTypeToOldTaxon() != null){
                    TaxonRelationship rel = cloneTaxon.addTaxonRelation(originalParentNode.getTaxon(), config.getRelationTypeToOldTaxon(),
                            config.getRelationshipReference(), microReference);
                    rel.setDoubtful(config.isRelationDoubtful());
                }
                childNodeClone = parentNodeClone.addChildTaxon(cloneTaxon, config.getParentChildReference(), microReference);
            }

            //probably necessary as taxon nodes do not cascade
            dao.saveOrUpdate(childNodeClone);

        }else{
            childNodeClone = parentNodeClone;
        }
        //add children
        if (config.isDoRecursive()){
            List<TaxonNode> originalChildNodes = originalParentNode.getChildNodes();
            HHH_9751_Util.removeAllNull(originalChildNodes);

            for (TaxonNode originalChildNode : originalChildNodes) {
                cloneTaxonRecursive(originalChildNode, childNodeClone, config);
            }
        }
    }

    @Override
    public HomotypicGroupDto getHomotypicGroupDto(UUID homotypicGroupUuid, UUID nodeUuid) {

        HomotypicalGroup group = homotypicalGroupDao.load(homotypicGroupUuid);
        if (group == null){
            return null;
        }
        return new HomotypicGroupDto(group, nodeUuid);
    }

    @Override
    public TaxonNodeDto getTaxonNodeDto(UUID nodeUuid) {
        return dao.getTaxonNodeDto(nodeUuid);
    }

    @Override
    public List<TaxonNodeDto> getTaxonNodeDtos(List<UUID> nodeUuids) {
        return dao.getTaxonNodeDtos(nodeUuids);
    }
}