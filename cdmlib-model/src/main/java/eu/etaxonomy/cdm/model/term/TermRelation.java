/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Class which creates a relation between 2 {@link DefinedTermBase defined terms}.
 *
 * @author a.mueller
 * @since 06.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermRelation", propOrder = {
        "toTerm",
})
@XmlRootElement(name = "TermRelation")
@Entity
@Audited
public abstract class TermRelation<T extends DefinedTermBase>
        extends TermRelationBase<T, TermRelation<T>, TermGraph<T>> {

    private static final long serialVersionUID = -7835146268318871033L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermRelation.class);


    @XmlElement(name = "ToTerm")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    private T toTerm;

 // ******************** CONSTRUCTOR ***************************************/

    protected TermRelation(){}

//** ********************** To Term ******************************/

    /**
     * Returns the {@link DefinedTermBase term} <i>this</i> term tree node is based on.
     */
    public T getToTerm() {
        return CdmBase.deproxy(toTerm);
    }

    public void setToTerm(T toTerm) {
        checkTermType(toTerm);
        this.toTerm = toTerm;
    }

 // ********************** CLONE **************************//

    @Override
    public TermRelation<T> clone() throws CloneNotSupportedException{
        TermRelation<T> result = (TermRelation<T>)super.clone();
        return result;
    }
}
