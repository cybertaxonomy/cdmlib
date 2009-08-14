/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LivingBeing", propOrder = {
})
@XmlRootElement(name = "LivingBeing")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
@Configurable
public class LivingBeing extends DerivedUnitBase<IIdentifiableEntityCacheStrategy<LivingBeing>> implements Cloneable {
	private static final Logger logger = Logger.getLogger(LivingBeing.class);

	/**
	 * Factory method
	 * @return
	 */
	public static LivingBeing NewInstance(){
		return new LivingBeing();
	}
	
	/**
	 * Constructor
	 */
	protected LivingBeing() {
		super();
		this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<LivingBeing>();
	}

//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> living beeing. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> living beeing
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link DerivedUnitBase DerivedUnitBase}.
	 * 
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public LivingBeing clone(){
		try{
			LivingBeing result = (LivingBeing)super.clone();
			//no changes to: -
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}