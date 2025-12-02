/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * An {@link IdentifiableEntity} which allows to have {@link Credit}s.
 * See #10772 for details.
 *
 * @author muellera
 * @since 02.12.2025
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreditableEntity", propOrder = {
    "credits",
})
@MappedSuperclass
@Audited
public abstract class CreditableEntity<S extends IIdentifiableEntityCacheStrategy<?>>
    extends IdentifiableEntity<S>
    implements IHasCredits{

    private static final long serialVersionUID = 3612910783891987069L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElementWrapper(name = "Credits", nillable = true)
    @XmlElement(name = "Credit")
    @OrderColumn(name="sortIndex")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    //TODO
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private List<Credit> credits = new ArrayList<>();


//********************** CREDITS **********************************************

    @Override
    public List<Credit> getCredits() {
        if(credits == null) {
            this.credits = new ArrayList<>();
        }
        return this.credits;
    }

    @Override
    public Credit getCredits(Integer index){
        return getCredits().get(index);
    }

    @Override
    public void addCredit(Credit credit){
        getCredits().add(credit);
    }

    @Override
    public void addCredit(Credit credit, int index){
        getCredits().add(index, credit);
    }

    @Override
    public void removeCredit(Credit credit){
        getCredits().remove(credit);
    }

    @Override
    public void removeCredit(int index){
        getCredits().remove(index);
    }

    @Override
    public boolean replaceCredit(Credit newObject, Credit oldObject){
        return replaceInList(this.credits, newObject, oldObject);
    }

  //***************** SUPPLEMENTAL DATA **************************************/

    @Override
    @Transient
    public boolean hasSupplementalData() {
        return super.hasSupplementalData()
                || !this.credits.isEmpty()
                ;
    }

    @Override
    public boolean hasSupplementalData(Set<UUID> exceptFor) {
        return super.hasSupplementalData(exceptFor)
           || !this.credits.isEmpty()  //credits don't have types
           ;
    }

//******************** CLONE **********************************************/

    @Override
    public CreditableEntity<S> clone() throws CloneNotSupportedException{
        CreditableEntity<S> result = (CreditableEntity<S>)super.clone();

        //Credits
        result.credits = new ArrayList<>();
        for(Credit credit : getCredits()) {
            Credit newCredit = credit.clone();
            result.addCredit(newCredit);
        }

        //no changes to: -
        return result;
    }
}