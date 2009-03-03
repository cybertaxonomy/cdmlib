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
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing single-access fixed dichotomous or polytomous authored
 * decision keys (as opposed to {@link FeatureTree multiple-access keys}) used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to assign {@link Taxon taxa} to).
 * The determination process is based on the tree structure of the key and on
 * the statements of its leads.
 * 
 * @author m.doering 
 * @version 1.0
 * @created 08-Nov-2007 13:06:28
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentificationKey", propOrder = {
    "coveredTaxa",
    "taxonomicScope",
    "geoScopes"
})
@XmlRootElement(name = "IdentificationKey")
@Entity
@Audited
public class IdentificationKey extends Media {
	private static final long serialVersionUID = -29095811051894471L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IdentificationKey.class);
	
	/*
     * FIXME - shouldn't this be @ManyToMany - i.e. many keys can refer to the
	 * same taxon and some taxa will be covered by multiple keys?
	 */
	@XmlElementWrapper(name = "CoveredTaxa")
	@XmlElement(name = "CoveredTaxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToMany(fetch = FetchType.LAZY)
	private Set<Taxon> coveredTaxa = new HashSet<Taxon>();
	
	@XmlElementWrapper( name = "GeoScopes")
	@XmlElement( name = "GeoScope")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	private Set<NamedArea> geoScopes = new HashSet<NamedArea>();
	
	@XmlElementWrapper(name = "TaxonomicScope")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	        name="IdentificationKey_Taxon",
	        joinColumns=@JoinColumn(name="identificationKey_fk"),
	        inverseJoinColumns=@JoinColumn(name="taxon_fk")
	)
	private Set<Taxon> taxonomicScope = new HashSet<Taxon>();
	
	/** 
	 * Class constructor: creates a new empty identification key instance.
	 */
	protected IdentificationKey() {
		super();
	}
	
	/** 
	 * Creates a new empty identification key instance.
	 */
	public static IdentificationKey NewInstance(){
		return new IdentificationKey();
	}

	
	/** 
	 * Returns the set of possible {@link Taxon taxa} corresponding to
	 * <i>this</i> identification key.
	 */
	public Set<Taxon> getCoveredTaxa() {
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
	public Set<NamedArea> getGeoScopes() {
		return geoScopes;
	}
	
	/**
	 * Adds a {@link NamedArea geoScope} to the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	geoScope	the named area to be added to <i>this</i> identification key
	 * @see    	   		 	#getGeoScopes()
	 */
	public void addGeoScope(NamedArea geoScope) {
		this.geoScopes.add(geoScope);
	}
	/** 
	 * Removes one element from the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	geoScope	the named area which should be removed
	 * @see     			#getGeoScopes()
	 * @see     			#addGeoScope(NamedArea)
	 */
	public void removeGeoScope(NamedArea geoScope) {
		this.geoScopes.remove(geoScope);
	}

	/** 
	 * Returns the set of {@link Taxon taxa} that define the taxonomic
	 * scope of <i>this</i> identification key 
	 */
	public Set<Taxon> getTaxonomicScope() {
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
}