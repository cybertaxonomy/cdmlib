/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import eu.etaxonomy.cdm.model.common.LSID;


public class LSIDAdapter extends XmlAdapter<String, LSID>{

	public String marshal(LSID lsid) throws Exception {
		return lsid.getLsid();
	}

	public LSID unmarshal(String string) throws Exception {
		return new LSID(string);
	}

}
