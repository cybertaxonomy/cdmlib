/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 05.08.2008
 * @version 1.0
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