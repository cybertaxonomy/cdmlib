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
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#PublicationStatus
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:27
 */
public class NomenclaturalStatus extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(NomenclaturalStatus.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@Description("The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in the note property.")
	private String ruleConsidered;
	private NomenclaturalStatusType type;

	public NomenclaturalStatusType getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(NomenclaturalStatusType type){
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