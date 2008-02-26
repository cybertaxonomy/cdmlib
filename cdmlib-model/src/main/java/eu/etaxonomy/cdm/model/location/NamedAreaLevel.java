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
 * Controlled vocabulary to diferenctiate levels of areas such as province, state,
 * etc.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:36
 */
@Entity
public class NamedAreaLevel extends OrderedTermBase<NamedAreaLevel> {
	static Logger logger = Logger.getLogger(NamedAreaLevel.class);

	public NamedAreaLevel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NamedAreaLevel(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
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