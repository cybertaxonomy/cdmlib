/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;

import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@Entity
public class PrintSeries extends PublicationBase implements Cloneable {
	static Logger logger = Logger.getLogger(PrintSeries.class);
	private String series;

	public String getSeries(){
		return this.series;
	}

	/**
	 * 
	 * @param series    series
	 */
	public void setSeries(String series){
		this.series = series;
	}

	@Override
	public String generateTitle(){
		return "";
	}
	
//*********** CLONE **********************************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.PublicationBase#clone()
	 */
	public PrintSeries clone(){
		PrintSeries result = (PrintSeries)super.clone();
		//no changes to: series
		return result;
	}
}