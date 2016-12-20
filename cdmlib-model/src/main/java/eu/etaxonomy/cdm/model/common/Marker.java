/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.validation.Level2;

/**
 * This class aims to make available some "flags" for identifiable entities in a
 * flexible way. Application developers (and even users) can define their own
 * "flags" as a MarkerType.
 * @author m.doering
 * @created 08-Nov-2007 13:06:33
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Marker")
@Entity
@Audited
public class Marker extends VersionableEntity implements Cloneable{
	private static final long serialVersionUID = -7474489691871404610L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Marker.class);

    @XmlElement(name = "Flag")
	private boolean flag;

    @XmlElement(name = "MarkerType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(groups=Level2.class)   //removed from Level1 for now, see #4588
	private MarkerType markerType;

// *********************** FACTORY *****************************/

	/**
	 * Factory method
	 * @return
	 */
	public static Marker NewInstance(){
		return new Marker();
	}

	/**
	 * Factory method
	 * @param markerType The type of the marker
	 * @param flag The value of the marker
	 * @return
	 */
	public static Marker NewInstance(MarkerType markerType, boolean flag){
		return new Marker(markerType, flag);
	}

	public static Marker NewInstance(AnnotatableEntity markedObject, boolean flag, MarkerType markerType){
		Marker marker = new Marker();
		marker.setFlag(flag);
		marker.setMarkerType(markerType);
		markedObject.addMarker(marker);
		return marker;
	}

//************************** CONSTRUCTOR **********************************/

	/**
	 * Default Constructor
	 */
	private Marker() {
	}

	/**
	 * Constructor
	 * @param flag
	 */
	protected Marker(MarkerType markerType, boolean flag){
		this.markerType = markerType;
		this.flag = flag;
	}

// ****************************** GETTER / SETTER ***************************/

	/**
	 * @return
	 */
	public MarkerType getMarkerType(){
		return this.markerType;
	}
	public void setMarkerType(MarkerType type){
		this.markerType = type;
	}

	/**
	 * The flag value.
	 * @return
	 */
	public boolean getFlag(){
		return this.flag;
	}
	public void setFlag(boolean flag){
		this.flag = flag;
	}

	/**
	 * @see getFlag()
	 * @return
	 */
	@Transient
	public boolean getValue(){
		return getFlag();
	}


//****************** CLONE ************************************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		Marker result = (Marker)super.clone();
		result.setFlag(this.flag);
		result.setMarkerType(this.markerType);
		return result;
	}

}
