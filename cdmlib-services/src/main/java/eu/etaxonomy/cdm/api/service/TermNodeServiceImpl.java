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
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @since Jul 22, 2019
 */
@Service
@Transactional(readOnly = false)
public class TermNodeServiceImpl
        extends VersionableServiceBase<TermNode, ITermNodeDao>
        implements ITermNodeService {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermNodeServiceImpl.class);

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
    public List<TermNode> list(TermType termType, Integer limit, Integer start,
	        List<OrderHint> orderHints, List<String> propertyPaths){
	    return dao.list(termType, limit, start, orderHints, propertyPaths);
	}

	@Override
	@Transactional(readOnly = false)
	public DeleteResult deleteNode(UUID nodeUuid, TermNodeDeletionConfigurator config) {
	    DeleteResult result = new DeleteResult();
        TermNode<Feature> node = CdmBase.deproxy(dao.load(nodeUuid));
	    result = isDeletable(nodeUuid, config);
	    if (result.isOk()){
	        TermNode<Feature> parent = node.getParent();
            parent = CdmBase.deproxy(parent, TermNode.class);
	        List<TermNode> children = new ArrayList<>(node.getChildNodes());

	        if (config.getChildHandling().equals(ChildHandling.DELETE)){

	            for (TermNode child: children){
	                deleteNode(child.getUuid(), config);
	               // node.removeChild(child);
	            }
	            if (parent != null){
	                parent.removeChild(node);
	            }

	        } else{

	            if (parent != null){
	                parent.removeChild(node);
	                for (TermNode child: children){
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
	             DefinedTermBase term = node.getTerm();
                 termService.delete(term.getUuid());
                 result.addDeletedObject(term);
             }
	     }
	     return result;
	 }

	 @Override
     public UpdateResult createChildNode(UUID parentNodeUuid, DefinedTermBase term, UUID vocabularyUuid){
	     TermVocabulary vocabulary = vocabularyService.load(vocabularyUuid);

	     vocabulary.addTerm(term);
	     vocabularyService.save(vocabulary);
	     return addChildNode(parentNodeUuid, term.getUuid());
	 }

     @Override
     public UpdateResult addChildNode(UUID nodeUUID, UUID termChildUuid){
         return addChildNode(nodeUUID, termChildUuid, 0);
     }

	 @Override
	 public UpdateResult addChildNode(UUID nodeUUID, UUID termChildUuid, int position){
	     UpdateResult result = new UpdateResult();

	     TermNode node = load(nodeUUID);
	     DefinedTermBase child = HibernateProxyHelper.deproxy(termService.load(termChildUuid), DefinedTermBase.class);

	     if(!node.getGraph().isAllowDuplicates() && node.getGraph().getDistinctTerms().contains(child)){
	         result.setError();
	         result.addException(new Exception("This term tree does not allow duplicate terms."));
	     }

	     TermNode childNode;
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
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid, int position) {
        UpdateResult result = new UpdateResult();
        TermNode movedNode = CdmBase.deproxy(load(movedNodeUuid));
        TermNode targetNode = CdmBase.deproxy(load(targetNodeUuid));
        TermNode parent = CdmBase.deproxy(movedNode.getParent());
        if(position < 0){
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
    public UpdateResult moveNode(UUID movedNodeUuid, UUID targetNodeUuid) {
        return moveNode(movedNodeUuid, targetNodeUuid, -1);
    }

}
