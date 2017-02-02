/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.CorrectEpithetsForRank;
import eu.etaxonomy.cdm.validation.annotation.NameMustHaveAuthority;
import eu.etaxonomy.cdm.validation.annotation.NoDuplicateNames;

/**
 * The taxon name class for all non viral taxa. Parenthetical authorship is derived
 * from basionym relationship. The scientific name including author strings and
 * maybe year can be stored as a string in the inherited {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
 * The year itself is an information obtained from the {@link eu.etaxonomy.cdm.model.reference.Reference#getYear() nomenclatural reference}.
 * The scientific name string without author strings and year can be stored in the {@link #getNameCache() nameCache} attribute.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonName according to the TDWG ontology
 * <li> ScientificName and CanonicalName according to the TCS
 * <li> ScientificName according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonViralName", propOrder = {})
@XmlRootElement(name = "NonViralName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
@Configurable

@CorrectEpithetsForRank(groups = Level2.class)
@NameMustHaveAuthority(groups = Level2.class)
@NoDuplicateNames(groups = Level3.class)
public class NonViralName<T extends NonViralName<?>>
        extends TaxonNameBase<T, INonViralNameCacheStrategy<T>>{

    private static final long serialVersionUID = -9083811681449792683L;

// ************************** CONSTRUCTORS *************/

    //needed by hibernate
    protected NonViralName(){
        super();
    }

    protected NonViralName(Rank rank, HomotypicalGroup homotypicalGroup) {
        super(rank, homotypicalGroup);
    }

    protected NonViralName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
        super(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
    }


//*********************** CLONE ********************************************************/

    @Override
    public Object clone() {
        NonViralName<?> result = (NonViralName<?>)super.clone();

        //no changes to:
        return result;
    }
}
