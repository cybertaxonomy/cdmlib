/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.integration;

import java.sql.Types;

public class HSQLDialect extends org.hibernate.dialect.HSQLDialect {
	
	public HSQLDialect() {
		super();
		registerColumnType(Types.BIT, "boolean"); 
	}

}
