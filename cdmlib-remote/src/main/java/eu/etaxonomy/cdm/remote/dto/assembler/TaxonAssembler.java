/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.remote.dto.HomotypicTaxonGroupSTO;
import eu.etaxonomy.cdm.remote.dto.SynonymRelationshipTO;
import eu.etaxonomy.cdm.remote.dto.TaxonRelationshipTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

@Component
public class TaxonAssembler extends AssemblerBase<TaxonSTO, TaxonTO, TaxonBase>{
	
	@Autowired
	private NameAssembler nameAssembler;
	@Autowired
	private ReferenceAssembler refAssembler;
	@Autowired
	private LocalisedTermAssembler termAssembler;
	@Autowired
	private SpecimenTypeDesignationAssembler specimenTypeDesignationAssembler;
	@Autowired
	private NameTypeDesignationAssembler nameTypeDesignationAssembler;
	@Autowired
	private DescriptionAssembler descriptionAssembler;
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	public TaxonSTO getSTO(TaxonBase tb, Enumeration<Locale> locales){		
		TaxonSTO t = null;
		if (tb!=null){
			t = new TaxonSTO();
			setVersionableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName(), locales));
			t.setSecUuid(tb.getSec().getUuid().toString());
			t.setAccepted((tb instanceof Taxon));
			//TODO: add more mappings
		}
		return t;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	public TaxonTO getTO(TaxonBase taxonBase, Enumeration<Locale> locales){
		TaxonTO taxonTO = null;
		if(taxonBase!=null){
			taxonTO = new TaxonTO();
			setVersionableEntity(taxonBase, taxonTO);
			taxonTO.setName(nameAssembler.getSTO(taxonBase.getName(), locales));
			taxonTO.setSec(refAssembler.getTO(taxonBase.getSec(), locales));
			taxonTO.setAccepted((taxonBase instanceof Taxon));
		    if(taxonBase instanceof Taxon){
		    	
		    	Taxon taxon = (Taxon) taxonBase;
		    	// -- homotypic & heterotypic synonyms
		    	List<Synonym> syns = taxon.getHomotypicSynonymsByHomotypicRelationship();
		    	List<Synonym> synList = new ArrayList<Synonym>();
		    	for(Synonym synonym : syns) {
		    		//FIXME remove skip-test hack if "missing synonym type"-bug is fixed 
		    		
		    		Set<SynonymRelationshipType> synonymRelationshipTypes = synonym.getRelationType(taxon);
		    		
		    		for (SynonymRelationshipType synonymRelationshipType : synonymRelationshipTypes){
			    		if( synonymRelationshipType.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
			    			synList.add(synonym);
			    		}
		    		}
				}
		    	taxonTO.setTypeDesignations(specimenTypeDesignationAssembler.getSTOs(taxon.getHomotypicGroup().getTypeDesignations(), locales));
		    	
		    	taxonTO.setNameTypeDesignations(nameTypeDesignationAssembler.getSTOs(taxon.getName().getNameTypeDesignations(), locales));
		    	
		    	taxonTO.setHomotypicSynonyms(getSynonymRelationshipTOs(synList, taxon, locales));
		    	List<HomotypicalGroup> heterotypicGroups = taxon.getHeterotypicSynonymyGroups();
		    	List<HomotypicTaxonGroupSTO> heterotypicSynonymyGroups = new ArrayList<HomotypicTaxonGroupSTO>(heterotypicGroups.size());
		    	for (HomotypicalGroup homotypicalGroup : heterotypicGroups) {
		    		heterotypicSynonymyGroups.add(getHomotypicTaxonGroupSTO(homotypicalGroup, taxon, locales));
				}
		    	taxonTO.setHeterotypicSynonymyGroups(heterotypicSynonymyGroups);
		    	
		    	// -- taxon relations (MISAPPLIED_NAME_FOR + INVALID_DESIGNATION)
		    	Set<TaxonRelationshipType> matchTypes = new HashSet<TaxonRelationshipType>();
				matchTypes.add(TaxonRelationshipType.MISAPPLIEDNAMEFOR());
				matchTypes.add(TaxonRelationshipType.INVALIDDESIGNATIONFOR());
		    	taxonTO.setTaxonRelations(getTaxonRelationshipTOs(taxon.getTaxonRelations(), taxon, matchTypes, locales));
		    	
		    	// -- descriptive data
		    	taxonTO.setDescriptions(descriptionAssembler.getTOs(taxon.getDescriptions(), locales));
		    	
		    	//TODO: add more mappings
			}
		}
		return taxonTO;
	}
	
	/**
	 * @param homotypicalGroup
	 * @param taxon
	 * @param locales
	 * @return
	 */
	private HomotypicTaxonGroupSTO getHomotypicTaxonGroupSTO(HomotypicalGroup homotypicalGroup, Taxon taxon,Enumeration<Locale> locales) {
		HomotypicTaxonGroupSTO homotypicTaxonGroupSTO = new HomotypicTaxonGroupSTO();
		homotypicTaxonGroupSTO.setUuid(homotypicalGroup.getUuid().toString());
		
				
		for(TaxonBase tb : homotypicalGroup.getSynonymsInGroup(taxon.getSec())){
			homotypicTaxonGroupSTO.getTaxa().add(getSTO(tb, locales));
		}
		homotypicTaxonGroupSTO.getTypeDesignations().addAll(specimenTypeDesignationAssembler.getSTOs(homotypicalGroup.getTypeDesignations(), locales));
		return homotypicTaxonGroupSTO;
	}

	/**
	 * @param synonyms
	 * @param taxon
	 * @param locales
	 * @return
	 */
	public List<SynonymRelationshipTO> getSynonymRelationshipTOs(List<Synonym> synonyms, Taxon taxon, Enumeration<Locale> locales){
		List<SynonymRelationshipTO> synonymRelationshipTOs = new ArrayList<SynonymRelationshipTO>(synonyms.size());
		for (Synonym synonym : synonyms) {
			synonymRelationshipTOs.add(getSynonymRelationshipTO(synonym, taxon, locales));
		}
		return synonymRelationshipTOs;
	}
	
	/**
	 * @param synonyms
	 * @param taxon
	 * @param locales
	 * @return
	 */
	public List<TaxonRelationshipTO> getTaxonRelationshipTOs(Set<TaxonRelationship> taxonRelationships, Taxon taxon, Set<TaxonRelationshipType> matchTypes, Enumeration<Locale> locales){
		List<TaxonRelationshipTO> taxonRelationshipTOs = new ArrayList<TaxonRelationshipTO>(taxonRelationships.size());
		for (TaxonRelationship taxonRelationship : taxonRelationships) {
			boolean myType = false;
			for (TaxonRelationshipType matchType : matchTypes) {
				myType = myType || matchType.equals(taxonRelationship.getType());
			}
			if(myType){
				taxonRelationshipTOs.add(getTaxonRelationshipTO(taxonRelationship, taxon, locales));
			}
		}
		return taxonRelationshipTOs;
	}
	
	/**
	 * @param syn
	 * @param t
	 * @param locales
	 * @return
	 */
	public TaxonRelationshipTO getTaxonRelationshipTO(TaxonRelationship taxrel, Taxon t, Enumeration<Locale> locales){
		TaxonRelationshipTO to = new TaxonRelationshipTO();
		if(taxrel != null){
			to.setType(termAssembler.getSTO(taxrel.getType(), locales));
			to.setTaxon(getSTO(taxrel.getFromTaxon(), locales));
		}
		return to;
	}
	
	/**
	 * @param synonym
	 * @param taxon
	 * @param locales
	 * @return
	 */
	public SynonymRelationshipTO getSynonymRelationshipTO(Synonym synonym, Taxon taxon, Enumeration<Locale> locales){
		SynonymRelationshipTO synonymRelationshipTO = new SynonymRelationshipTO();
		if(synonym != null){
			synonymRelationshipTO.setSynoynm(getSTO(synonym, locales));
			
			for(TermBase relationType : synonym.getRelationType(taxon)){
				synonymRelationshipTO.addType(termAssembler.getSTO(relationType, locales));
			}
		}
		return synonymRelationshipTO;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public TreeNode getTreeNode(Taxon taxon){
		TreeNode treeNode = null;
		if(taxon!=null){
			treeNode = new TreeNode();
			setVersionableEntity(taxon, treeNode);
			treeNode.setFullname(taxon.getName().getTitleCache());
			treeNode.setHasChildren(taxon.getTaxonomicChildren().size());
			treeNode.setTaggedName(nameAssembler.getTaggedName(taxon.getName()));
			treeNode.setSecUuid(taxon.getSec().getUuid().toString());
		}
		return treeNode;
	}
	public List<TreeNode> getTreeNodeList(Taxon[] taxa){
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		for (Taxon t : taxa){
			result.add(this.getTreeNode(t));
		}
		return result;
	}

	/**
	 * @param taxa
	 * @return
	 */
	public List<TreeNode> getTreeNodeListSortedByName(Iterable<Taxon> taxa){
		Map<String, TreeNode> nameMap = new HashMap<String, TreeNode>();
		for (Taxon t : taxa){
			nameMap.put(t.getTitleCache()+"  "+t.getUuid().toString(), this.getTreeNode(t));
		}
		ArrayList<TreeNode> treeNodeList = new ArrayList<TreeNode>();
		ArrayList<String> keys = new ArrayList<String>(nameMap.keySet());
		Collections.sort(keys);
		for (String name : keys){
			treeNodeList.add(nameMap.get(name));
		}
		return treeNodeList;
	}
	
}
