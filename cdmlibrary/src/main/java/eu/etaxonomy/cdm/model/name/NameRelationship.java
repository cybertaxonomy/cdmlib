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
 * @created 02-Nov-2007 18:43:30
 */
public class NameRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NameRelationship.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@Description("The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in the note property.")
	private String ruleConsidered;
	private TaxonNameBase fromName;
	private NameRelationshipType type;
	private TaxonNameBase toName;

	public TaxonNameBase getFromName(){
		return fromName;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFromName(TaxonNameBase newVal){
		fromName = newVal;
	}

	public NameRelationshipType getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(NameRelationshipType newVal){
		type = newVal;
	}

	public TaxonNameBase getToName(){
		return toName;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setToName(TaxonNameBase newVal){
		toName = newVal;
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