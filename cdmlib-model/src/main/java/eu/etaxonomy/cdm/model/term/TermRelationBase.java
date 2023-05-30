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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Common base class for {@link TermNode} and {@link TermRelation}.
 * @author a.mueller
 * @since 06.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermRelationBase", propOrder = {
        "graph",
        "termType",
        "term"
})
@Entity
@Audited
@Table(name="TermRelation", indexes = { @Index(name = "termNodeTreeIndex", columnList = "treeIndex") })  //was feature NodeTreeIndex before
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TermRelationBase<TERM extends DefinedTermBase, REL extends TermRelationBase, GRAPH extends TermGraphBase>
        extends AnnotatableEntity
        implements IHasTermType {

    private static final long serialVersionUID = -7832621515891195623L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "TermGraph")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=TermTree.class)
     //TODO Val #3379
//    @NotNull
    private GRAPH graph;

    /**
     * The {@link TermType type} of this term relation.
     * Must be the same type as for the {@link TermCollection term collection}
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

    @XmlElement(name = "Term")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    private TERM term;

 // ******************** CONSTRUCTOR ***************************************/

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    TermRelationBase(){}

    protected TermRelationBase(TermType termType) {
        this.termType = termType;
        IHasTermType.checkTermTypeNull(this);
    }

// ********************** *****************************/

    @Override
    public TermType getTermType() {
        return termType;
    }

//** ********************** Term ******************************/

    /**
     * Returns the {@link DefinedTermBase term} <i>this</i> term tree node is based on.
     */
    public TERM getTerm() {
        return CdmBase.deproxy(term);
    }
    public void setTerm(TERM term) {
        checkTermTypeKindOf(term);
        this.term = term;
    }

    /**
     * Throws {@link IllegalArgumentException} if the given
     * term has not the same term type as this term or if it is no sub or super type
     * or if term type is null.
     * @param term
     */
    private void checkTermTypeKindOf(IHasTermType descendant) {
        IHasTermType.checkTermTypeEqualOrDescendant(this, descendant);
    }

//*************************** GRAPH ************************************/

    public GRAPH getGraph() {
        return graph;
    }
    protected void setGraph(GRAPH graph) {
        checkTermType(graph);
        this.graph = graph;
    }


    /**
     * Throws {@link IllegalArgumentException} if the given
     * term has not the same term type as this term or if term type is null.
     * @param term
     */
    protected void checkTermType(IHasTermType term) {
        IHasTermType.checkTermTypes(term, this);
    }

// ********************** CLONE **************************//

    @Override
    public TermRelationBase<TERM, REL, GRAPH> clone() throws CloneNotSupportedException{
        @SuppressWarnings("unchecked")
        TermRelationBase<TERM, REL, GRAPH> result = (TermRelationBase<TERM, REL, GRAPH>)super.clone();
        return result;
    }
}