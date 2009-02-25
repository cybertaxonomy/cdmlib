package eu.etaxonomy.cdm.test.integration;

import java.sql.Types;

public class HSQLDialect extends org.hibernate.dialect.HSQLDialect {
	
	public HSQLDialect() {
		super();
		registerColumnType(Types.BIT, "boolean"); 
	}

}
