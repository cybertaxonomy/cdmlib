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
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
	
	public TaxonSTO getSTO(TaxonBase tb, Enumeration<Locale> locales){		
		TaxonSTO t = null;
		if (tb!=null){
			t = new TaxonSTO();
			setVersionableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName(), null));
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
	
	public TaxonTO getTO(TaxonBase tb, Enumeration<Locale> locales){
		TaxonTO t = null;
		if(tb!=null){
			t = new TaxonTO();
			setVersionableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName(), null));
			t.setSec(refAssembler.getTO(tb.getSec(), null));
			//TODO: add more mappings
			if(tb instanceof Taxon){
				tb = (Taxon)tb;
			    if(tb instanceof Taxon){
			    	Taxon taxon = (Taxon) tb;
			    	List<Synonym> syns = taxon.getHomotypicGroup().getSynonymsInGroup(taxon.getSec());
			    	t.setHomotypicSynonyms(getSynonymRelationshipTOs(syns, taxon, locales));
//			    	List<HomotypicalGroup> heterotypicGroups = taxon.getHeterotypicSynonymyGroups();
//			    	for (HomotypicalGroup homotypicalGroup : heterotypicGroups) {
//			    		t.setHeterotypicSynonymyGroups(....);						
//					}
			    }
			}
		}
		return t;
	}
	
	public List<SynonymRelationshipTO> getSynonymRelationshipTOs(List<Synonym> syns, Taxon t, Enumeration<Locale> locales){
		List<SynonymRelationshipTO> r = new ArrayList<SynonymRelationshipTO>(syns.size());
		for (Synonym s : syns) {
			r.add(getSynonymRelationshipTO(s, t, locales));
		}
		return r;
	}
	
	public SynonymRelationshipTO getSynonymRelationshipTO(Synonym syn, Taxon t, Enumeration<Locale> locales){
		SynonymRelationshipTO sr = new SynonymRelationshipTO();
		if(syn != null){
			sr.setSynoynm(getSTO(syn, null));
			sr.setType(termAssembler.getSTO(syn.getRelationType(t), locales));
		}
		return sr;
	}
	
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
