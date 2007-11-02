/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:46
 */
public class TypeDesignationStatus extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(TypeDesignationStatus.class);

	@Description("")
	private static final int initializationClassUri = http://rs.tdwg.org/ontology/voc/TaxonName#NomencalturalTypeTypeTerm;

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri
	 */
	public void setInitializationClassUri(initializationClassUri){
		;
	}

	public static final TypeDesignationStatus HOLOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus LECTOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus NEOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus EPITYPE(){
		return null;
	}

	public static final TypeDesignationStatus ISOLECTOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus ISONEOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus ISOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus PARANEOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus PARATYPE(){
		return null;
	}

	public static final TypeDesignationStatus SECOND_STEP_LECTOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus SECOND_STEP_NEOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus SYNTYPE(){
		return null;
	}

	public static final TypeDesignationStatus PARALECTOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus ISOEPITYPE(){
		return null;
	}

	public static final TypeDesignationStatus ICONOTYPE(){
		return null;
	}

	public static final TypeDesignationStatus PHOTOTYPE(){
		return null;
	}

}