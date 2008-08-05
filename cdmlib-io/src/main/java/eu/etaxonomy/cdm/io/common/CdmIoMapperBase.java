/**
 * 
 */
package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public abstract class CdmIoMapperBase {
	private static final Logger logger = Logger.getLogger(CdmIoMapperBase.class);
	
	private String sourceValue;
	private String destinationValue;

	protected CdmIoMapperBase(String dbAttributString, String cdmAttributeString){
		this.sourceValue = dbAttributString;
		this.destinationValue = cdmAttributeString;
	}
	
	public String getSourceAttribute(){
		return sourceValue;
	}

	public String getDestinationAttribute(){
		return destinationValue;
	}
	
	public abstract Class getTypeClass();
}
