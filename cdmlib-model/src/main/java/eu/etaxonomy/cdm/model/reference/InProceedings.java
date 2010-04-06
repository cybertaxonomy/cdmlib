/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceBaseDefaultCacheStrategy;

/**
 * This class represents isolated parts (usually papers or abstracts) within
 * {@link Proceedings conference proceedings}.
 * <P>
 * This class corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "SubReference".
 *   
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InProceedings", propOrder = {
//    "inProceedings"
})
@XmlRootElement(name = "InProceedings")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Configurable
@Deprecated
public class InProceedings extends SectionBase<IReferenceBaseCacheStrategy<InProceedings>> {
	private static final long serialVersionUID = -286946099144494551L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InProceedings.class);
	
//	@XmlElement(name = "InProceedings")
//	@XmlIDREF
//	@XmlSchemaType(name = "InProceedings")
//	@ManyToOne(fetch = FetchType.LAZY)
//	@IndexedEmbedded
//	@Cascade(CascadeType.SAVE_UPDATE)
//	private Proceedings inProceedings;


	/** 
	 * Creates a new empty "in proceedings" instance.
	 * 
	 * @see #NewInstance(Proceedings)
	 */
	public static InProceedings NewInstance(){
		InProceedings result = new InProceedings();
		return result;
	}
	
	/** 
	 * Creates a new "in proceedings" instance with the given proceedings it belongs to.
	 * 
	 * @param	inProceedings	the proceedings <i>this</i> "in proceedings" is part of
	 * @see 					#NewInstance()
	 * @see 					Proceedings
	 */
	public static InProceedings NewInstance(Proceedings inProceedings){
		InProceedings result = NewInstance();
		result.setInProceedings(inProceedings);
		return result;
	}
	
	
	protected InProceedings() {
		super();
		this.type = ReferenceType.InProceedings;
		this.cacheStrategy = new ReferenceBaseDefaultCacheStrategy<InProceedings>();
	}
	
	
	/**
	 * Returns the {@link Proceedings proceedings} <i>this</i> "in proceedings" (usually 
	 * a paper or an abstract) is part of.
	 * 
	 * @return  the proceedings in which <i>this</i> "in proceedings" has been
	 * 			published
	 * @see 	Proceedings
	 */
	@Transient
	public Proceedings getInProceedings(){
		if (inReference == null){
			return null;
		}
		if (! this.inReference.isInstanceOf(Proceedings.class)){
			throw new IllegalStateException("The in-reference of an inproceeding may only be of type Proceedings");
		}
		return CdmBase.deproxy(this.inReference,Proceedings.class);
	}

	/**
	 * @see #getInProceedings()
	 */
	public void setInProceedings(Proceedings inProceedings){
		this.inReference = inProceedings;
	}

}