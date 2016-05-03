/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.hibernate.search.AcceptedTaxonBridge;
import eu.etaxonomy.cdm.hibernate.search.ClassInfoBridge;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.strategy.cache.taxon.ITaxonCacheStrategy;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;
import eu.etaxonomy.cdm.validation.annotation.TaxonNameCannotBeAcceptedAndSynonym;

/**
 * The upmost (abstract) class for the use of a {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} in a {@link eu.etaxonomy.cdm.model.reference.Reference reference}
 * or within a taxonomic view/treatment either as a {@link Taxon taxon}
 * ("accepted" respectively "correct" name) or as a (junior) {@link Synonym synonym}.
 * Within a taxonomic view/treatment or a reference a taxon name can be used
 * only in one of both described meanings. The reference using the taxon name
 * is generally cited with "sec." (secundum, sensu). For instance:
 * "<i>Juncus longirostris</i> Kuvaev sec. Kirschner, J. et al. 2002".
 * <P>
 * This class corresponds to: <ul>
 * <li> TaxonConcept according to the TDWG ontology
 * <li> TaxonConcept according to the TCS
 * </ul>
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonBase", propOrder = {
    "name",
    "sec",
    "doubtful",
    "secMicroReference",
    "appendedPhrase",
    "useNameCache",
    "publish"
})
@Entity
@Audited
//@PreFilter("hasPermission(filterObject, 'edit')")
@Table(appliesTo="TaxonBase", indexes = { @Index(name = "taxonBaseTitleCacheIndex", columnNames = { "titleCache" }) })
@TaxonNameCannotBeAcceptedAndSynonym(groups = Level3.class)
@ClassBridges({
    @ClassBridge(name="classInfo",
            index = org.hibernate.search.annotations.Index.YES,
            store = Store.YES,
            impl = ClassInfoBridge.class),
    @ClassBridge(name="accTaxon", // TODO rename to acceptedTaxon, since we are usually not using abbreviations for field names
            index = org.hibernate.search.annotations.Index.YES,
            store = Store.YES,
            impl = AcceptedTaxonBridge.class),
    @ClassBridge(impl = eu.etaxonomy.cdm.hibernate.search.NomenclaturalSortOrderBrigde.class)
})
public abstract class TaxonBase<S extends ITaxonCacheStrategy> extends IdentifiableEntity<S> implements  IPublishable, Cloneable {
    private static final long serialVersionUID = -3589185949928938529L;
    private static final Logger logger = Logger.getLogger(TaxonBase.class);

    private static Method methodTaxonNameAddTaxonBase;

    static {
        try {
            methodTaxonNameAddTaxonBase = TaxonNameBase.class.getDeclaredMethod("addTaxonBase", TaxonBase.class);
            methodTaxonNameAddTaxonBase.setAccessible(true);
        } catch (Exception e) {
            logger.error(e);
            for(StackTraceElement ste : e.getStackTrace()) {
                logger.error(ste);
            }
        }
    }

    //The assignment to the Taxon or to the Synonym class is not definitive
    @XmlAttribute(name = "isDoubtful")
    private boolean doubtful;


    @XmlElement(name = "Name", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded(includeEmbeddedObjectId=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull(groups = Level2.class)
    private TaxonNameBase<?,?> name;

    // The concept reference
    @XmlElement(name = "Sec")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    @NotNull(groups = Level2.class)
    @IndexedEmbedded
    private Reference<?> sec;

    @XmlElement(name = "secMicroReference")
    @CacheUpdate(noUpdate ="titleCache")
    @NullOrNotEmpty
    @Column(length=255)
    private String secMicroReference;

    @XmlElement(name = "AppendedPhrase")
    private String appendedPhrase;

    @XmlAttribute(name= "UseNameCache")
    private boolean useNameCache = false;

    @XmlAttribute(name = "publish")
    private boolean publish = true;


// ************* CONSTRUCTORS *************/
    /**
     * Class constructor: creates a new empty (abstract) taxon.
     *
     * @see 	#TaxonBase(TaxonNameBase, Reference)
     */
    protected TaxonBase(){
        super();
    }

    /**
     * Class constructor: creates a new (abstract) taxon with the
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it.
     *
     * @param  taxonNameBase	the taxon name used
     * @param  sec				the reference using the taxon name
     * @see    #TaxonBase()
     */
    protected TaxonBase(TaxonNameBase taxonNameBase, Reference sec){
        super();
        if (taxonNameBase != null){
            this.invokeSetMethod(methodTaxonNameAddTaxonBase, taxonNameBase);
        }
        this.setSec(sec);
    }

