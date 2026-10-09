/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.beans.Transient;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

import eu.etaxonomy.cdm.model.common.IHasCredits;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.media.IHasLink;

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
@Table(name = "TermCollection")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class TermCollection<TERM extends DefinedTermBase, REL extends TermRelationBase>
        extends TermBase
        implements IHasCredits, IHasLink {

    private static final long serialVersionUID = 6102175902060054329L;

    @XmlElementWrapper(name = "TermRelations")
    @XmlElement(name = "TermRelation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="graph", fetch=FetchType.LAZY, targetEntity = TermRelationBase.class)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @IndexedEmbedded(includeDepth = 2)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
    private Set<REL> termRelations = new HashSet<>();

    //#7372 indicates if this tree/graph allows duplicated terms/features
    private boolean allowDuplicates = false;

    private boolean orderRelevant = true;

    private boolean isFlat = false;

//*************************** CONSTRUCTOR *************************************/

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    TermCollection(){}

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

    @Transient
    @jakarta.persistence.Transient
    public abstract Set<TERM> getDistinctTerms();

    /**
     * @Deprecated for use by defined subclasses only
     */
    @Deprecated
    Set<REL> termRelations() {
        return termRelations;
    }
    /**
     * @Deprecated for use by defined subclasses only
     */
    @Deprecated
    void termRelations(Set<REL> termRelations) {
        this.termRelations = termRelations;
    }
}
