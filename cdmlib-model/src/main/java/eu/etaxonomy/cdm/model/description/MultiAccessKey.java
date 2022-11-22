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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

/**
 *
 * The class representing multi-access dynamic keys used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to assign {@link Taxon taxa} to).
 * The determination process is performed by an identification software.
 *
 * @author h.fradin
 * @since 13.08.2009
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
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptiveDataSet")
@Audited

public class MultiAccessKey extends DescriptiveDataSet implements IIdentificationKey {

	private static final long serialVersionUID = -240407483572972239L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@XmlElementWrapper(name = "CoveredTaxa")
	@XmlElement(name = "CoveredTaxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="MultiAccessKey_CoveredTaxon")
    @NotNull
	private Set<Taxon> coveredTaxa = new HashSet<>();

	@XmlElementWrapper(name = "TaxonomicScope")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="MultiAccessKey_TaxonScope")
	@NotNull
	private Set<Taxon> taxonomicScope = new HashSet<>();

	@XmlElementWrapper( name = "GeographicalScope")
	@XmlElement( name = "Area")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="MultiAccessKey_NamedArea")
	@NotNull
	private Set<NamedArea> geographicalScope = new HashSet<>();

	@XmlElementWrapper( name = "ScopeRestrictions")
	@XmlElement( name = "Restriction")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="MultiAccessKey_Scope")
	@NotNull
	private Set<DefinedTerm> scopeRestrictions = new HashSet<>();

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
	@Override
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
	@Override
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
	@Override
    public void removeCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.remove(taxon);
	}

	/**
	 * Returns the set of {@link NamedArea named areas} indicating the geospatial
	 * data where <i>this</i> identification key is valid.
	 */
	@Override
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
	@Override
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
	@Override
    public void removeGeographicalScope(NamedArea geoScope) {
		this.geographicalScope.remove(geoScope);
	}

	/**
	 * Returns the set of {@link Taxon taxa} that define the taxonomic
	 * scope of <i>this</i> identification key
	 */
	@Override
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
	@Override
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
	@Override
    public void removeTaxonomicScope(Taxon taxon) {
		this.taxonomicScope.remove(taxon);
	}

	/**
	 * Returns the set of {@link Scope scope restrictions} corresponding to
	 * <i>this</i> identification key
	 */
	@Override
    public Set<DefinedTerm> getScopeRestrictions() {
		if(scopeRestrictions == null) {
			this.scopeRestrictions = new HashSet<DefinedTerm>();
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
	@Override
    public void addScopeRestriction(DefinedTerm scopeRestriction) {
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
	@Override
    public void removeScopeRestriction(DefinedTerm scopeRestriction) {
		this.scopeRestrictions.remove(scopeRestriction);
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> MultiAccessKey. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> MultiAccessKey by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MultiAccessKey clone() {
		MultiAccessKey result;

			result = (MultiAccessKey)super.clone();

			result.coveredTaxa = new HashSet<Taxon>();
			for (Taxon taxon: this.coveredTaxa){
				result.addCoveredTaxon(taxon);
			}

			result.geographicalScope = new HashSet<NamedArea>();
			for (NamedArea area: this.geographicalScope){
				result.addGeographicalScope(area);
			}

			result.scopeRestrictions = new HashSet<DefinedTerm>();
			for (DefinedTerm scope: this.scopeRestrictions){
				result.addScopeRestriction(scope);
			}

			result.taxonomicScope = new HashSet<Taxon>();
			for (Taxon taxon: this.taxonomicScope){
				result.addTaxonomicScope(taxon);
			}
			return result;

	}
}
