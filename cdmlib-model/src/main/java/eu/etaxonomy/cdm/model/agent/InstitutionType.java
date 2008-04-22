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
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * Represents an element of a controlled vocabulary for different kinds of institutions.
 * Each element belongs to one {@link common.TermVocabulary vocabulary}.
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

	/** 
	 * Class constructor.
	 * 
	 * @see #InstitutionType(String, String)
	 */
	public InstitutionType() {
		super();
		// TODO Auto-generated constructor stub
	}

	/** 
	 * Class constructor using both term and label strings.
	 *
	 * @param  term   the string describing this vocabulary element
	 * 				  in the default language
	 * @param  label  the string which identifies this vocabulary element
	 * 				  irrespective of the language
	 * @see           common.Representation
	 * @see           common.TermBase#TermBase(String, String)
	 */
	public InstitutionType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

	
}