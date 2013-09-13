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
