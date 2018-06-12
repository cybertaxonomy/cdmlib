/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.ITaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.taxon.ITaxonCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.HomotypicSynonymsShouldBelongToGroup;

/**
 * The class for synonyms: these are {@link TaxonBase taxa} the {@link name.TaxonName taxon names}
 * of which are not used by the {@link TaxonBase#getSec() reference} to designate a real
 * taxon but are mentioned as taxon names that were oder are used by some other
 * unspecified references to designate (at least to some extent) the same
 * particular real taxon. Synonyms that are {@link #getAcceptedTaxon() attached} to an accepted {@link Taxon taxon}
 * are actually meaningless.<BR>
 * Splitting taxa in "accepted/valid" and "synonyms"
 * makes it easier to handle particular relationships between
 * ("accepted/valid") {@link Taxon taxa} on the one hand and ("synonym") taxa
 *  on the other.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:55
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Synonym", propOrder = {
    "acceptedTaxon",
    "type",
})
@XmlRootElement(name = "Synonym")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonBase")
@Audited
@Configurable
@HomotypicSynonymsShouldBelongToGroup(groups = Level3.class)
public class Synonym extends TaxonBase<ITaxonCacheStrategy<Synonym>> {
    private static final long serialVersionUID = 6977221584815363620L;


    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Synonym.class);


    @XmlElement(name = "acceptedTaxon")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @ContainedIn
//  @NotEmpty(groups = Level2.class,message="{eu.etaxonomy.cdm.model.taxon.Synonym.noOrphanedSynonyms.message}")
//    @NotNull(groups = Level2.class)
    private Taxon acceptedTaxon;

    @XmlElement(name = "Type")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.EAGER)
    private SynonymType type;

//************************************* FACTORY ****************************/
    /**
     * @see #NewInstance(TaxonName, Reference)
     * @param taxonName
     * @param sec
     * @return
     */
    public static Synonym NewInstance(ITaxonNameBase taxonName, Reference sec){
        return NewInstance(TaxonName.castAndDeproxy(taxonName), sec);
    }

    /**
     * Creates a new synonym instance with
     * the {@link eu.etaxonomy.cdm.model.name.TaxonName taxon name} used and the {@link eu.etaxonomy.cdm.model.reference.Reference reference}
     * using it as a synonym and not as an ("accepted/correct") {@link Taxon taxon}.
     *
     * @param  TaxonName    the taxon name used
     * @param  sec              the reference using the taxon name
     * @see                     #Synonym(TaxonName, Reference)
     */
    public static Synonym NewInstance(TaxonName taxonName, Reference sec){
        Synonym result = new Synonym(taxonName, sec, null);
        return result;
    }

    public static Synonym NewInstance(TaxonName taxonName, Reference sec, String secDetail){
        Synonym result = new Synonym(taxonName, sec, secDetail);
        return result;
    }

// ************* CONSTRUCTORS *************/
	/**
	 * Class constructor: creates a new empty synonym instance.
	 *
	 * @see 	#Synonym(TaxonName, Reference)
	 */
	//TODO should be private, but still produces Spring init errors
	public Synonym(){
		this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<>();
	}

	private Synonym(TaxonName taxonName, Reference sec, String secDetail){
		super(taxonName, sec, secDetail);
		this.cacheStrategy = new TaxonBaseDefaultCacheStrategy<>();
	}

//********************** GETTER/SETTER ******************************/

	/**
	 * Returns the "accepted/valid" {@link Taxon taxon}
	 *
	 */
	public Taxon getAcceptedTaxon() {
		return this.acceptedTaxon;
	}


    /**
     * @param acceptedTaxon the acceptedTaxon to set
     */
    protected void setAcceptedTaxon(Taxon acceptedTaxon) {
        if (acceptedTaxon == null){
            Taxon oldTaxon = this.acceptedTaxon;
            this.acceptedTaxon = null;
            oldTaxon.removeSynonym(this);
        }else{
            if (this.acceptedTaxon != null){
                this.acceptedTaxon.removeSynonym(this, false);
            }
            this.acceptedTaxon = acceptedTaxon;
            this.acceptedTaxon.addSynonym(this);
            checkHomotypic();
        }
    }

    public SynonymType getType() {
        return type;
    }

    public void setType(SynonymType type) {
        this.type = type;
        checkHomotypic();
    }


//***************** METHODS **************************/
	/**
	 * Returns true if <i>this</i> is a synonym of the given taxon.
	 *
	 * @param taxon	the taxon to check synonym for
	 * @return	true if <i>this</i> is a synonm of the given taxon
	 *
	 * @see #getAcceptedTaxon()
	 */
	@Transient
	public boolean isSynonymOf(Taxon taxon){
		return taxon != null && taxon.equals(this.acceptedTaxon);
	}

	@Override
    @Transient
	public boolean isOrphaned() {
	    return this.acceptedTaxon == null || this.acceptedTaxon.isOrphaned();
	}

    /**
     * Checks if the synonym type is homotypic. If it is
     * the name of <code>this</code> synonym is added to the {@link HomotypicalGroup
     * homotypic group} of the {@link Taxon accepted taxon}.
     */
    private void checkHomotypic() {
        if (type != null && type.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())
                && acceptedTaxon != null && acceptedTaxon.getName() != null){
                acceptedTaxon.getName().getHomotypicalGroup().addTypifiedName(this.getName());
        }
    }

//*********************** CLONE ********************************************************/

	@Override
	public Object clone() {
		Synonym result;
		result = (Synonym)super.clone();

		//no changes to accepted taxon, type, partial, proParte

		return result;

	}


}