//********* METHODS **************************************/

    /**
     * Generates and returns the string with the full scientific name (including
     * authorship) of the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used in <i>this</i>
     * (abstract) taxon as well as the title of the {@link eu.etaxonomy.cdm.model.reference.Reference reference} using
     * this taxon name. This string may be stored in the inherited
     * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
     * This method overrides the generic and inherited generateTitle() method
     * from {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity IdentifiableEntity}.
     *
     * @return  the string with the full scientific name of the taxon name
     *			and with the title of the reference involved in <i>this</i> (abstract) taxon
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
     * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
     */
//	@Override
//	public String generateTitle() {
//		String title;
//		if (name != null && name.getTitleCache() != null){
//			title = name.getTitleCache() + " sec. ";
//			if (sec != null){
//				title += sec.getTitleCache();
//			}else{
//				title += "???";
//			}
//		}else{
//			title = this.toString();
//		}
//		return title;
//	}

    @Transient
    public List<TaggedText> getTaggedTitle(){
        return getCacheStrategy().getTaggedTitle(this);
    }



    /**
     * Returns the {@link TaxonNameBase taxon name} used in <i>this</i> (abstract) taxon.
     */
    public TaxonNameBase getName(){
        return this.name;
    }

    public void setName(TaxonNameBase name) {
        if (this.name != null){
            this.name.getTaxonBases().remove(this);
        }
        if(name != null) {
            name.getTaxonBases().add(this);
        }
        this.name = name;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.name.HomotypicalGroup homotypical group} of the
     * {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name} used in <i>this</i> (abstract) taxon.
     */
    @Transient
    public HomotypicalGroup getHomotypicGroup(){
        if (this.getName() == null){
            return null;
        }else{
            return this.getName().getHomotypicalGroup();
        }
    }

    /**
     * Returns the boolean value indicating whether the assignment of <i>this</i>
     * (abstract) taxon to the {@link Taxon Taxon} or to the {@link Synonym Synonym} class
     * is definitive (false) or not (true). If this flag is set the use of <i>this</i> (abstract)
     * taxon as an "accepted/correct" name or as a (junior) "synonym" might
     * still change in the course of taxonomical working process.
     */
    public boolean isDoubtful(){
        return this.doubtful;
    }
    /**
     * @see  #isDoubtful()
     */
    public void setDoubtful(boolean doubtful){
        this.doubtful = doubtful;
    }


    /**
     * Returns the boolean value indicating if this taxon should be withheld (<code>publish=false</code>) or not
     * (<code>publish=true</code>) during any publication process to the general public.
     * This publish flag implementation is preliminary and may be replaced by a more general
     * implementation of READ rights in future.<BR>
     * The default value is <code>true</code>.
     */
    @Override
    public boolean isPublish() {
        return publish;
    }

    @Override
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.reference.Reference reference} of <i>this</i> (abstract) taxon.
     * This is the reference or the treatment using the {@link TaxonNameBase taxon name}
     * in <i>this</i> (abstract) taxon.
     */
    public Reference getSec() {
        return sec;
    }

    /**
     * @see  #getSec()
     */
    public void setSec(Reference sec) {
        this.sec = sec;
    }



    /**
     * An appended phrase is a phrase that is added to the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase taxon name}
     * 's title cache to be used just in this taxon. E.g. the phrase "sensu latu" may be added
     * to the name to describe this taxon more precisely.
     * If {@link #isUseNameCache()}
     * @return the appendedPhrase
     */
    public String getAppendedPhrase() {
        return appendedPhrase;
    }

    /**
     * @param appendedPhrase the appendedPhrase to set
     */
    public void setAppendedPhrase(String appendedPhrase) {
        this.appendedPhrase = appendedPhrase;
    }

    /**
     * @return the useNameCache
     */
    public boolean isUseNameCache() {
        return useNameCache;
    }

    /**
     * @param useNameCache the useNameCache to set
     */
    public void setUseNameCache(boolean useNameCache) {
        this.useNameCache = useNameCache;
    }

    @Transient
    public abstract boolean isOrphaned();


    /**
     * @return
     */
    @Transient
    public Rank getNullSafeRank() {
        return name == null ? null : name.getRank();
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> taxon. This is a shortcut that enables to create
     * a new instance with empty taxon name and sec reference.
     *
     * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        TaxonBase result;
        try {
            result = (TaxonBase)super.clone();
            result.setSec(null);

            return result;
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }


    }


}