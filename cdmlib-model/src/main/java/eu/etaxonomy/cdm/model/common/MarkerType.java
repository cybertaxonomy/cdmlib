/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;

/**
 * Marker types similar to dynamically defined attributes. These  content types
 * like "IS_DOUBTFUL", "COMPLETE"  or specific local flags.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@Entity
public class MarkerType extends DefinedTermBase {
	public static final Logger logger = Logger.getLogger(MarkerType.class);

	private static final UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
	private static final UUID uuidToBeChecked = UUID.fromString("34204192-b41d-4857-a1d4-28992bef2a2a");
	private static final UUID uuidIsDoubtful = UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e");
	private static final UUID uuidComplete = UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433");

	public static MarkerType NewInstance(String term, String label, String labelAbbrev){
		return new MarkerType(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	public MarkerType() {
		super();
	}
	
	/**
	 * Constructor
	 * @param term
	 * @param label
	 */
	protected MarkerType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	

	public static final MarkerType getByUuid(UUID uuid){
		return (MarkerType) findByUuid(uuid);
	}	


	public static final MarkerType IMPORTED(){
		return getByUuid(uuidImported);
	}

	public static final MarkerType TO_BE_CHECKED(){
		return getByUuid(uuidToBeChecked);
	}

	public static final MarkerType IS_DOUBTFUL(){
		return getByUuid(uuidIsDoubtful);
	}

	public static final MarkerType COMPLETE(){
		return getByUuid(uuidComplete);
	}

}