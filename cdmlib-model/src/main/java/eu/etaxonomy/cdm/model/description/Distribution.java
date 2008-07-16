/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import eu.etaxonomy.cdm.model.location.NamedArea;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;

/**
 * fact attribute contains the concrete occurrence term like "Extinct" This allows
 * all terms to enter the database and classify them basically according to class
 * hierarchy of distribution.  {validInRegion mandatory} {type is "distribution"}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
public class Distribution extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(Distribution.class);
	
	private NamedArea area;
	private PresenceAbsenceTermBase status;

	
	/**
	 * Creates an empty distribution. Feature set to <code>Feature.DISTRIBUTION</code>
	 * @return
	 */
	public static Distribution NewInstance(){
		Distribution result = new Distribution();
		return result;
	}

	/**
	 * Creates a distribution and sets the area and status. Feature is set to <code>Feature.DISTRIBUTION</code>
	 * @return
	 */
	public static Distribution NewInstance(NamedArea area, PresenceAbsenceTermBase status){
		Distribution result = new Distribution();
		result.setArea(area);
		result.setStatus(status);
		return result;
	}
	
	protected Distribution(){
		super();
		this.setFeature(Feature.DISTRIBUTION());
	}
	
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public NamedArea getArea(){
		return this.area;
	}
	public void setArea(NamedArea area){
		this.area = area;
	}

	@ManyToOne
	public PresenceAbsenceTermBase getStatus(){
		return this.status;
	}
	public void setStatus(PresenceAbsenceTermBase status){
		this.status = status;
	}

}