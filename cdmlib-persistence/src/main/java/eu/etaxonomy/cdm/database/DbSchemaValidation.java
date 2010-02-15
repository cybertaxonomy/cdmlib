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
 * 
 * Wrapper class for the hibernate <code>hibernate.hbm2ddl.auto</code> property.<BR><BR>
 * <UL>
 * <LI>VALIDATE will validate the existing schema and throw an exception if the schema is not compatible</LI>
 * <LI>UPDATE will try to update the existing db schema to be compatible with the new schema</LI>
 * <LI>CREATE works as UPDATE but also empties the needed tables </LI>
 * <LI>CREATE-DROP works as CREATE but also drops all tables after closing the session factory (use only for 
 * testing or for creating temporary database schemas)</LI>
 * </UL>
 
 * @see {@link http://docs.jboss.org/hibernate/stable/core/reference/en/html_single/#configuration-programmatic} 
 * 
 * @author a.mueller
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
