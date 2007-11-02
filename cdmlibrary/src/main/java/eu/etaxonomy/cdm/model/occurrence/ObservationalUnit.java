/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.common.MultilanguageString;
import eu.etaxonomy.cdm.model.description.Description;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * part of a specimen or observation that is being described or determined.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:28
 */
public class ObservationalUnit extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(ObservationalUnit.class);

	//Description defining the Observational unit in the context of the original Occurrence
	@Description("Description defining the Observational unit in the context of the original Occurrence")
	private MultilanguageString definition;
	private ArrayList descriptions;
	private ArrayList determinations;
	private Occurrence occurence;

	public ArrayList getDeterminations(){
		return determinations;
	}

	/**
	 * 
	 * @param determinations
	 */
	public void setDeterminations(ArrayList determinations){
		;
	}

	public Occurrence getOccurence(){
		return occurence;
	}

	/**
	 * 
	 * @param occurence
	 */
	public void setOccurence(Occurrence occurence){
		;
	}

	public ArrayList getDescriptions(){
		return descriptions;
	}

	/**
	 * 
	 * @param descriptions
	 */
	public void setDescriptions(ArrayList descriptions){
		;
	}

	public MultilanguageString getDefinition(){
		return definition;
	}

	/**
	 * 
	 * @param definition
	 */
	public void setDefinition(MultilanguageString definition){
		;
	}

}