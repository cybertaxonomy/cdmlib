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

import eu.etaxonomy.cdm.model.common.LSIDAuthority;

public class LSIDAuthorityArguementMatcher implements IArgumentMatcher {

	private LSIDAuthority expected;

	public LSIDAuthorityArguementMatcher(LSIDAuthority expected) {
		super();
		this.expected = expected;
	}

	@Override
    public void appendTo(StringBuffer buffer) {
		buffer.append("LSIDAuthority matches " + expected.toString());
	}

	@Override
    public boolean matches(Object actual) {
		return (actual instanceof LSIDAuthority)
        && ((LSIDAuthority) actual).equals(expected);
	}
}