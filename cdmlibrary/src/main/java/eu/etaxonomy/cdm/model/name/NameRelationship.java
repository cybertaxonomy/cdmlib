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
 * @created 08-Nov-2007 13:06:37
 */
@Entity
public class NameRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NameRelationship.class);
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	private String ruleConsidered;
	private TaxonNameBase fromName;
	private NameRelationshipType type;
	private TaxonNameBase toName;

	public TaxonNameBase getFromName(){
		return this.fromName;
	}

	/**
	 * 
	 * @param fromName    fromName
	 */
	public void setFromName(TaxonNameBase fromName){
		this.fromName = fromName;
	}

	public NameRelationshipType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(NameRelationshipType type){
		this.type = type;
	}

	public TaxonNameBase getToName(){
		return this.toName;
	}

	/**
	 * 
	 * @param toName    toName
	 */
	public void setToName(TaxonNameBase toName){
		this.toName = toName;
	}

	public String getRuleConsidered(){
		return this.ruleConsidered;
	}

	/**
	 * 
	 * @param ruleConsidered    ruleConsidered
	 */
	public void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}