/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.molecular;


import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@Entity
public class TissueSample extends VersionableEntity {
	static Logger logger = Logger.getLogger(TissueSample.class);
	private String description;
	private Calendar samplingDate;
	private ObservationalUnit sampledFrom;

	public ObservationalUnit getSampledFrom(){
		return this.sampledFrom;
	}

	/**
	 * 
	 * @param sampledFrom    sampledFrom
	 */
	public void setSampledFrom(ObservationalUnit sampledFrom){
		this.sampledFrom = sampledFrom;
	}

	public String getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
	public void setDescription(String description){
		this.description = description;
	}

	public Calendar getSamplingDate(){
		return this.samplingDate;
	}

	/**
	 * 
	 * @param samplingDate    samplingDate
	 */
	public void setSamplingDate(Calendar samplingDate){
		this.samplingDate = samplingDate;
	}

}