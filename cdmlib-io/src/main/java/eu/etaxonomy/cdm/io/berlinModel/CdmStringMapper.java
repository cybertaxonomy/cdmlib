/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import eu.etaxonomy.cdm.io.common.CdmIoMapperBase;

/**
 * @author a.mueller
 *
 */
public class CdmStringMapper extends CdmIoMapperBase {

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmStringMapper(String dbAttributString, String cdmAttributeString) {
		super(dbAttributString, cdmAttributeString);
	}
	
	public Class getTypeClass(){
		return String.class;
	}

}
