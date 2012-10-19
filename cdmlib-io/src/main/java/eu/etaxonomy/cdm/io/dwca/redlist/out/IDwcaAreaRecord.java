/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.redlist.out;

import eu.etaxonomy.cdm.model.location.NamedArea;

public interface IDwcaAreaRecord {

	public void setLocationId(NamedArea cdmBase);
	
	public void setLocality(String label);

	public void setCountryCode(String iso3166_A2);
	
}
