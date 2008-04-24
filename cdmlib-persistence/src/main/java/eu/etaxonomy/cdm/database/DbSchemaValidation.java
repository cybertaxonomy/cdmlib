/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public enum DbSchemaValidation {
	VALIDATE,
	UPDATE,
	CREATE,
	CREATE_DROP;

	@Override
	public String toString(){
		switch (this){
			case VALIDATE:
				return "validate";
			case UPDATE:
				return "update";
			case CREATE:
				return "create";
			case CREATE_DROP:
				return "create-drop";
			default: 
				throw new IllegalArgumentException( "Unknown enumeration type" );
		}
	}
	
}
