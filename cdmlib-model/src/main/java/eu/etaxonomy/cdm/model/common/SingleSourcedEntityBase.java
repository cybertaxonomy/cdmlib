/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Abstract class for all objects that may have a reference
 * @author m.doering
 * @since 08-Nov-2007 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleSourcedEntityBase", propOrder = {
    "source"
})
@XmlRootElement(name = "SingleSourcedEntityBase")
@MappedSuperclass
@Audited
public abstract class SingleSourcedEntityBase
        extends AnnotatableEntity {

    static final long serialVersionUID = 2035568689268762760L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleSourcedEntityBase.class);

    //the source for this single sourced entity
    @XmlElement(name = "source")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private NamedSource source;

// ************ CONSTRUCTOR ********************************************/

	//for hibernate use only
    protected SingleSourcedEntityBase() {
		super();
	}

    protected SingleSourcedEntityBase(Reference reference, String microReference,
            String originalNameString) {
        super();
        this.setCitation(reference);
        this.setCitationMicroReference(microReference);
        if (StringUtils.isNotEmpty(originalNameString)){
            getSource(true).setOriginalNameString(originalNameString);
        }
    }

	protected SingleSourcedEntityBase(NamedSource source) {
		this.source = source;
	}

//********************* GETTER / SETTER *******************************/

    @Transient
    public String getCitationMicroReference() {
        return source == null ? null : this.source.getCitationMicroReference();
    }
    public void setCitationMicroReference(String microReference) {
        this.getSource(true).setCitationMicroReference(isBlank(microReference)? null : microReference);
        checkNullSource();
    }

    @Transient
    public Reference getCitation() {
        return (this.source == null) ? null : source.getCitation();
    }
    public void setCitation(Reference reference) {
        getSource(true).setCitation(reference);
        checkNullSource();
    }

    public NamedSource getSource() {
        return source;
    }
    public void setSource(NamedSource source) {
        this.source = source;
    }

    private void checkNullSource() {
        if (this.source != null && this.source.checkEmpty(true)){
            this.source = null;
        }
    }

    private NamedSource getSource(boolean createIfNotExist){
        if (this.source == null && createIfNotExist){
            this.source = NamedSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
        }
        return source;
    }

// **************** EMPTY ************************/

    @Override
    protected boolean checkEmpty(){
       return super.checkEmpty()
            && this.source == null
           ;
    }

//****************** CLONE ************************************************/

	@Override
	public SingleSourcedEntityBase clone() throws CloneNotSupportedException{
		SingleSourcedEntityBase result = (SingleSourcedEntityBase)super.clone();

		if (this.source != null){
		    result.source = source.clone();
		}

		//no changes to: --
		return result;
	}

//*********************************** EQUALS *********************************************************/


}