/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.molecular;


import etaxonomy.cdm.model.occurrence.ObservationalUnit;
import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:24
 */
public class TissueSample extends VersionableEntity {
	static Logger logger = Logger.getLogger(TissueSample.class);

	@Description("")
	private String description;
	@Description("")
	private Calendar samplingDate;
	private ObservationalUnit sampledFrom;

	public ObservationalUnit getSampledFrom(){
		return sampledFrom;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSampledFrom(ObservationalUnit newVal){
		sampledFrom = newVal;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
	}

	public Calendar getSamplingDate(){
		return samplingDate;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSamplingDate(Calendar newVal){
		samplingDate = newVal;
	}

}