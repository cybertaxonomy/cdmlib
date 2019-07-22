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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.NodeDeletionConfigurator.ChildHandling;
import eu.etaxonomy.cdm.api.service.config.TermNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.ITermNodeDao;

/**
 * @author n.hoffmann
 * @since Aug 5, 2010
 */
@Service
@Transactional(readOnly = false)
public class FeatureNodeServiceImpl extends VersionableServiceBase<TermNode, ITermNodeDao> implements IFeatureNodeService {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FeatureNodeServiceImpl.class);

	@Override
    @Autowired
	protected void setDao(ITermNodeDao dao) {
		this.dao = dao;
	}

	@Autowired
    private ITermService termService;

	@Autowired
	private IVocabularyService vocabularyService;

	 @Override
	 @Transactional(readOnly = false)
	 public DeleteResult deleteFeatureNode(UUID nodeUuid, TermNodeDeletionConfigurator config) {
	     DeleteResult result = new DeleteResult();
         TermNode<Feature> node = CdmBase.deproxy(dao.load(nodeUuid));
	     result = isDeletable(nodeUuid, config);
	     if (result.isOk()){
	         TermNode<Feature> parent = node.getParent();
             parent = CdmBase.deproxy(parent);
	         List<TermNode<Feature>> children = new ArrayList<>(node.getChildNodes());

	         if (config.getChildHandling().equals(ChildHandling.DELETE)){

	             for (TermNode<Feature> child: children){
	                 deleteFeatureNode(child.getUuid(), config);
	                // node.removeChild(child);
	             }
	             if (parent != null){
	                 parent.removeChild(node);
	             }

	         } else{

	             if (parent != null){
	                 parent.removeChild(node);
	                 for (TermNode<Feature> child: children){
	                     node.removeChild(child);
	                     parent.addChild(child);
	                 }
	             }else{
	                 result.setAbort();
	                 result.addException(new ReferencedObjectUndeletableException("The root node can not be deleted without its child nodes"));
	                 return result;
	             }
	         }

	         dao.delete(node);
	         result.addDeletedObject(node);
	         if(parent!=null){
	             result.addUpdatedObject(parent);
	         }
	         if (config.isDeleteElement()){
	             DefinedTermBase<?> term = node.getTerm();
                 termService.delete(term.getUuid());
                 result.addDeletedObject(term);
             }
	     }
	     return result;
	 }

	 @Override
     public UpdateResult createChildFeatureNode(UUID parentNodeUuid, DefinedTermBase term, UUID vocabularyUuid){
	     TermVocabulary vocabulary = vocabularyService.load(vocabularyUuid);

	     vocabulary.addTerm(term);
	     vocabularyService.save(vocabulary);
	     return addChildFeatureNode(parentNodeUuid, term.getUuid());
	 }

     @Override
     public UpdateResult addChildFeatureNode(UUID nodeUUID, UUID termChildUuid){
         return addChildFeatureNode(nodeUUID, termChildUuid, 0);
     }

	 @Override
	 public UpdateResult addChildFeatureNode(UUID nodeUUID, UUID termChildUuid, int position){
	     TermNode node = load(nodeUUID);
	     DefinedTermBase child = HibernateProxyHelper.deproxy(termService.load(termChildUuid), DefinedTermBase.class);

	     TermNode childNode;
         UpdateResult result = new UpdateResult();
         if(position<0) {
             childNode = node.addChild(child);
         }
         else{
             childNode = node.addChild(child, position);
         }
         save(childNode);
         result.addUpdatedObject(node);
         result.setCdmEntity(childNode);
         return result;
     }

	 @Override
	 public DeleteResult isDeletable(UUID nodeUuid, TermNodeDeletionConfigurator config){
	     TermNode<Feature> node = load(nodeUuid);
	     DeleteResult result = new DeleteResult();
	     Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(node);
	     for (CdmBase ref:references){
	         if (ref instanceof TermNode){
	             break;
	         }
	         if (ref instanceof TermTree){
	             TermTree<Feature> refTree = HibernateProxyHelper.deproxy(ref, TermTree.class);
	             if (node.getGraph().equals((refTree))){
	                 break;
	             }
	         }
	         result.setAbort();
	         result.addException(new ReferencedObjectUndeletableException("The featureNode is referenced by " + ref.getUserFriendlyDescription() +" with id " +ref.getId()));

	     }
	     return result;
	 }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid, int position) {
        UpdateResult result = new UpdateResult();
        TermNode<Feature> movedNode = HibernateProxyHelper.deproxy(load(movedNodeUuid), TermNode.class);
        TermNode<Feature> targetNode = HibernateProxyHelper.deproxy(load(targetNodeUuid), TermNode.class);
        TermNode<Feature> parent = HibernateProxyHelper.deproxy(movedNode.getParent(), TermNode.class);
        if(position<0){
            targetNode.addChild(movedNode);
        }
        else{
            targetNode.addChild(movedNode, position);
        }
        result.addUpdatedObject(targetNode);
        if(parent!=null){
            result.addUpdatedObject(parent);
        }
        result.setCdmEntity(movedNode);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateResult moveFeatureNode(UUID movedNodeUuid, UUID targetNodeUuid) {
        return moveFeatureNode(movedNodeUuid, targetNodeUuid, -1);
    }

}
