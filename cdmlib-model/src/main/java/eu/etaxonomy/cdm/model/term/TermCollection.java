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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 06.03.2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermCollection", propOrder = {
        "termRelations",
        "allowDuplicates",
        "orderRelevant",
        "isFlat"
})
@Entity
@Audited
@Table(name="TermCollection")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class TermCollection<TERM extends DefinedTermBase, REL extends TermRelationBase>
            extends TermBase{

    private static final long serialVersionUID = 6102175902060054329L;

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

    private boolean orderRelevant = true;

    private boolean isFlat = false;


//*************************** CONSTRUCTOR *************************************/

    @SuppressWarnings("deprecation")
    protected TermCollection(){}

    protected TermCollection(TermType type){
        super(type);
    }

    protected TermCollection(TermType type, String term, String label, String labelAbbrev, Language lang) {
        super(type, term, label, labelAbbrev, lang);
    }

    protected TermCollection(TermType type, String term, String label, String labelAbbrev) {
        super(type, term, label, labelAbbrev, null);
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

    /**
     * @Deprecated for use by defined subclasses only
     */
    @Deprecated
    protected Set<REL> termRelations() {
        return termRelations;
    }
    /**
     * @Deprecated for use by defined subclasses only
     */
    @Deprecated
    protected void termRelations(Set<REL> termRelations) {
        this.termRelations = termRelations;
    }

}
