/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@Entity
public class Synonym extends TaxonBase {
	static Logger logger = Logger.getLogger(Synonym.class);
	private Set<SynonymRelationship> synoynmRelations;


	@OneToMany
	public Set<SynonymRelationship> getSynoynmRelations() {
		return synoynmRelations;
	}
	protected void setSynoynmRelations(Set<SynonymRelationship> synoynmRelations) {
		this.synoynmRelations = synoynmRelations;
	}
	public void addSynoynmRelations(SynonymRelationship synoynmRelation) {
		this.synoynmRelations.add(synoynmRelation);
	}
	public void removeSynoynmRelations(SynonymRelationship synoynmRelation) {
		this.synoynmRelations.remove(synoynmRelation);
	}


	@Override
	public String generateTitle(){
		return "";
	}

}