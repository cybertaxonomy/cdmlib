/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:08
 */
@Entity
public class AbsenceTerm extends PresenceAbsenceTermBase<AbsenceTerm> {
	static Logger logger = Logger.getLogger(AbsenceTerm.class);

	public static AbsenceTerm NewInstance(){
		return new AbsenceTerm();
	}
	
	public static AbsenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new AbsenceTerm(term, label, labelAbbrev);
	}
	
	public AbsenceTerm() {
		super();
	}

	public AbsenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
}