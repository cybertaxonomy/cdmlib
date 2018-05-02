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

import eu.etaxonomy.cdm.model.common.IKeyTerm;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * A Sequence Direction defines the direction in which a DNA part was read by a {@link Primer}
 * for a {@link SingleRead sequencing process}.
 * This can be either {@link #Forward} or {@link #Reverse}.
 * 
 * @author a.mueller
 * @since 2013-07-11
 */
public enum SequenceDirection implements IKeyTerm {
	Forward("e611de24-09bf-468f-b6ee-e34124022912", "Forward", "FWD"), 
	Reverse("d116fb2c-00e7-46a4-86b4-74c46ca2afa0", "Reverse", "REV")
	;
	
	private UUID uuid;
	private String key;
	private String message;
//	private static Map<String,SequenceDirection> keyMap = new HashMap<String, SequenceDirection>();
	
	private SequenceDirection(String uuidString, String defaultMessage, String key){
		uuid = UUID.fromString(uuidString);
		this.key = key;
//		keyMap.put(key, this);
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getKey() {
		return key;
	}


	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getMessage(Language language) {
		//TODO support i18n
		return message;
	}

	public static IKeyTerm getByKey(String val) {
		for (SequenceDirection dir : SequenceDirection.values()){
			if (dir.key.equals(val)){
				return dir;
			}
		}
		return null;
	}

}
