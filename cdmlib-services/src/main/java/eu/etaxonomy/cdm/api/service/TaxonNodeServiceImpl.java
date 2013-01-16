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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.ITaxonNodeComparator;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TaxonNodeServiceImpl extends AnnotatableServiceBase<TaxonNode, ITaxonNodeDao> implements ITaxonNodeService{
	private static final Logger logger = Logger.getLogger(TaxonNodeServiceImpl.class);

	@Autowired
	private IBeanInitializer defaultBeanInitializer;
	
	private Comparator<? super TaxonNode> taxonNodeComparator;
	@Autowired
	public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
		this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
	}

	public TaxonNode getTaxonNodeByUuid(UUID uuid) {
		return dao.findByUuid(uuid);
	}
	
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
	@Transactional(readOnly = false)
	public Synonym makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymRelationshipType synonymRelationshipType, Reference citation, String citationMicroReference) {

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
		TaxonNameBase<?,?> synonymName = oldTaxon.getName();
		if (synonymRelationshipType == null){
			if (synonymName.isHomotypic(newAcceptedTaxon.getName())){
				synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}else{
				synonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			}
		}
		SynonymRelationship synonmyRelationship = newAcceptedTaxon.addSynonymName(synonymName, synonymRelationshipType, citation, citationMicroReference);
		
		//Move Synonym Relations to new Taxon
		for(SynonymRelationship synRelation : oldTaxon.getSynonymRelations()){
			newAcceptedTaxon.addSynonym(synRelation.getSynonym(), synRelation.getType(), 
					synRelation.getCitation(), synRelation.getCitationMicroReference());
		}

		
		// CHILD NODES
		if(oldTaxonNode.getChildNodes() != null && oldTaxonNode.getChildNodes().size() != 0){
			for(TaxonNode childNode : oldTaxonNode.getChildNodes()){
				newAcceptedTaxonNode.addChildNode(childNode, childNode.getReference(), childNode.getMicroReference(), childNode.getSynonymToBeUsed());
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
				taxonRelationship.getFromTaxon().addTaxonRelation(newAcceptedTaxon, taxonRelationship.getType(), 
						taxonRelationship.getCitation(), taxonRelationship.getCitationMicroReference());

			}else{
				logger.warn("Taxon is not part of its own Taxonrelationship");
			}
			// Remove old relationships
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
				
		oldTaxonNode.delete();
		
		return synonmyRelationship.getSynonym();
	}
}
