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

import org.apache.log4j.Logger;


/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@Embeddable
public class TimePeriod {
	private Calendar start;
	private Calendar end;

	public TimePeriod() {
		// TODO Auto-generated constructor stub
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
	
}