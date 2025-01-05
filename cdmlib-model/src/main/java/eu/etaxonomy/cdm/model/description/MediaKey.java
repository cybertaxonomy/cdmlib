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
import javax.persistence.OneToMany;
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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * The class representing single-access fixed dichotomous or polytomous authored
 * decision keys (as opposed to {@link TermTree multiple-access keys}) used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to assign {@link Taxon taxa} to).
 * The determination process is based on the tree structure of the key and on
 * the statements of its leads.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:28
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaKey", propOrder = {
    "coveredTaxa",
    "taxonomicScope",
    "geographicalScope",
    "scopeRestrictions",
    "keyRepresentations"
})
@XmlRootElement(name = "MediaKey")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.media.Media")
@Audited
public class MediaKey extends Media implements IIdentificationKey{

	private static final long serialVersionUID = -29095811051894471L;
    private static final Logger logger = LogManager.getLogger();

	@XmlElementWrapper(name = "CoveredTaxa")
	@XmlElement(name = "CoveredTaxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="MediaKey_CoveredTaxon")
    @NotNull
	private Set<Taxon> coveredTaxa = new HashSet<>();

	@XmlElementWrapper( name = "GeographicalScope")
	@XmlElement( name = "Area")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="MediaKey_NamedArea")
	@NotNull
	private Set<NamedArea> geographicalScope = new HashSet<>();

	@XmlElementWrapper(name = "TaxonomicScope")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="MediaKey_TaxonScope")
	@NotNull
	private Set<Taxon> taxonomicScope = new HashSet<>();

	@XmlElementWrapper( name = "ScopeRestrictions")
	@XmlElement( name = "Restriction")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="MediaKey_Scope")
	@NotNull
	private Set<DefinedTerm> scopeRestrictions = new HashSet<>();

	@XmlElementWrapper( name = "KeyRepresentations")
	@XmlElement( name = "KeyRepresentation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToMany(fetch=FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE})
	@NotNull
	private Set<Representation> keyRepresentations = new HashSet<>();

	/**
	 * Class constructor: creates a new empty identification key instance.
	 */
	protected MediaKey() {
		super();
	}

	/**
	 * Creates a new empty identification key instance.
	 */
	public static MediaKey NewInstance(){
		return new MediaKey();
	}


	/**
	 * Returns the set of possible {@link Taxon taxa} corresponding to
	 * <i>this</i> identification key.
	 */
	@Override
    public Set<Taxon> getCoveredTaxa() {
		if(coveredTaxa == null) {
			this.coveredTaxa = new HashSet<>();
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
			this.geographicalScope = new HashSet<>();
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
			this.taxonomicScope = new HashSet<>();
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
	 * Returns the set of {@link Representation key representations} corresponding to
	 * <i>this</i> identification key
	 */
	public Set<Representation> getKeyRepresentations() {
		if(keyRepresentations == null) {
			this.keyRepresentations = new HashSet<>();
		}
		return keyRepresentations;
	}

	/**
	 * Adds a {@link Representation key representation} to the set of {@link #getKeyRepresentations() key representations}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	keyRepresentation	the key representation to be added to <i>this</i> identification key
	 * @see    	   		#getKeyRepresentations()
	 */
	public void addKeyRepresentation(Representation keyRepresentation) {
		this.keyRepresentations.add(keyRepresentation);
	}

	/**
	 * Removes one element from the set of {@link #getKeyRepresentations() key representations}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	keyRepresentation	the key representation which should be removed
	 * @see     		#getKeyRepresentations()
	 * @see     		#addKeyRepresentation(Representation)
	 */
	public void removeKeyRepresentation(Representation keyRepresentation) {
		this.keyRepresentations.remove(keyRepresentation);
	}

	/**
	 * Returns the set of {@link Scope scope restrictions} corresponding to
	 * <i>this</i> identification key
	 */
	@Override
    public Set<DefinedTerm> getScopeRestrictions() {
		if(scopeRestrictions == null) {
			this.scopeRestrictions = new HashSet<>();
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
	 * Clones <i>this</i> MediaKey. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> MediaKey by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.media.Media#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MediaKey clone() {
		MediaKey result;

		try{
			result = (MediaKey)super.clone();

			result.coveredTaxa = new HashSet<>();
			for (Taxon taxon: this.coveredTaxa){
				result.addCoveredTaxon(taxon);
			}

			result.geographicalScope = new HashSet<>();
			for (NamedArea area: this.geographicalScope){
				result.addGeographicalScope(area);
			}

			result.keyRepresentations = new HashSet<>();
			for (Representation rep: this.keyRepresentations) {
				result.addKeyRepresentation(rep);
			}

			result.scopeRestrictions = new HashSet<>();
			for (DefinedTerm scope: this.scopeRestrictions){
				result.addScopeRestriction(scope);
			}

			result.taxonomicScope = new HashSet<>();
			for (Taxon taxon: this.taxonomicScope){
				result.addTaxonomicScope(taxon);
			}

			return result;

		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}