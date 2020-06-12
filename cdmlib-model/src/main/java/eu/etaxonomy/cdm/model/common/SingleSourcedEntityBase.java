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
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

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
        //TODO move to AnnotatableEntity once als ReferencedEntityBase attributes are removed from subclasses
        extends ReferencedEntityBase {

    static final long serialVersionUID = 2035568689268762760L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SingleSourcedEntityBase.class);

    //the source for this single sourced entity
    @XmlElement(name = "source")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    private IdentifiableSource source;

// ************ CONSTRUCTOR ********************************************/

	//for hibernate use only
    protected SingleSourcedEntityBase() {
		super();
	}

	public SingleSourcedEntityBase(IdentifiableSource source) {
		this.source = source;
	}

//********************* GETTER / SETTER *******************************/

	//TODO source

// **************** EMPTY ************************/

    @Override
    protected boolean isEmpty(){
       return super.isEmpty()
            && this.source == null
           ;
    }

//****************** CLONE ************************************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		SingleSourcedEntityBase result = (SingleSourcedEntityBase)super.clone();

		if (this.source != null){
		    result.source = source.clone();
		}

		//no changes to: --
		return result;
	}

//*********************************** EQUALS *********************************************************/




}
