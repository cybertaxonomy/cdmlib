/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.eflora.floraMalesiana;

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
	private static final Logger logger = Logger.getLogger(UnmatchedLeads.class);

	
	private Map<UnmatchedLeadsKey, Set<FeatureNode>> map = new HashMap<UnmatchedLeadsKey, Set<FeatureNode>>();
	
	protected static class UnmatchedLeadsKey{
		protected static UnmatchedLeadsKey NewInstance(PolytomousKey key, String num){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.key = key;
			result.num = num;
			return result;
		}
		
		protected static UnmatchedLeadsKey NewInstance(String num, String taxon){
			num = (num == null ? "" : num + " " );
			return NewInstance(num + taxon);
		}
		
		protected static UnmatchedLeadsKey NewInstance(String numAndTaxon){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.numAndTaxon = numAndTaxon;
			return result;
		}
		
		//firstPart
		PolytomousKey key;
		//secondPart
		String num;
		
		//taxonKey
		String numAndTaxon;
		
		
		@Override
		public boolean equals(Object object){
			if (object == null ||   ! (object instanceof UnmatchedLeadsKey)){
				return false;
			}
			UnmatchedLeadsKey unmatchedLeadsKey = (UnmatchedLeadsKey)object;
			if (! CdmUtils.nullSafeEqual(this.num, unmatchedLeadsKey.num)){
				return false;
			}
			if (! CdmUtils.nullSafeEqual(this.numAndTaxon, unmatchedLeadsKey.numAndTaxon)){
				return false;
			}
			if (! CdmUtils.nullSafeEqual(this.key, unmatchedLeadsKey.key)){
				return false;
			}
			
//			//all null
//			if (this.key == null && unmatchedLeadsKey.key == null && this.taxon == null && unmatchedLeadsKey.taxon == null){
//				return true;
//			}
//			//key != null && taxon == null
//			if (this.key != null && unmatchedLeadsKey.key != null && this.taxon == null && unmatchedLeadsKey.taxon == null){
//				return this.key.equals(unmatchedLeadsKey.key);
//			}
//			//key == null && taxon != null
//			if (this.key == null && unmatchedLeadsKey.key == null && this.taxon != null && unmatchedLeadsKey.taxon != null){
//				return this.taxon.equals(unmatchedLeadsKey.taxon);
//			}
			//else false
			return true;
		}
		
		@Override
		public int hashCode() {
			   int hashCode = 29 * 7;
			   if(this.key != null && this.key.getUuid() != null) {
				   hashCode = hashCode + this.key.getUuid().hashCode();
			   }
			   if(this.numAndTaxon != null ) {
				   hashCode = hashCode + this.numAndTaxon.hashCode();
			   }
			   if(this.num != null) {
				   hashCode = hashCode + this.num.hashCode();
			   }
			   return hashCode;
		}
		
		@Override
		public String toString(){
			String result = "";
			if (this.num != null){
				result += num;
			}
			if (this.key != null){
				result += ":" + this.key.getUuid();
			}
			if (this.numAndTaxon != null){
				result += this.numAndTaxon;
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
		}else{
			logger.info("A Feature node for this key does already exist: " + key.toString());
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
	
//********************** toString()******************************/
	
	@Override
	public String toString(){
		String result = "[";
		for (UnmatchedLeadsKey key : map.keySet()){
			result += (result.equals("[")? "":"; ") + key.toString();
		}
		return result + "]";
	}
	
	
}
