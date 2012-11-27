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
import javax.persistence.OneToMany;
import javax.persistence.Transient;
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

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DnaSample", propOrder = {
    "sequences"
})
@XmlRootElement(name = "DnaSample")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
public class DnaSample extends Specimen implements Cloneable {
	private static final long serialVersionUID = -2978411330023671805L;
	private static final Logger logger = Logger.getLogger(DnaSample.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static DnaSample NewInstance(){
		return new DnaSample();
	}

	
//	@XmlElement(name = "BankNumber")
//	private String bankNumber;
	
	@XmlElementWrapper(name = "Sequences")
	@XmlElement(name = "sequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch = FetchType.LAZY)
	private Set<Sequence> sequences = new HashSet<Sequence>();

	
	
	/**
	 * Constructor
	 */
	private DnaSample() {
		super();
		this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<Specimen>();
	}
	
	public Set<Sequence> getSequences() {
		return sequences;
	}

	// FIXME shouldn't this be the singular? i.e. addSequence( . . . )
	public void addSequences(Sequence sequence) {
		this.sequences.add(sequence);
	}

	// FIXME shouldn't this be the singular? i.e. removeSequence( . . . )
	public void removeSequences(Sequence sequence) {
		this.sequences.remove(sequence);
	}

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
	 * 
	 * @see Specimen#clone()
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DnaSample clone(){
		DnaSample result = (DnaSample)super.clone();
		//sequenceSet
		result.sequences = new HashSet<Sequence>();
		for(Sequence sequence : this.sequences) {
			result.addSequences(sequence);
		}
		//no changes to: bankNumber
		return result;
	}
}