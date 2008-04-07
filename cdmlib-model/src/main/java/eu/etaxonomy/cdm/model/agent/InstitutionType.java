/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * Represents a term of a controlled vocabulary for different kinds of institutions.
 * Each term belongs to one {@link common.TermVocabulary vocabulary}.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/InstitutionType">TDWG Ontology</a>
 * 
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
@Entity
public class InstitutionType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(InstitutionType.class);

	public InstitutionType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InstitutionType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

	
}