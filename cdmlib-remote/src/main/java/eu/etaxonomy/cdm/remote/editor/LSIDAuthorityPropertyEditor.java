/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.model.common.LSIDAuthority;

public class LSIDAuthorityPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
		try {
			setValue(new LSIDAuthority(text));
		} catch (MalformedLSIDException e) {
			throw new IllegalArgumentException(e);
		}
	}
}