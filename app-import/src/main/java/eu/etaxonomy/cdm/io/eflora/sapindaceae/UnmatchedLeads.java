/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.eflora.sapindaceae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class UnmatchedLeads {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(UnmatchedLeads.class);

	
	private Map<UnmatchedLeadsKey, Set<FeatureNode>> map = new HashMap<UnmatchedLeadsKey, Set<FeatureNode>>();
	
	protected static class UnmatchedLeadsKey{
		protected static UnmatchedLeadsKey NewInstance(PolytomousKey key, String num){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.key = key;
			result.num = num;
			return result;
		}
		
		protected static UnmatchedLeadsKey NewInstance(Taxon taxon, String num){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.taxon = taxon;
			result.num = num;
			return result;
		}
		//firstPart
		PolytomousKey key;
		Taxon taxon;
		
		//secondPart
		String num;
		
		@Override
		public boolean equals(Object object){
			if (object == null ||   ! (object instanceof UnmatchedLeadsKey)){
				return false;
			}
			UnmatchedLeadsKey unmatchedLeadsKey = (UnmatchedLeadsKey)object;
			if (! CdmUtils.nullSafeEqual(this.num, unmatchedLeadsKey.num)){
				return false;
			}
			//all null
			if (this.key == null && unmatchedLeadsKey.key == null && this.taxon == null && unmatchedLeadsKey.taxon == null){
				return true;
			}
			//key != null && taxon == null
			if (this.key != null && unmatchedLeadsKey.key != null && this.taxon == null && unmatchedLeadsKey.taxon == null){
				return this.key.equals(unmatchedLeadsKey.key);
			}
			//key == null && taxon != null
			if (this.key == null && unmatchedLeadsKey.key == null && this.taxon != null && unmatchedLeadsKey.taxon != null){
				return this.taxon.equals(unmatchedLeadsKey.taxon);
			}
			//else false
			return false;
		}
		
		@Override
		public int hashCode() {
			   int hashCode = 29 * 7;
			   if(this.key != null && this.key.getUuid() != null) {
				   hashCode = hashCode + this.key.getUuid().hashCode();
			   }
			   if(this.taxon != null && this.taxon.getUuid() != null) {
				   hashCode = hashCode + this.taxon.getUuid().hashCode();
			   }
			   if(this.num != null) {
				   hashCode = hashCode + this.num.hashCode();
			   }
			   return hashCode;
		}
		
		@Override
		public String toString(){
			String result = "";
			result += num;
			if (this.key != null){
				result += ":" + this.key.getUuid();
			}
			if (this.taxon != null){
				result += ":" + this.taxon.getUuid();
			}
			return result;
		}
	}
	
	
//************************* FACTORY METHODS ********************************/
	
	public static UnmatchedLeads NewInstance(){
		return new UnmatchedLeads();
	}
	
//************************* METHODS ********************************/
	
	
	public void addKey(UnmatchedLeadsKey key, FeatureNode node){
		Set<FeatureNode> nodes = map.get(key);
		if (nodes == null){
			nodes = new HashSet<FeatureNode>();
			map.put(key, nodes);
		}
		nodes.add(node);
	}
	
	public Set<FeatureNode> getNodes(UnmatchedLeadsKey key){
		Set<FeatureNode> result = new HashSet<FeatureNode>();
		Set<FeatureNode> nodes = map.get(key);
		if (nodes != null){
			result.addAll(nodes);
		}
		return result;
	}

	public boolean removeNode(UnmatchedLeadsKey key, FeatureNode matchingNode) {
		Set<FeatureNode> nodes = map.get(key);
		if (nodes != null){
			boolean result = nodes.remove(matchingNode);
			if (nodes.isEmpty()){
				map.remove(key);
			}
			return result;
		}
		return false;
	}
	
	
}
