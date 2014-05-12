package eu.etaxonomy.cdm.test.util;

import org.easymock.IArgumentMatcher;

import eu.etaxonomy.cdm.model.common.LSID;

public class LSIDArguementMatcher implements IArgumentMatcher {

	private LSID expected;
	
	public LSIDArguementMatcher(LSID expected) {
		super();
		this.expected = expected;
	}
	
	public void appendTo(StringBuffer buffer) {
		buffer.append("LSID matches " + expected.toString());
	}

	public boolean matches(Object actual) {
		return (actual instanceof LSID) 
        && ((LSID) actual).equals(expected);
	}
}
