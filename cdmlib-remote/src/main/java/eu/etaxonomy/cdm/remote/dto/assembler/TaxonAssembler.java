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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
			if (Taxon.class.isInstance(tb)){
				t.setAccepted(true);
			}else{
				t.setAccepted(false);
			}
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
		    if(taxonBase instanceof Taxon){
		    	Taxon taxon = (Taxon) taxonBase;
		    	List<Synonym> syns = taxon.getHomotypicSynonymsByHomotypicGroup();
		    	List<Synonym> synList = new ArrayList<Synonym>();
		    	for(Synonym synonym : syns) {
		    		//FIXME remove skip-test hack if "missing synonym type"-bug is fixed 
		    		if(true || synonym.getRelationType(taxon).equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())){
		    			synList.add(synonym);
		    		}
				}
		    	taxonTO.setTypeDesignations(specimenTypeDesignationAssembler.getSTOs(taxon.getHomotypicGroup().getTypeDesignations(), locales));
		    	taxonTO.setHomotypicSynonyms(getSynonymRelationshipTOs(synList, taxon, locales));
		    	List<HomotypicalGroup> heterotypicGroups = taxon.getHeterotypicSynonymyGroups();
		    	List<HomotypicTaxonGroupSTO> heterotypicSynonymyGroups = new ArrayList<HomotypicTaxonGroupSTO>(heterotypicGroups.size());
		    	for (HomotypicalGroup homotypicalGroup : heterotypicGroups) {
		    		heterotypicSynonymyGroups.add(getHomotypicTaxonGroupSTO(homotypicalGroup, taxon, locales));
				}
		    	taxonTO.setHeterotypicSynonymyGroups(heterotypicSynonymyGroups);	
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
	 * @param syn
	 * @param t
	 * @param locales
	 * @return
	 */
	public SynonymRelationshipTO getSynonymRelationshipTO(Synonym syn, Taxon t, Enumeration<Locale> locales){
		SynonymRelationshipTO sr = new SynonymRelationshipTO();
		if(syn != null){
			sr.setSynoynm(getSTO(syn, null));
			sr.setType(termAssembler.getSTO(syn.getRelationType(t), locales));
		}
		return sr;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public TreeNode getTreeNode(Taxon t){
		TreeNode tn = null;
		if(t!=null){
			tn = new TreeNode();
			setVersionableEntity(t, tn);
			tn.setFullname(t.getName().getTitleCache());
			tn.setHasChildren(t.getTaxonomicChildren().size());
			tn.setTaggedName(nameAssembler.getTaggedName(t.getName()));
			tn.setSecUuid(t.getSec().getUuid().toString());
		}
		return tn;
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
