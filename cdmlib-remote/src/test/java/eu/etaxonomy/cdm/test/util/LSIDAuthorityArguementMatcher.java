package eu.etaxonomy.cdm.test.util;

import org.easymock.IArgumentMatcher;

import eu.etaxonomy.cdm.model.common.LSIDAuthority;

public class LSIDAuthorityArguementMatcher implements IArgumentMatcher {

	private LSIDAuthority expected;
	
	public LSIDAuthorityArguementMatcher(LSIDAuthority expected) {
		super();
		this.expected = expected;
	}
	
	public void appendTo(StringBuffer buffer) {
		buffer.append("LSIDAuthority matches " + expected.toString());
	}

	public boolean matches(Object actual) {
		return (actual instanceof LSIDAuthority) 
        && ((LSIDAuthority) actual).equals(expected);
	}

}
