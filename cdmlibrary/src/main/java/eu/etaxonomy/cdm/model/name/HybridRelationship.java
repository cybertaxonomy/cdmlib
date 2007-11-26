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

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNote
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:26
 */
@Entity
public class HybridRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(HybridRelationship.class);
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	private String ruleConsidered;
	private BotanicalName parentName;
	private HybridRelationshipType type;
	private BotanicalName hybridName;

	@ManyToOne
	public HybridRelationshipType getType(){
		return this.type;
	}
	public void setType(HybridRelationshipType type){
		this.type = type;
	}

	@ManyToOne
	public BotanicalName getParentName(){
		return this.parentName;
	}
	public void setParentName(BotanicalName parentName){
		this.parentName = parentName;
	}

	@ManyToOne
	public BotanicalName getHybridName(){
		return this.hybridName;
	}
	public void setHybridName(BotanicalName hybridName){
		this.hybridName = hybridName;
	}

	public String getRuleConsidered(){
		return this.ruleConsidered;
	}
	public void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}