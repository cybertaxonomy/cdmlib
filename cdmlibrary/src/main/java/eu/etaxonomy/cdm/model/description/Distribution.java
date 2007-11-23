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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
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
public class Distribution extends FeatureBase {
	static Logger logger = Logger.getLogger(Distribution.class);
	private NamedArea area;
	private PresenceAbsenceTermBase status;

	@ManyToOne
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