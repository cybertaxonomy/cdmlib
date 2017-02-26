/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.markup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

public class UnmatchedLeads {
	private static final Logger logger = Logger.getLogger(UnmatchedLeads.class);


	private final Map<UnmatchedLeadsKey, Set<PolytomousKeyNode>> map = new HashMap<UnmatchedLeadsKey, Set<PolytomousKeyNode>>();

	public static class UnmatchedLeadsKey{
		public static UnmatchedLeadsKey NewInstance(PolytomousKey key, String num){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.key = key;
			result.num = num == null? null : num.toLowerCase();
			return result;
		}

		public static UnmatchedLeadsKey NewInstance(String num, String taxon){
			num = (StringUtils.isBlank(num) ? "" : num + " " );
			return NewInstance(num + taxon);
		}

		public static UnmatchedLeadsKey NewInstance(String numAndTaxon){
			UnmatchedLeadsKey result = new UnmatchedLeadsKey();
			result.numAndTaxon = numAndTaxon.toLowerCase();
			return result;
		}

		//firstPart
		PolytomousKey key;
		//secondPart
		String num;

		//taxonKey
		String numAndTaxon;


		public boolean isInnerLead(){
			return (key != null);
		}

		public boolean isTaxonLead(){
			return (numAndTaxon != null);
		}

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


	/**
	 * Adds a polytomous key node to the
	 * @param key
	 * @param node
	 */
	public void addKey(UnmatchedLeadsKey key, PolytomousKeyNode node){
		Set<PolytomousKeyNode> nodes = map.get(key);
		if (nodes == null){
			nodes = new HashSet<>();
			map.put(key, nodes);
		}else{
			String message = "A key node for this key does already exist: %s";
			message = String.format(message, key.toString());
			logger.info(message);
		}
		nodes.add(node);
	}

	public Set<PolytomousKeyNode> getNodes(UnmatchedLeadsKey key){
		Set<PolytomousKeyNode> result = new HashSet<>();
		Set<PolytomousKeyNode> nodes = map.get(key);
		if (nodes != null){
			result.addAll(nodes);
		}
		return result;
	}

	public boolean removeNode(UnmatchedLeadsKey key, PolytomousKeyNode matchingNode) {
		Set<PolytomousKeyNode> nodes = map.get(key);
		if (nodes != null){
			boolean result = nodes.remove(matchingNode);
			if (nodes.isEmpty()){
				map.remove(key);
			}
			return result;
		}
		return false;
	}


	public int size(){
		return map.size();
	}

	/**
	 * SaveOrUpdates all polytomousKeyNodes in the unmatchedLeadsKey map.
	 * Used to move nodes from one transaction to another.
	 * @param service
	 */
	public void saveToSession(IPolytomousKeyNodeService service){
		Set<PolytomousKeyNode> allNodes = new HashSet<PolytomousKeyNode>();
		for (Set<PolytomousKeyNode> set :map.values()){
			allNodes.addAll(set);
		}
		service.saveOrUpdate(allNodes);
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
