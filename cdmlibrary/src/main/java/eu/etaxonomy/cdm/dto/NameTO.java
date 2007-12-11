/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * Data Transfer Object representing a taxonomic Name. The fields are mainly derived from the 
 * domain object {@link TaxonNameBase}. The <code>typeDesignations</code> field however is not 
 * included since these will obtained by a separate call to the web service.
 * 
 * @author a.kohlbecker
 * @author  m.doering
 * @version 1.0 r$LastChangedRevision$
 * @created 11.12.2007 11:04:42
 */
public class NameTO extends BaseTO {

	private String fullname;
	private List<TaggedText> taggedName = new ArrayList();
	
	private Set<ReferenceTO> typeDesignations;
	private Set<NameRelationshipTO> nameRelations;
	private Set<LocalisedRepresentationTO> status;
	private LocalisedRepresentationTO rank;
	private NomenclaturalReferenceTO nomenclaturalReference;
	private Set<NameTO> newCombinations;
	private NameTO basionym;
	

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public List<TaggedText> getTaggedName() {
		return taggedName;
	}

	protected void addNameToken(TaggedText token) {
		this.taggedName.add(token);
	}

}
