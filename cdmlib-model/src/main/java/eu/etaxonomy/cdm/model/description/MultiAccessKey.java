// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * 
 * The class representing multi-access dynamic keys used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to assign {@link Taxon taxa} to).
 * The determination process is performed by an identification software.
 * 
 * @author h.fradin
 * @created 13.08.2009
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiAccessKey", propOrder = {
    "coveredTaxa",
    "taxonomicScope",
    "geographicalScope",
    "scopeRestrictions"
})
@XmlRootElement(name = "MultiAccessKey")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.media.WorkingSet")
@Audited

public class MultiAccessKey extends WorkingSet implements IIdentificationKey{
	private static final long serialVersionUID = -240407483572972239L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MultiAccessKey.class);
	
	@XmlElementWrapper(name = "CoveredTaxa")
	@XmlElement(name = "CoveredTaxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@NotNull
	private Set<Taxon> coveredTaxa = new HashSet<Taxon>();
	
	@XmlElementWrapper(name = "TaxonomicScope")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	        name="MultiAccessKey_Taxon",
	        joinColumns=@JoinColumn(name="multiAccessKey_id"),
	        inverseJoinColumns=@JoinColumn(name="taxon_id")
	)
	@NotNull
	private Set<Taxon> taxonomicScope = new HashSet<Taxon>();
	
	@XmlElementWrapper( name = "GeographicalScope")
	@XmlElement( name = "Area")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="MultiAccessKey_NamedArea")
	@NotNull
	private Set<NamedArea> geographicalScope = new HashSet<NamedArea>();
	
	@XmlElementWrapper( name = "ScopeRestrictions")
	@XmlElement( name = "Restriction")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="MultiAccessKey_Scope")
	@NotNull
	private Set<Scope> scopeRestrictions = new HashSet<Scope>();
	
	/** 
	 * Class constructor: creates a new empty multi-access key instance.
	 */
	protected MultiAccessKey() {
		super();
	}
	
	/** 
	 * Creates a new empty identification multi-access key instance.
	 */
	public static MultiAccessKey NewInstance(){
		return new MultiAccessKey();
	}
	
	/** 
	 * Returns the set of possible {@link Taxon taxa} corresponding to
	 * <i>this</i> identification key.
	 */
	public Set<Taxon> getCoveredTaxa() {
		if(coveredTaxa == null) {
			this.coveredTaxa = new HashSet<Taxon>();
		}
		return coveredTaxa;
	}
	/**
	 * @see	#getCoveredTaxa() 
	 */
	protected void setCoveredTaxa(Set<Taxon> coveredTaxa) {
		this.coveredTaxa = coveredTaxa;
	}
	
	/**
	 * Adds a {@link Taxon taxa} to the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	taxon	the taxon to be added to <i>this</i> identification key
	 * @see    	   		#getCoveredTaxa()
	 */
	public void addCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.add(taxon);
	}
	
	/** 
	 * Removes one element from the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	taxon	the taxon which should be removed
	 * @see     		#getCoveredTaxa()
	 * @see     		#addCoveredTaxon(Taxon)
	 */
	public void removeCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.remove(taxon);
	}

	/** 
	 * Returns the set of {@link NamedArea named areas} indicating the geospatial
	 * data where <i>this</i> identification key is valid.
	 */
	public Set<NamedArea> getGeographicalScope() {
		if(geographicalScope == null) {
			this.geographicalScope = new HashSet<NamedArea>();
		} 
		return geographicalScope;
	}
	
	/**
	 * Adds a {@link NamedArea geoScope} to the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	geoScope	the named area to be added to <i>this</i> identification key
	 * @see    	   		 	#getGeoScopes()
	 */
	public void addGeographicalScope(NamedArea geoScope) {
		this.geographicalScope.add(geoScope);
	}
	/** 
	 * Removes one element from the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	geoScope	the named area which should be removed
	 * @see     			#getGeoScopes()
	 * @see     			#addGeoScope(NamedArea)
	 */
	public void removeGeographicalScope(NamedArea geoScope) {
		this.geographicalScope.remove(geoScope);
	}

	/** 
	 * Returns the set of {@link Taxon taxa} that define the taxonomic
	 * scope of <i>this</i> identification key 
	 */
	public Set<Taxon> getTaxonomicScope() {
		if(taxonomicScope == null) {
			this.taxonomicScope = new HashSet<Taxon>();
		}
		return taxonomicScope;
	}
	
	/**
	 * Adds a {@link Taxon taxa} to the set of {@link #getTaxonomicScope() taxonomic scopes}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	taxon	the taxon to be added to <i>this</i> identification key
	 * @see    	   		#getTaxonomicScope()
	 */
	public void addTaxonomicScope(Taxon taxon) {
		this.taxonomicScope.add(taxon);
	}
	
	/** 
	 * Removes one element from the set of {@link #getTaxonomicScope() taxonomic scopes}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	taxon	the taxon which should be removed
	 * @see     		#getTaxonomicScope()
	 * @see     		#addTaxonomicScope(Taxon)
	 */
	public void removeTaxonomicScope(Taxon taxon) {
		this.taxonomicScope.remove(taxon);
	}
	
	/** 
	 * Returns the set of {@link Scope scope restrictions} corresponding to
	 * <i>this</i> identification key 
	 */
	public Set<Scope> getScopeRestrictions() {
		if(scopeRestrictions == null) {
			this.scopeRestrictions = new HashSet<Scope>();
		}
		return scopeRestrictions;
	}
	
	/**
	 * Adds a {@link Scope scope restriction} to the set of {@link #getScopeRestrictions() scope restrictions}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	scopeRestriction	the scope restriction to be added to <i>this</i> identification key
	 * @see    	   		#getScopeRestrictions()
	 */
	public void addScopeRestriction(Scope scopeRestriction) {
		this.scopeRestrictions.add(scopeRestriction);
	}
	
	/** 
	 * Removes one element from the set of {@link #getScopeRestrictions() scope restrictions}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	scopeRestriction	the scope restriction which should be removed
	 * @see     		#getScopeRestrictions()
	 * @see     		#addScopeRestriction(Scope)
	 */
	public void removeScopeRestriction(Scope scopeRestriction) {
		this.scopeRestrictions.remove(scopeRestriction);
	}
}
