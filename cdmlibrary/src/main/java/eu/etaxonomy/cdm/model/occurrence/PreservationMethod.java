/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NonOrderedTermBase;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@Entity
public class PreservationMethod extends DefinedTermBase {
	static Logger logger = Logger.getLogger(PreservationMethod.class);

	public PreservationMethod(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}
}