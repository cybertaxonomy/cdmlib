/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import javax.persistence.*;

/**
 * This class represents elementary distribution data for a {@link Taxon taxon}.
 * Only {@link TaxonDescription taxon descriptions} may contain distributions.
 * A distribution instance consist of a {@link NamedArea named area} and of a {@link PresenceAbsenceTermBase status}
 * describing the absence or the presence of a taxon (like "extinct"
 * or "introduced") in this named area.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> CodedDescription according to the the SDD schema
 * <li> Distribution according to the TDWG ontology
 * </ul>
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
public class Distribution extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(Distribution.class);
	
	private NamedArea area;
	private PresenceAbsenceTermBase status;

	
	/**
	 * Class constructor: creates a new empty distribution instance.
	 * The corresponding {@link Feature feature} is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 */
	protected Distribution(){
		super(Feature.DISTRIBUTION());
	}
	
	
	/**
	 * Creates an empty distribution instance. The corresponding {@link Feature feature}
	 * is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 *
	 * @see		#NewInstance(NamedArea, PresenceAbsenceTermBase)
	 */
	public static Distribution NewInstance(){
		Distribution result = new Distribution();
		return result;
	}

	/**
	 * Creates a distribution instance with the given {@link NamedArea named area} and {@link PresenceAbsenceTermBase status}.
	 * The corresponding {@link Feature feature} is set to {@link Feature#DISTRIBUTION() DISTRIBUTION}.
	 *
	 * @param	area	the named area for the new distribution 
	 * @param	status	the presence or absence term for the new distribution
	 * @see				#NewInstance()
	 */
	public static Distribution NewInstance(NamedArea area, PresenceAbsenceTermBase status){
		Distribution result = new Distribution();
		result.setArea(area);
		result.setStatus(status);
		return result;
	}
	
	/** 
	 * Deprecated because {@link Feature feature} should always be {@link Feature#DISTRIBUTION() DISTRIBUTION}
	 * for all distribution instances.
	 */
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#setFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	@Deprecated
	public void setFeature(Feature feature) {
		super.setFeature(feature);
	}
	
	/** 
	 * Returns the {@link NamedArea named area} <i>this</i> distribution applies to.
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public NamedArea getArea(){
		return this.area;
	}
	/** 
	 * @see	#getArea()
	 */
	public void setArea(NamedArea area){
		this.area = area;
	}

	/** 
	 * Returns the {@link PresenceAbsenceTermBase presence or absence term} for <i>this</i> distribution.
	 */
	@ManyToOne
	public PresenceAbsenceTermBase getStatus(){
		return this.status;
	}
	/** 
	 * @see	#getStatus()
	 */
	public void setStatus(PresenceAbsenceTermBase status){
		this.status = status;
	}

}