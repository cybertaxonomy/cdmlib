/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * Controlled vocabulary to diferenciate levels of areas such as province, state,
 * etc.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@Entity
public class NamedAreaLevel extends OrderedTermBase<NamedAreaLevel> {
	static Logger logger = Logger.getLogger(NamedAreaLevel.class);

	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaLevel NewInstance(){
		return new NamedAreaLevel();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static NamedAreaLevel NewInstance(String term, String label, String labelAbbrev){
		return new NamedAreaLevel(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	protected NamedAreaLevel() {
		super();
	}

	protected NamedAreaLevel(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	/**
	 * continents
	 */
	public static final NamedAreaLevel TDWG_LEVEL1(){
		return null;
	}

	/**
	 * larger regions
	 */
	public static final NamedAreaLevel TDWG_LEVEL2(){
		return null;
	}

	/**
	 * mostly countries
	 */
	public static final NamedAreaLevel TDWG_LEVEL3(){
		return null;
	}

	public static final NamedAreaLevel TDWG_LEVEL4(){
		return null;
	}

	public static final NamedAreaLevel NATURE_RESERVE(){
		return null;
	}

	public static final NamedAreaLevel STATE(){
		return null;
	}

	public static final NamedAreaLevel PROVINCE(){
		return null;
	}

	public static final NamedAreaLevel TOWN(){
		return null;
	}

}