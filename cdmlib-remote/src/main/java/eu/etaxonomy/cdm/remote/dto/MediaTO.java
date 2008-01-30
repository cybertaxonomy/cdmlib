/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.MediaInstance;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;
import eu.etaxonomy.cdm.model.common.Rights;


/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 12:13:42
 *
 */
public class MediaTO extends BaseTO {

	private Set<LocalisedTermTO> title;
	/**	
	 * creation date of the media (not of the record)
	 */
	private Calendar mediaCreated;
	private Set<LocalisedTermTO> description;
	/**
	 * A single medium such as a picture can have multiple representations in files. 
	 * Common are multiple resolutions or file
	 * formats for images for example
	 */
	
	//FIXME: Is it required to have the instances right in here?
	private Set<MediaInstanceTO> instances = new HashSet();
	
	private Set<RightsTO> rights;
	private IdentifiedString artist;
	
}
