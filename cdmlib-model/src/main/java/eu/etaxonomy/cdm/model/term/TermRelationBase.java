/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.mueller
 * @since 01.03.2019
 *
 */
public abstract class TermRelationBase
        extends VersionableEntity
        implements IHasTermType {

    private static final long serialVersionUID = -7832621515891195623L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermRelationBase.class);


    /**
     * The {@link TermType type} of this term node.
     * Must be the same type as for the {@link FeatureTree term collection}
     * this node belongs to and as the term type of the term this node links to.
     */
    @XmlAttribute(name ="TermType")
    @Column(name="termType")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.term.TermType")}
    )
    @Audited
    private TermType termType;

 // ******************** CONSTRUCTOR ***************************************/

    protected TermRelationBase(){}

    /**
     * Class constructor: creates a new empty feature node instance.
     */
    protected TermRelationBase(TermType termType) {
        this.termType = termType;
        IHasTermType.checkTermTypeNull(this);
    }


    @Override
    public TermType getTermType() {
        return termType;
    }

//    TODO clone
}
