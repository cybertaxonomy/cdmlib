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
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.description.TaxonDescriptionDefaultCacheStrategy;

/**
 * This class represents descriptions that delimit or circumscribe a real taxon.
 * <P>
 * This class corresponds to: <ul>
 * <li> DescriptionsBaseType with a "Class" element according to the the SDD schema
 * <li> SpeciesProfileModel according to the TDWG ontology
 * <li> CharacterCircumscription according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:20
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonDescription", propOrder = {
    "scopes",
    "geoScopes",
    "taxon"
})
@XmlRootElement(name = "TaxonDescription")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionBase")
@Audited
@Configurable
public class TaxonDescription extends DescriptionBase<IIdentifiableEntityCacheStrategy<TaxonDescription>> {
	private static final long serialVersionUID = 8065879180505546803L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonDescription.class);

	@XmlElementWrapper(name = "Scopes")
	@XmlElement(name = "Scope")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="DescriptionBase_Scope")
	private Set<Scope> scopes = new HashSet<Scope>();
	
	@XmlElementWrapper( name = "GeoScopes")
	@XmlElement( name = "GeoScope")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="DescriptionBase_GeoScope")
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<NamedArea> geoScopes = new HashSet<NamedArea>();
	
	@XmlElement( name = "Taxon")
	@ManyToOne(fetch = FetchType.LAZY)
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@JoinColumn(name="taxon_fk")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Taxon taxon;

	public Taxon getTaxon() {
		return taxon;
	}

	/**
	 * Class constructor: creates a new empty taxon description instance.
	 */
	public TaxonDescription(){
		super();
		this.cacheStrategy = new TaxonDescriptionDefaultCacheStrategy();
		}
	
	/**
	 * Creates a new empty taxon description instance.
	 * 
	 * @see	#NewInstance(Taxon)
	 */
	public static TaxonDescription NewInstance(){
		return new TaxonDescription();
	}
	
	/**
	 * Creates a new taxon description instance for the given {@link Taxon taxon}.
	 * The new taxon description will be also added to the {@link Taxon#getDescriptions() set of descriptions}
	 * assigned to the given taxon.
	 * 
	 * @see	#NewInstance()
	 */
	public static TaxonDescription NewInstance(Taxon taxon){
		TaxonDescription description = new TaxonDescription();
		taxon.addDescription(description);
		return description;
	}
	
	/** 
	 * Returns the set of {@link NamedArea named areas} indicating the geospatial
	 * data where <i>this</i> taxon description is valid.
	 */
	public Set<NamedArea> getGeoScopes(){
		return this.geoScopes;
	}

	/**
	 * Adds a {@link NamedArea named area} to the set of {@link #getGeoScopes() named areas}
	 * delimiting the geospatial area where <i>this</i> taxon description is valid.
	 * 
	 * @param geoScope	the named area to be additionally assigned to <i>this</i> taxon description
	 * @see    	   		#getGeoScopes()
	 */
	public void addGeoScope(NamedArea geoScope){
		this.geoScopes.add(geoScope);
	}
	
	/** 
	 * Removes one element from the set of {@link #getGeoScopes() named areas} delimiting
	 * the geospatial area where <i>this</i> taxon description is valid.
	 *
	 * @param  geoScope   the named area which should be removed
	 * @see     		  #getGeoScopes()
	 * @see     		  #addGeoScope(NamedArea)
	 */
	public void removeGeoScope(NamedArea geoScope){
		this.geoScopes.remove(geoScope);
	}

	
	/** 
	 * Returns the set of {@link Scope scopes} (this covers mostly {@link Stage life stage} or {@link Sex sex} or both)
	 * restricting the validity of <i>this</i> taxon description. This set
	 * of scopes should contain no more than one "sex" and one "life stage".
	 */
	public Set<Scope> getScopes(){
		return this.scopes;
	}

	/**
	 * Adds a {@link Scope scope} (mostly a {@link Stage life stage} or a {@link Sex sex})
	 * to the set of {@link #getScopes() scopes} restricting the validity of
	 * <i>this</i> taxon description.
	 * 
	 * @param scope	the scope to be added to <i>this</i> taxon description
	 * @see    	   	#getScopes()
	 */
	public void addScope(Scope scope){
		this.scopes.add(scope);
	}
	
	/** 
	 * Removes one element from the set of {@link #getScopes() scopes}
	 * restricting the validity of <i>this</i> taxon description.
	 *
	 * @param  scope	the scope which should be removed
	 * @see     		#getScopes()
	 * @see     		#addScope(Scope)
	 */
	public void removeScope(Scope scope){
		this.scopes.remove(scope);
	}
}