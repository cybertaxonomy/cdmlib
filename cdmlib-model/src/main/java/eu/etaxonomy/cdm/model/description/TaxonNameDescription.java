/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * This class represents all piece of information (not ruled by a {@link NomenclaturalCode nomenclatural code})
 * concerning a {@link TaxonNameBase taxon name} like for instance the content of its first
 * publication (protolog) or a picture of this publication.
 *  
 * @author a.mueller
 * @version 1.0
 * @created 08-Jul-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNameDescription", propOrder = {
})
@XmlRootElement(name = "TaxonNameDescription")
@Entity
public class TaxonNameDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(TaxonNameDescription.class);
	
	@XmlTransient
	private TaxonNameBase taxonName;


	/**
	 * Class constructor: creates a new empty taxon name description instance.
	 */
	public TaxonNameDescription() {
		super();
	}
	
	
	/**
	 * Creates a new empty taxon name description instance.
	 */
	public static TaxonNameDescription NewInstance(){
		return new TaxonNameDescription();
	}
	
	/** 
	 * Returns the {@link TaxonNameBase taxon name} to which <i>this</i> taxon name description
	 * provides additional information not ruled by a {@link NomenclaturalCode nomenclatural code}.
	 */
	@ManyToOne
	@JoinColumn(name="taxonName_fk")
	@Cascade(CascadeType.SAVE_UPDATE)
	public TaxonNameBase getTaxonName() {
		return taxonName;
	}
	@Deprecated //for hibernate use only, use taxonName.addDescription() instead
	protected void setTaxonName(TaxonNameBase taxonName) {
		this.taxonName = taxonName;
	}


}
