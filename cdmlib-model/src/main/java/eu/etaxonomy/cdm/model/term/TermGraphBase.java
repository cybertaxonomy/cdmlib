/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * @author a.mueller
 * @since 07.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermGraph", propOrder = {
      "allowDuplicates",
      "orderRelevant",
      "isFlat"
})
@Entity
@Audited
public abstract class TermGraphBase<TERM extends DefinedTermBase, REL extends TermRelationBase> //<TERM, REL, TermGraphBase>
            extends TermCollection<TERM> {

    private static final long serialVersionUID = -704169783744494023L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermGraphBase.class);


    @XmlElementWrapper(name = "TermRelations")
    @XmlElement(name = "TermRelation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="graph", fetch=FetchType.LAZY, targetEntity = TermRelationBase.class)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded(depth = 2)
    private Set<REL> termRelations = new HashSet<>();

    //#7372 indicates if this tree/graph allows duplicated terms/features
    private boolean allowDuplicates = false;

    private boolean orderRelevant = false;

    private boolean isFlat = false;

 // ******************** CONSTRUCTOR *************************************/

    @Deprecated
    protected TermGraphBase(){}

    protected TermGraphBase(TermType termType) {
        super(termType);
    }

 // ****************** GETTER / SETTER **********************************/

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }
    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    public boolean isOrderRelevant() {
        return orderRelevant;
    }
    public void setOrderRelevant(boolean orderRelevant) {
        this.orderRelevant = orderRelevant;
    }

    public boolean isFlat() {
        return isFlat;
    }
    public void setFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }



    public Set<REL> getTermRelations() {
        return termRelations;
    }
    /**
     * For now protected to avoid type checking etc. Might become
     * public in future
     * @param termRelations
     */
    protected void setTermRelations(Set<REL> termRelations) {
        this.termRelations = termRelations;
    }

    public abstract Set<TERM> getDistinctTerms();
}
