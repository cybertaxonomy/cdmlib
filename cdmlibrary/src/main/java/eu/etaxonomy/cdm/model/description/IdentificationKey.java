/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Media;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Dichotomous or multifurcating authored keys (incl. legacy data)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */
@Entity
public class IdentificationKey extends Media {
	static Logger logger = Logger.getLogger(IdentificationKey.class);
	private Set<Taxon> coveredTaxa;
	
	@OneToMany
	public Set<Taxon> getCoveredTaxa() {
		return coveredTaxa;
	}
	protected void setCoveredTaxa(Set<Taxon> coveredTaxa) {
		this.coveredTaxa = coveredTaxa;
	}
	public void addCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.add(taxon);
	}
	public void removeCoveredTaxon(IdentifiableEntity taxon) {
		this.coveredTaxa.remove(taxon);
	}


}