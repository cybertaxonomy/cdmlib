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
 * This class represents collections of {@link PrintedUnitBase printed published references} which
 * are grouped according to topic or any other feature. 
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "BookSeries".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@Entity
public class PrintSeries extends PublicationBase implements Cloneable {
	private static final Logger logger = Logger.getLogger(PrintSeries.class);
	private String series;

	/** 
	 * Creates a new empty print series instance.
	 */
	public static PrintSeries NewInstance(){
		PrintSeries result = new PrintSeries();
		return result;
	}
	
	/** 
	 * Creates a new print series instance with a given title string.
	 */
	public static PrintSeries NewInstance(String series){
		PrintSeries result = NewInstance();
		result.setSeries(series);
		return result;
	}
	
	/**
	 * Returns the string representing the title of <i>this</i> print series.
	 * 
	 * @return  the string representing the print series
	 */
	public String getSeries(){
		return this.series;
	}

	/**
	 * @see #getSeries()
	 */
	public void setSeries(String series){
		this.series = series;
	}
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> print series instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * print series instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PrintSeries clone(){
		PrintSeries result = (PrintSeries)super.clone();
		//no changes to: series
		return result;
	}
}