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
import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
public class Description extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Description.class);
	//in 95% of all cases this will be the taxon name. getLabel() should return the taxon name in case label is null.
	private String label;
	private Set<FeatureBase> features = new HashSet();
	private Set<Scope> scopes = new HashSet();
	private Set<NamedArea> geoScopes = new HashSet();
	private ReferenceBase source;
	private Set<ObservationalUnit> observationalUnits = new HashSet();
	private Taxon taxon;

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public ReferenceBase getSource(){
		return this.source;
	}
	public void setSource(ReferenceBase source){
		this.source= source;
	}

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

	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<FeatureBase> getFeatures(){
		return this.features;
	}
	protected void setFeatures(Set<FeatureBase> features) {
		this.features = features;
	}
	public void addFeature(FeatureBase feature){
		this.features.add(feature);
	}
	public void removeFeature(FeatureBase feature){
		this.features.remove(feature);
	}

	
	public String getLabel(){
		return this.label;
	}
	public void setLabel(String label){
		this.label = label;
	}

	@Override
	public String generateTitle(){
		return "";
	}


	@OneToMany
	public Set<ObservationalUnit> getObservationalUnits() {
		return observationalUnits;
	}
	protected void setObservationalUnits(Set<ObservationalUnit> observationalUnits) {
		this.observationalUnits = observationalUnits;
	}
	public void addObservationalUnit(ObservationalUnit observationalUnit) {
		this.observationalUnits.add(observationalUnit);
	}
	public void removeObservationalUnit(ObservationalUnit observationalUnit) {
		this.observationalUnits.remove(observationalUnit);
	}
	
	
	@ManyToOne
	public Taxon getTaxon() {
		return taxon;
	}
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

}