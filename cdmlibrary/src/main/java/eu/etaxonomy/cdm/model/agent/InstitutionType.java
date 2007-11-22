/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import eu.etaxonomy.cdm.model.common.Enumeration;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/InstitutionType
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
@Entity
public class InstitutionType extends EnumeratedTermBase {
	public InstitutionType(String term, String label, Enumeration enumeration) {
		super(term, label, enumeration);
	}

	static Logger logger = Logger.getLogger(InstitutionType.class);

}