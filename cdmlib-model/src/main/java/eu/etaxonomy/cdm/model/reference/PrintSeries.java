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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrintSeries", propOrder = {
//    "series"
})
@XmlRootElement(name = "PrintSeries")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class PrintSeries extends PublicationBase<IReferenceBaseCacheStrategy<PrintSeries>> implements Cloneable {
	private static final long serialVersionUID = -6723799677497340157L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PrintSeries.class);
	
//	@XmlElement(name = "Series")
//	@Field(index=Index.TOKENIZED)
//	private String series;

	protected PrintSeries() {
		this.type = ReferenceType.PrintSeries;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<PrintSeries>();
	}
	
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
	
	
	
	/** 
	 * Clones <i>this</i> print series instance. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * print series instance by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PrintSeries clone(){
		PrintSeries result = (PrintSeries)super.clone();
		//no changes to: series
		return result;
	}
}