/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.util;

import org.easymock.EasyMock;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;

public class LSIDMatchers {

    public static LSIDAuthority eqLSIDAuthority(LSIDAuthority lsidAuthority) {
		EasyMock.reportMatcher(new LSIDAuthorityArguementMatcher(lsidAuthority));
		return null;
	}

	public static LSID eqLSID(LSID lsid) {
		EasyMock.reportMatcher(new LSIDArguementMatcher(lsid));
		return null;
	}
}