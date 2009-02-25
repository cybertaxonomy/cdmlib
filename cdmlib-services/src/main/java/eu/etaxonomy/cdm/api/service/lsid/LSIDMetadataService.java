package eu.etaxonomy.cdm.api.service.lsid;

import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDService;
import eu.etaxonomy.cdm.model.common.LSID;
/** * LSIDMetadataService interface which was altered from com.ibm.lsid.server.LSIDMetadataService, * replacing the LSIDRequestContext with a simple LSID. My thinking behind this is that provided the  * LSIDMetadataService has no responsibility for security, then there is no need to pass that * information to it (in an LSIDRequestContext). This allows better separation of concerns in that methods * requiring authentication and authorization can be secured transparently within CATE without the need for  * the LSIDMetadataService to know anything about it. I could be wrong, of course. *  * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>) * @author ben * * @see com.ibm.lsid.server.LSIDMetadataService * @see com.ibm.lsid.server.LSIDRequestContext  */
public interface LSIDMetadataService extends LSIDService {	
	/**
	 * Get the meta data associated with the LSID
	 * @param LSIDContext lsid
	 * @return MetadataResponse value contains an IdentifiableEntity, null if there is no meta data
	 */
	public MetadataResponse getMetadata(LSID lsid, String[] acceptedFormats) throws LSIDServerException;
}
