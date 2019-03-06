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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * Common base class for {@link TermTreeNode} and {@link TermRelation}.
 * @author a.mueller
 * @since 06.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermRelationBase", propOrder = {
        "termTree",
        "termType",
        "term"
})
@Entity
@Audited
@Table(name="TermRelation", indexes = { @Index(name = "termTreeNodeTreeIndex", columnList = "treeIndex") })  //was feature NodeTreeIndex before
public abstract class TermRelationBase<T extends DefinedTermBase>
        extends VersionableEntity
        implements IHasTermType {

    private static final long serialVersionUID = -7832621515891195623L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermRelationBase.class);

    @XmlElement(name = "TermTree")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=FeatureTree.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE}) //TODO this usage is incorrect, needed only for OneToMany, check why it is here, can it be removed??
     //TODO Val #3379
//    @NotNull
    private FeatureTree<T> termTree;

    /**
     * The {@link TermType type} of this term relation.
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

    @XmlElement(name = "Term")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    private T term;

 // ******************** CONSTRUCTOR ***************************************/

    protected TermRelationBase(){}

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
    public T getTerm() {
        return CdmBase.deproxy(term);
    }
    public void setTerm(T term) {
        checkTermType(term);
        this.term = term;
    }


//*************************** TREE ************************************/

    public FeatureTree<T> getFeatureTree() {
        return termTree;
    }

    protected void setFeatureTree(FeatureTree<T> featureTree) {
        checkTermType(featureTree);
        this.termTree = featureTree;
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

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException{
        TermRelationBase<T> result;
        result = (TermRelationBase<T>)super.clone();
        return result;
    }
}
