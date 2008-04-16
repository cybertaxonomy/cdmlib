/**
 * 
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
