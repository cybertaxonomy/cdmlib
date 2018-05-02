/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * The class representing the assignation of a nomenclatural status to a
 * {@link TaxonName taxon name}. This includes a {@link NomenclaturalStatusType nomenclatural status type}
 * (for instance "invalid", "novum" or "conserved") and eventually the article
 * of the corresponding {@link NomenclaturalCode nomenclatural code} this status assignation is based on.
 * One nomenclatural status can be assigned to several taxon names.
 *
 * @author m.doering
 * @version 1.0
 * @since 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NomenclaturalStatus", propOrder = {
    "ruleConsidered",
    "type"
})
@Entity
@Audited
public class NomenclaturalStatus extends ReferencedEntityBase implements Cloneable{
	private static final long serialVersionUID = -2451270405173131900L;
	static Logger logger = Logger.getLogger(NomenclaturalStatus.class);

	//The nomenclatural code rule considered. The article/note/recommendation in the code in question that is commented on in
	//the note property.
	@XmlElement(name = "RuleConsidered")
	private String ruleConsidered;

	@XmlElement(name = "NomenclaturalStatusType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
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
		return NewInstance(nomStatusType, null, null);
	}


	/**
	 * Creates a new nomenclatural status instance with a given
	 * {@link NomenclaturalStatusType nomenclatural status type}.
	 *
	 * @see #NomenclaturalStatus()
	 */
	public static NomenclaturalStatus NewInstance(NomenclaturalStatusType nomStatusType, Reference citation, String microCitation){
		NomenclaturalStatus status = new NomenclaturalStatus();
		status.setType(nomStatusType);
		status.setCitation(citation);
		status.setCitationMicroReference(microCitation);
		return status;
	}


	/**
	 * Returns the {@link NomenclaturalStatusType nomenclatural status type} of <i>this</i>
	 * nomenclatural status.
	 */
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
	 * the {@link TaxonName#getNomenclaturalCode() taxon name(s)}) of <i>this</i>
	 * nomenclatural status. The considered rule gives the reason why the
	 * {@link NomenclaturalStatusType nomenclatural status type} has been
	 * assigned to the {@link TaxonName taxon name(s)}.
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


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> nomenclatural status. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> nomenclatural status by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.common.ReferencedEntityBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			NomenclaturalStatus result = (NomenclaturalStatus)super.clone();
			//no changes to: ruleConsidered, type
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}
