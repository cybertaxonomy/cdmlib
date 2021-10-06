/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * Abstract base class for classes implementing {@link eu.etaxonomy.cdm.model.reference.IOriginalSource IOriginalSource}.
 * @see eu.etaxonomy.cdm.model.reference.IOriginalSource
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:22
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalSource", propOrder = {
    "type",
	"idInSource",
    "idNamespace",
    "originalNameString",
    "citation",
    "citationMicroReference",
    "cdmSource",
    "links"
})
@XmlRootElement(name = "OriginalSource")
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="OriginalSourceBase")
public abstract class OriginalSourceBase
        extends AnnotatableEntity
        implements IOriginalSource, IIntextReferenceTarget {

	private static final long serialVersionUID = -1972959999261181462L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OriginalSourceBase.class);

	/**
	 * The {@link OriginalSourceType type} of this source. According to PROV the type has to be thought as
	 * an activity that leads from the source entity to the current entity. It is not a property of the
	 * source itself.
	 */
	@XmlAttribute(name ="type")
	@Column(name="sourceType")
	@NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
    	parameters = {@org.hibernate.annotations.Parameter(name="enumClass", value="eu.etaxonomy.cdm.model.reference.OriginalSourceType")}
    )
	@Audited
	private OriginalSourceType type;

	//The object's ID in the source, where the alternative string comes from
	@XmlElement(name = "IdInSource")
	private String idInSource;

	@XmlElement(name = "IdNamespace")
	private String idNamespace;

    @XmlElement(name = "Citation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Reference citation;

    //Details of the reference. These are mostly (implicitly) pages but can also be tables or any other element of a
    //publication. {if the citationMicroReference exists then there must be also a reference}
    @XmlElement(name = "CitationMicroReference")
    private String citationMicroReference;

    @XmlElement(name = "OriginalNameString")
    private String originalNameString;

    @XmlElement(name = "CdmSource")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.EAGER, orphanRemoval=true)  //EAGER to avoid LIEs cdmSource should always be part of the OriginalSource itself
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    private CdmLinkSource cdmSource;

    @XmlElementWrapper(name = "Links", nillable = true)
    @XmlElement(name = "Link")
    @OneToMany(fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
	private Set<ExternalLink> links = new HashSet<>();

//***************** CONSTRUCTOR ***********************/

	//for hibernate use only
	protected OriginalSourceBase() {
	}

	/**
	 * Constructor
	 * @param type
	 */
	protected OriginalSourceBase(OriginalSourceType type){
		if (type == null){
			throw new IllegalArgumentException("OriginalSourceType must not be null");
		}
		this.type = type;
	}

//**************** GETTER / SETTER *******************************/

    @Override
    public OriginalSourceType getType() {
        return type;
    }
    @Override
    public void setType(OriginalSourceType type) {
        Assert.notNull(type, "OriginalSourceType must not be null");
        this.type = type;
    }

	@Override
	public String getIdInSource(){
		return this.idInSource;
	}
	@Override
	public void setIdInSource(String idInSource){
		this.idInSource = idInSource;
	}

	@Override
	public String getIdNamespace() {
		return idNamespace;
	}
	@Override
	public void setIdNamespace(String idNamespace) {
		this.idNamespace = idNamespace;
	}

    @Override
    public Reference getCitation(){
        return this.citation;
    }
    @Override
    public void setCitation(Reference citation) {
        this.citation = citation;
    }

    @Override
    public String getCitationMicroReference(){
        return this.citationMicroReference;
    }
    @Override
    public void setCitationMicroReference(String citationMicroReference){
        this.citationMicroReference = citationMicroReference;
    }

    public String getOriginalNameString(){
        return this.originalNameString;
    }
    public void setOriginalNameString(String originalNameString){
        this.originalNameString = originalNameString;
    }

	@Override
    public ICdmTarget getCdmSource() {
        return cdmSource == null? null: cdmSource.getTarget();
    }
     /* this method was implemented in the context of the CdmLinkSourceBeanProcessor which is unused
      *  method is preserved for the time when the REST API will be revised (#8637)
    @Override
    public CdmLinkSource getCdmSource() {
        if(cdmSource != null){
            logger.error("NOT NULL");
        }
        return cdmSource;
    }
	*/


//	@Override
//    public void setCdmSource(CdmLinkSource cdmSource) {
//        this.cdmSource = cdmSource;
//    }
    @Override
    public void setCdmSource(ICdmTarget cdmTarget){
        if (cdmTarget != null){
            this.cdmSource = CdmLinkSource.NewInstance(cdmTarget);
        }else{
            this.cdmSource = null;
        }
    }

//********************** External Links **********************************************

    public Set<ExternalLink> getLinks(){
        return this.links;
    }
    public void setLinks(Set<ExternalLink> links){
        this.links = links;
    }
    public void addLink(ExternalLink link){
        if (link != null){
            links.add(link);
        }
    }
    public void removeLink(ExternalLink link){
        if(links.contains(link)) {
            links.remove(link);
        }
    }

//********************** CLONE ************************************************/

	@Override
	public OriginalSourceBase clone() throws CloneNotSupportedException{

        OriginalSourceBase result = (OriginalSourceBase)super.clone();

		Set<ExternalLink> links = new HashSet<>();
		result.setLinks(links);
		for(ExternalLink link : this.links){
		    result.addLink(link.clone());
		}

		if (this.cdmSource != null){
		    result.setCdmSource(this.cdmSource.getTarget());
		}

		//no changes to: type, idInSource, idNamespace,
		//   citation, citationMicroReference, originalNameString
		return result;
	}

// **************** EMPTY ************************/

    @Override
    protected boolean checkEmpty(){
        return checkEmpty(false);
    }

    /**
     * Checks if the source is completely empty.
     *
     * @param excludeType if <code>true</code> the source type
     * is ignored for the check.
     *
     * @see #checkEmpty()
     * @return <code>true</code> if empty
     */
    public boolean checkEmpty(boolean excludeType){
	   return super.checkEmpty()
	        && (excludeType || this.type == null)
	        && this.getCitation() == null
	        && isBlank(this.getCitationMicroReference())
	        && isBlank(this.getOriginalNameString())
	        && isBlank(this.getIdInSource())
	        && isBlank(this.getIdNamespace())
	        && this.links.isEmpty()
	        && this.cdmSource == null
           ;
	}

//************************ toString ***************************************/
	@Override
	public String toString(){
		if (isNotBlank(idInSource) || isNotBlank(idNamespace) ){
			return "OriginalSource:" + CdmUtils.concat(":", idNamespace, idInSource);
		}else{
			return super.toString();
		}
	}

//*********************************** EQUALS *********************************************************/

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * Uses a content based compare strategy which avoids bean initialization. This is achieved by
     * comparing the cdm entity ids.
     */
    public boolean equalsByShallowCompare(OriginalSourceBase other) {

        int thisCitationId = -1;
        int otherCitationId = -1;
        if(this.getCitation() != null) {
            thisCitationId = this.getCitation().getId();
        }
        if(other.getCitation() != null) {
            otherCitationId = other.getCitation().getId();
        }

        if(thisCitationId != otherCitationId
                || !StringUtils.equals(this.getCitationMicroReference(), other.getCitationMicroReference())
                || !StringUtils.equals(this.getOriginalNameString(), other.getOriginalNameString())
                        ){
            return false;
        }

        OriginalSourceBase theOther = other;
        if(!StringUtils.equals(this.getIdInSource(), theOther.getIdInSource())
                || !CdmUtils.nullSafeEqual(this.getIdNamespace(), theOther.getIdNamespace())
                || !CdmUtils.nullSafeEqual(this.getType(), theOther.getType())
                || !CdmUtils.nullSafeEqual(this.getCdmSource(), theOther.getCdmSource())
                || !CdmUtils.nullSafeEqual(this.getLinks(), theOther.getLinks())) {
            return false;
        }

        return true;
    }
}