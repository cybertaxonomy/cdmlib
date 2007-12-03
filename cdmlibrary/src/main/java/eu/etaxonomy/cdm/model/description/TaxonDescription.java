/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:20
 */
@Entity
public class TaxonDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(TaxonDescription.class);
	private Set<Scope> scopes = new HashSet();
	private Set<NamedArea> geoScopes = new HashSet();
	private Taxon taxon;

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<NamedArea> getGeoScopes(){
		return this.geoScopes;
	}
	protected void setGeoScopes(Set<NamedArea> geoScopes){
		this.geoScopes = geoScopes;
	}
	public void addGeoScope(NamedArea geoScope){
		this.geoScopes.add(geoScope);
	}
	public void removeGeoScope(NamedArea geoScope){
		this.geoScopes.remove(geoScope);
	}

	
	@OneToMany
	public Set<Scope> getScopes(){
		return this.scopes;
	}
	protected void setScopes(Set<Scope> scopes){
		this.scopes = scopes;
	}
	public void addScope(Scope scope){
		this.scopes.add(scope);
	}
	public void removeScope(Scope scope){
		this.scopes.remove(scope);
	}


	@ManyToOne
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

}