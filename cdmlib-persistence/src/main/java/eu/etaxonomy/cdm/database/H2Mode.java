/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;

/**
 * @author AM
 * @since 09.06.2008
 * @version 1.0
 */
public enum H2Mode {
	EMBEDDED,
	IN_MEMORY,
	TCP;
	private static final Logger logger = Logger.getLogger(H2Mode.class);
	
	
	private static final String embedded = "embedded";
	private static final String inMemory = "inMemory";
	private static final String tcp = "tcp";

	public String toString(){
		if (this.equals(H2Mode.EMBEDDED)){
			return embedded;
		}else if (this.equals(H2Mode.IN_MEMORY)){
			return inMemory;
		}else if (this.equals(H2Mode.TCP)){
			return tcp;
		}else{
			logger.warn("toString for mode not yet implemented");
			return "";
		}
		
	}
	
	public static H2Mode fromString(String modeString){
		if (modeString == null){
			return null;
		}else if (modeString.equals(H2Mode.EMBEDDED.toString())){
			return H2Mode.EMBEDDED;
		}else if (modeString.equals(H2Mode.IN_MEMORY.toString())){
			return H2Mode.IN_MEMORY;
		}else if (modeString.equals(H2Mode.IN_MEMORY.toString())){
			return H2Mode.TCP;
		}else{
			logger.warn("Unknown modeString");
			return null;
		}
	}
	
}
