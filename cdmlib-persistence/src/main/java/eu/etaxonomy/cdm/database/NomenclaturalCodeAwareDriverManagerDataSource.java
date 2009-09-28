// $Id$
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
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 * @version 1.0
 */
public class NomenclaturalCodeAwareDriverManagerDataSource extends
		DriverManagerDataSource {
	private static final Logger logger = Logger
			.getLogger(NomenclaturalCodeAwareDriverManagerDataSource.class);
	
	private NomenclaturalCode nomenclaturalCode;

	public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}

	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
}
