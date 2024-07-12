/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.strategy.cache.occurrence.DnaSampleDefaultCacheStrategy;

/**
 * A DNA Sample is the extracted DNA of a given tissue sample. It may be stored in
 * a DNA Bank and should then be handled as a collection unit.
 * DNA Sample are used to determine their {@link Sequence DNA sequences}
 * starting with a process called {@link Amplification amplification}.
 *
 * @author m.doering
 * @since 08-Nov-2007
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DnaSample", propOrder = {
    "sequences",
    "amplificationResults",
    "dnaQuality"
})
@XmlRootElement(name = "DnaSample")
@Entity
@Audited
public class DnaSample extends DerivedUnit {

	private static final long serialVersionUID = -2978411330023671805L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

// ****************** FACTORY METHOD *****************/

	public static DnaSample NewInstance(){
		return new DnaSample(SpecimenOrObservationType.DnaSample);
	}

    public static DnaSample NewTissueSampleAsDnaSampleInstance(){
        return new DnaSample(SpecimenOrObservationType.TissueSample);
    }

// ************** ATTRIBUTES ****************************/

	@XmlElementWrapper(name = "Sequences")
	@XmlElement(name = "sequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="dnaSample", fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<Sequence> sequences = new HashSet<>();

	@XmlElementWrapper(name = "AmplificationResults")
	@XmlElement(name = "AmplificationResult")
	@OneToMany(mappedBy="dnaSample", fetch = FetchType.LAZY)
	@Cascade( { CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    @NotNull
	private final Set<AmplificationResult> amplificationResults = new HashSet<>();

    @XmlElement(name = "DnaQuality", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    private DnaQuality dnaQuality;

// ******************* CONSTRUCTOR *************************/

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
	DnaSample() {}

	private  DnaSample(SpecimenOrObservationType type) {
        super(type);
    }

    @Override
    protected void initDefaultCacheStrategy() {
        if (this.getRecordBasis() == null || SpecimenOrObservationType.DnaSample.equals(this.getRecordBasis())){
            this.cacheStrategy = new DnaSampleDefaultCacheStrategy();
        }else{
            super.initDefaultCacheStrategy();
        }
    }

//************ GETTER / SETTER  **********************************/

    //sequencings
	public Set<Sequence> getSequences() {
		return sequences;
	}
	public void addSequence(Sequence sequence) {
		if (sequence.getDnaSample() != null){
			sequence.getDnaSample().removeSequence(sequence);
		}
		this.sequences.add(sequence);
		sequence.setDnaSample(this);
	}
	public void removeSequence(Sequence sequence) {
		sequence.setDnaSample(null);
		this.sequences.remove(sequence);
	}

	//amplifications
	public Set<AmplificationResult> getAmplificationResults() {
		return amplificationResults;
	}
	public void addAmplificationResult(AmplificationResult amplificationResult) {
		this.amplificationResults.add(amplificationResult);
		amplificationResult.setDnaSample(this);
	}
	public void removeAmplificationResult(AmplificationResult amplificationResult) {
		this.amplificationResults.remove(amplificationResult);
		amplificationResult.setDnaSample(null);
	}

	public DnaQuality getDnaQuality() {
		return dnaQuality;
	}
	public void setDnaQuality(DnaQuality dnaQuality) {
		this.dnaQuality = dnaQuality;
	}

// ************* Convenience Getter / Setter ************/

	@Transient
	public Collection getStoredAt(){
		return this.getCollection();
	}
	public void setStoredAt(Collection storedAt){
		this.setCollection(storedAt);
	}

	@Transient
	public Set<SpecimenOrObservationBase> getExtractedFrom(){
		return getOriginals();
	}

	@Transient
	public String getBankNumber(){
		return this.getCatalogNumber();
	}
	public void setBankNumber(String bankNumber){
		this.setCatalogNumber(bankNumber);
	}

//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> dna sample. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> dna sample
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link Specimen Specimen}.
	 * @throws CloneNotSupportedException
	 *
	 * @see Specimen#clone()
	 * @see DerivedUnit#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DnaSample clone() {
		DnaSample result = (DnaSample)super.clone();
		//sequenceSet
		result.sequences = new HashSet<>();
		for(Sequence sequence : this.sequences) {
			result.addSequence(sequence.clone());
		}
		//no changes to: bankNumber
		return result;
	}
}