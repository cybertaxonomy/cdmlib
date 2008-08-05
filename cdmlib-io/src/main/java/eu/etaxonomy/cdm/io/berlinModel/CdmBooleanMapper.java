/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import eu.etaxonomy.cdm.io.common.CdmIoMapperBase;

/**
 * @author a.mueller
 *
 */
public class CdmBooleanMapper extends CdmIoMapperBase {

	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	public CdmBooleanMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}

	public Class getTypeClass(){
		return Boolean.class;
	}
}
