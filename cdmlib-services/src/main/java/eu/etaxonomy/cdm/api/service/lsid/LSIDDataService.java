/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid;
import java.io.InputStream;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDService;

import eu.etaxonomy.cdm.model.common.LSID;
/** * LSIDDataService interface which was altered from com.ibm.lsid.server.LSIDDataService, * replacing the LSIDRequestContext with a simple LSID. My thinking behind this is that provided the  * LSIDDataService has no responsibility for security, then there is no need to pass that * information to it (in an LSIDRequestContext). This allows better separation of concerns in that methods * requiring authentication and authorization can be secured transparently within CATE without the need for  * the LSIDAuthorityService to know anything about it. I could be wrong, of course. * 
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>) 
 * @author ben *  * @see com.ibm.lsid.server.LSIDDataService * @see com.ibm.lsid.server.LSIDRequestContext 
 */
public interface LSIDDataService extends LSIDService {
	/**
	 * Get the data associated with the LSID
	 * @param LSIDContext lsid
	 * @return InputStream an input stream to the data, null if no data exists
	 */
	public InputStream getData(LSID lsid) throws LSIDServerException;
	/**
	 * Get the data range associated with the LSID
	 * @param LSIDContext lsid
	 * @param int start the 0 offset of the first byte to retrieve
	 * @param int length the number of bytes to retrieve
	 * @return InputStream an input stream to the data, null if no data exists
	 */
	public InputStream getDataByRange(LSID lsid, Integer start, Integer length) throws LSIDServerException;
}
