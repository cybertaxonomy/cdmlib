/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NonOrderedTermBase;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:18
 */
@Entity
public class Continent extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Continent.class);

	public Continent() {
		super();
	}
	public Continent(String term, String label) {
		super(term, label);
	}

	public static final Continent EUROPE(){
		return null;
	}

	public static final Continent AFRICA(){
		return null;
	}

	public static final Continent ASIA(){
		return null;
	}

	public static final Continent NORTH_AMERICA(){
		return null;
	}

	public static final Continent ANTARCTICA(){
		return null;
	}

	public static final Continent SOUTH_AMERICA(){
		return null;
	}

	public static final Continent OCEANIA(){
		return null;
	}

}