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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNote
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:09
 */
@Entity
public class HybridRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(HybridRelationship.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@Description("The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in the note property.")
	private String ruleConsidered;
	private BotanicalName parentName;
	private HybridRelationshipType type;
	private BotanicalName hybridName;

	public HybridRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(HybridRelationshipType type){
		;
	}

	public BotanicalName getParentName(){
		return parentName;
	}

	/**
	 * 
	 * @param parentName
	 */
	public void setParentName(BotanicalName parentName){
		;
	}

	public BotanicalName getHybridName(){
		return hybridName;
	}

	/**
	 * 
	 * @param hybridName
	 */
	public void setHybridName(BotanicalName hybridName){
		;
	}

	public String getRuleConsidered(){
		return ruleConsidered;
	}

	/**
	 * 
	 * @param ruleConsidered
	 */
	public void setRuleConsidered(String ruleConsidered){
		;
	}

}