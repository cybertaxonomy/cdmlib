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
 * @created 08-Nov-2007 13:06:44
 */
@Entity
public class PresenceTerm extends PresenceAbsenceTermBase<PresenceTerm> {
	private static final Logger logger = Logger.getLogger(PresenceTerm.class);

	public static PresenceTerm NewInstance(){
		return new PresenceTerm();
	}
	

	public static PresenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new PresenceTerm(term, label, labelAbbrev);
	}
	
	protected PresenceTerm() {
		super();
	}

	protected PresenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

}