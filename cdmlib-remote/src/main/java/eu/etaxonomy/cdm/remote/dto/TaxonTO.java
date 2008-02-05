/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Data Transfer Object derived from {@link Taxon}.
 * The TaxonTO is always the accepted taxon.
 * Descriptions are not included, and therefore have to be queried separately from the web service.
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0
 * @created 11.12.2007 12:11:29
 *
 */
public class TaxonTO extends BaseTO {

	private NameSTO name;
	/**
	 * The concept reference
	 */
	private ReferenceTO sec;
	
	private List<SynonymRelationshipTO> homotypicSynonyms = new ArrayList();
	private List<HomotypicGroupTO> heterotypicSynonymyGroups = new ArrayList();
	private Set<TaxonRelationshipTO> taxonRelations = new HashSet();
	
}
