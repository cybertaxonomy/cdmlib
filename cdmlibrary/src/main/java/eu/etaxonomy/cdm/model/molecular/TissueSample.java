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
 * @created 02-Nov-2007 19:36:39
 */
@Entity
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
	 * @param sampledFrom
	 */
	public void setSampledFrom(ObservationalUnit sampledFrom){
		;
	}

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description){
		;
	}

	public Calendar getSamplingDate(){
		return samplingDate;
	}

	/**
	 * 
	 * @param samplingDate
	 */
	public void setSamplingDate(Calendar samplingDate){
		;
	}

}