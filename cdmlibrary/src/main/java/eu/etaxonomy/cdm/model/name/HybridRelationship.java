/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNote
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:20
 */
public class HybridRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(HybridRelationship.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@Description("The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in the note property.")
	private String ruleConsidered;
	private BotanicalName parentName;
	private BotanicalName hybridName;
	private HybridRelationshipType type;

	public HybridRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(HybridRelationshipType newVal){
		type = newVal;
	}

	public BotanicalName getParentName(){
		return parentName;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setParentName(BotanicalName newVal){
		parentName = newVal;
	}

	public BotanicalName getHybridName(){
		return hybridName;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHybridName(BotanicalName newVal){
		hybridName = newVal;
	}

	public String getRuleConsidered(){
		return ruleConsidered;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRuleConsidered(String newVal){
		ruleConsidered = newVal;
	}

}