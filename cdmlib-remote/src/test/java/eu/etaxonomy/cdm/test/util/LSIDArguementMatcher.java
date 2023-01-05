/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.util;

import org.easymock.IArgumentMatcher;

import eu.etaxonomy.cdm.model.common.LSID;

public class LSIDArguementMatcher implements IArgumentMatcher {

	private LSID expected;

	public LSIDArguementMatcher(LSID expected) {
		super();
		this.expected = expected;
	}

	@Override
    public void appendTo(StringBuffer buffer) {
		buffer.append("LSID matches " + expected.toString());
	}

	@Override
    public boolean matches(Object actual) {
		return (actual instanceof LSID)
        && ((LSID) actual).equals(expected);
	}
}
