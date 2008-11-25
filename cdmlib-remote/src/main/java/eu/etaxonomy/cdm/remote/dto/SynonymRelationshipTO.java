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


/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:52
 *
 */
public class SynonymRelationshipTO {
	
	private ArrayList<LocalisedTermSTO> typeRepresenations = new ArrayList<LocalisedTermSTO>();
	private String typeUuid;
	private TaxonSTO synonym;
	
	public TaxonSTO getSynonym() {
		return synonym;
	}
	public void setSynonym(TaxonSTO synonym) {
		this.synonym = synonym;
	}
	public ArrayList<LocalisedTermSTO> getTypeRepresentations() {
		return typeRepresenations;
	}
	public void setTypeRepresentations(ArrayList<LocalisedTermSTO> types) {
		this.typeRepresenations = types;
	}
	public void addTypeRepresentation(LocalisedTermSTO sto) {
		typeRepresenations.add(sto);
	}
	/**
	 * @return the uuid
	 */
	public String getTypeUuid() {
		return typeUuid;
	}
	/**
	 * @param uuid the uuid to set
	 */
	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

}
