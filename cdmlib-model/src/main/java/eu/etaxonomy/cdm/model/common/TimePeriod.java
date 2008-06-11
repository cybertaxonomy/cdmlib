/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.Calendar;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@Embeddable
public class TimePeriod {
	private static final Logger logger = Logger.getLogger(TimePeriod.class);
	
	
	private Calendar start;
	private Calendar end;

	
	/**
	 * Factory method
	 * @return
	 */
	public static TimePeriod NewInstance(){
		return new TimePeriod();
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static TimePeriod NewInstance(Calendar startDate){
		return new TimePeriod(startDate);
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static TimePeriod NewInstance(Calendar startDate, Calendar endDate){
		return new TimePeriod(startDate, endDate);
	}
	
	/**
	 * Constructor
	 */
	protected TimePeriod() {
		super();
	}
	public TimePeriod(Calendar startDate) {
		start=startDate;
	}
	public TimePeriod(Calendar startDate, Calendar endDate) {
		start=startDate;
		end=endDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getStart() {
		return start;
	}
	public void setStart(Calendar start) {
		this.start = start;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEnd() {
		return end;
	}
	public void setEnd(Calendar end) {
		this.end = end;
	}
	
	@Transient
	public String getYear(){
		String result = "";
		if (start != null){
			result += String.valueOf(this.start.get(Calendar.YEAR));
			if (end != null){
				result += "-" + String.valueOf(this.end.get(Calendar.YEAR));
			}
		}else{
			if (end != null){
				result += String.valueOf(this.end.get(Calendar.YEAR));
			}
		}
		return result;
	}
	
}