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

public class ClassAdapter extends XmlAdapter<String, Class> {

	@Override
	public String marshal(Class clazz) throws Exception {
		return clazz.getCanonicalName();
	}

	@Override
	public Class unmarshal(String string) throws Exception {
		return Class.forName(string);
	}

}
