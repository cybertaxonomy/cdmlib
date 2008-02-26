/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName#NomencalturalTypeTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:00
 */
@Entity
public class TypeDesignationStatus extends OrderedTermBase<TypeDesignationStatus> {
	static Logger logger = Logger.getLogger(TypeDesignationStatus.class);

	public TypeDesignationStatus() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TypeDesignationStatus(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

	public static final TypeDesignationStatus HOLOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus LECTOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus NEOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus EPITYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus ISOLECTOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus ISONEOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus ISOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus PARANEOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus PARATYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus SECOND_STEP_LECTOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus SECOND_STEP_NEOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus SYNTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus PARALECTOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus ISOEPITYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus ICONOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

	public static final TypeDesignationStatus PHOTOTYPE(){
		logger.warn("not yet implemented");
		return null;
	}

}