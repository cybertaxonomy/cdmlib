package eu.etaxonomy.cdm.database;

public enum HBM2DDL{
	VALIDATE,
	UPDATE,
	CREATE;

	public String getHibernateString(){
		switch (this){
			case VALIDATE:
				return "validate";
			case UPDATE:
				return "update";
			case CREATE:
				return "create";
			default: 
				throw new IllegalArgumentException( "Unknown enumeration type" );
		}
	}
}