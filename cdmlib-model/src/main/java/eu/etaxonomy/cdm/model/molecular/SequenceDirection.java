/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.util.UUID;

/**
 * @author a.mueller
 * @created 2013-07-11
 */
public enum SequenceDirection {
	Forward("e611de24-09bf-468f-b6ee-e34124022912", "FWD"), 
	Reverse("d116fb2c-00e7-46a4-86b4-74c46ca2afa0", "REV")
	;
	
	private UUID uuid;
	private String key;
	
	private SequenceDirection(String uuidString, String key){
		uuid = UUID.fromString(uuidString);
		this.key = key;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getKey() {
		return key;
	}

}
