/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing the assignation of a nomenclatural status to a 
 * {@link TaxonNameBase taxon name}. This includes a {@link NomenclaturalStatusType nomenclatural status type}
 * (for instance "invalid", "novum" or "conserved") and eventually the article
 * of the corresponding {@link NomenclaturalCode nomenclatural code} this status assignation is based on.
 * One nomenclatural status can be assigned to several taxon names.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NomenclaturalStatus", propOrder = {
    "ruleConsidered",
    "type"
})
@Entity
public class NomenclaturalStatus extends ReferencedEntityBase {
	
	static Logger logger = Logger.getLogger(NomenclaturalStatus.class);
	
	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@XmlElement(name = "ruleConsidered")
	private String ruleConsidered;
	
	@XmlElement(name = "NomenclaturalStatusType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private NomenclaturalStatusType type;

	/** 
	 * Class constructor: creates a new empty nomenclatural status instance.
	 */
	protected NomenclaturalStatus() {
		super();
	}

	/** 
	 * Creates a new nomenclatural status instance with a given
	 * {@link NomenclaturalStatusType nomenclatural status type}.
	 * 
	 * @see #NomenclaturalStatus()
	 */
	public static NomenclaturalStatus NewInstance(NomenclaturalStatusType nomStatusType){
		NomenclaturalStatus status = new NomenclaturalStatus();
		status.setType(nomStatusType);
		return status;
	}
	

	/** 
	 * Returns the {@link NomenclaturalStatusType nomenclatural status type} of <i>this</i>
	 * nomenclatural status.
	 */
	@ManyToOne
	public NomenclaturalStatusType getType(){
		return this.type;
	}

	/**
	 * @see  #getType()
	 */
	public void setType(NomenclaturalStatusType type){
		this.type = type;
	}

	/** 
	 * Returns the nomenclatural code rule considered (that is the
	 * article/note/recommendation in the nomenclatural code ruling
	 * the {@link TaxonNameBase#getNomenclaturalCode() taxon name(s)}) of <i>this</i>
	 * nomenclatural status. The considered rule gives the reason why the
	 * {@link NomenclaturalStatusType nomenclatural status type} has been
	 * assigned to the {@link TaxonNameBase taxon name(s)}.
	 */
	public String getRuleConsidered(){
		return this.ruleConsidered;
	}

	/**
	 * @see  #getRuleConsidered()
	 */
	public void setRuleConsidered(String ruleConsidered){
		this.ruleConsidered = ruleConsidered;
	}

}