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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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
import org.hibernate.search.annotations.FieldBridge;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IHasCredits;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.IHasLink;
import eu.etaxonomy.cdm.model.media.IHasRights;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermType;
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
 * @since 08-Nov-2007 13:06:20
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonDescription", propOrder = {
    "scopes",
    "geoScopes",
    "taxon"
})
@XmlRootElement(name = "TaxonDescription")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionBase")
@Audited
@Configurable
public class TaxonDescription
            extends DescriptionBase<IIdentifiableEntityCacheStrategy<TaxonDescription>>
            implements IHasRights, IHasCredits, IHasLink {

    private static final long serialVersionUID = 8065879180505546803L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElementWrapper(name = "Scopes")
    @XmlElement(name = "Scope")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DescriptionBase_Scope")
    private Set<DefinedTerm> scopes = new HashSet<>();

    @XmlElementWrapper( name = "GeoScopes")
    @XmlElement( name = "GeoScope")
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="DescriptionBase_GeoScope")
    private Set<NamedArea> geoScopes = new HashSet<>();

    @XmlElement( name = "Taxon")
    @ManyToOne(fetch = FetchType.LAZY)
    @XmlIDREF
    @XmlSchemaType(name="IDREF")
    @FieldBridge(impl=NotNullAwareIdBridge.class)
    private Taxon taxon;

//*********************** FACTORY *********************************/

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
        if (taxon != null){
            taxon.addDescription(description);
        }
        return description;
    }

    /**
     * Creates a new taxon description instance for the given {@link Taxon taxon}.
     * The new taxon description will be also added to the {@link Taxon#getDescriptions() set of descriptions}
     * assigned to the given taxon.
     *
     * @see	#NewInstance()
     */
    public static TaxonDescription NewInstance(Taxon taxon, boolean isImageGallery){
        TaxonDescription description = new TaxonDescription();
        taxon.addDescription(description);
        description.setImageGallery(isImageGallery);
        return description;
    }

//******************** CONSTRUCTOR *************************************************/

    /**
     * Class constructor: creates a new empty taxon description instance.
     */
    public TaxonDescription(){
        super();
    }

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = new TaxonDescriptionDefaultCacheStrategy();
    }

//************************** METHODS **********************************************/


    public Taxon getTaxon() {
        return taxon;
    }


    protected void setTaxon(Taxon taxon) {
    	//TODO needs correct bidirectional handling before making it public
    	this.taxon = taxon;
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
     * Returns the set of {@link Scope scopes} (this covers mostly terms for {@link TermType#Stage life stage}
     * or {@link TermType#Sex sex} or both)
     * restricting the validity of <i>this</i> taxon description. This set
     * of scopes should contain no more than one "sex" and one "life stage".
     */
    public Set<DefinedTerm> getScopes(){
        return this.scopes;
    }

    /**
     * Adds a {@link Scope scope} (mostly a <code>life stage</code> or <code>sex</code> term)
     * to the set of {@link #getScopes() scopes} restricting the validity of
     * <i>this</i> taxon description.
     *
     * @param scope	the scope to be added to <i>this</i> taxon description
     * @see    	   	#getScopes()
     */
    public void addScope(DefinedTerm scope){
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
    public void removeScope(DefinedTerm scope){
        this.scopes.remove(scope);
    }

    /**
     * Returns the first TextData element of feature type image. If no such element exists,
     * a new one is created.
     */
    @Transient
    public TextData getOrCreateImageTextData(){
        for (DescriptionElementBase element : this.getElements()){
            if (element.getFeature().equals(Feature.IMAGE())){
                if (element.isInstanceOf(TextData.class)){
                    return CdmBase.deproxy(element, TextData.class);
                }
            }
        }
        TextData textData = TextData.NewInstance(Feature.IMAGE());
        addElement(textData);
        return textData;
    }

    @Override
    public IDescribable<?> describedEntity(){
        return this.taxon;
    }


//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> taxon description. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> taxon description by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public TaxonDescription clone() {
        TaxonDescription result = (TaxonDescription)super.clone();

        //scopes
        result.scopes = new HashSet<>();
        for (DefinedTerm scope : getScopes()){
            result.scopes.add(scope);
        }

        //geo-scopes
        result.geoScopes = new HashSet<>();
        for (NamedArea namedArea : getGeoScopes()){
            result.geoScopes.add(namedArea);
        }

        //no changes to: taxon
        return result;
    }
}
