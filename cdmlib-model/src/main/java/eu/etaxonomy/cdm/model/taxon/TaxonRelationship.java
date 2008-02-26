/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@Entity
public class TaxonRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(TaxonRelationship.class);
	private TaxonRelationshipType type;
	private Taxon fromTaxon;
	private Taxon toTaxon;

	@ManyToOne
	public TaxonRelationshipType getType(){
		return this.type;
	}

	public void setType(TaxonRelationshipType type){
		this.type = type;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Taxon getFromTaxon(){
		return this.fromTaxon;
	}
	public void setFromTaxon(Taxon fromTaxon){
		this.fromTaxon = fromTaxon;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Taxon getToTaxon(){
		return this.toTaxon;
	}

	public void setToTaxon(Taxon toTaxon){
		this.toTaxon = toTaxon;
	}

}