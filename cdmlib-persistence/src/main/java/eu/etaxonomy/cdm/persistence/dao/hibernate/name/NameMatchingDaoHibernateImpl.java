/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.INameMatchingDao;
import eu.etaxonomy.cdm.persistence.dto.NameMatchingParts;

/**
 * @author andreabee90
 *
 */

@Repository
@Qualifier("nameMatchingDaoHibernateImpl")
public class NameMatchingDaoHibernateImpl
        extends IdentifiableDaoBase<TaxonName>
        implements INameMatchingDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("unchecked")
    public NameMatchingDaoHibernateImpl() {
        super(TaxonName.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = TaxonName.class;
    }

    @Override
    public List<NameMatchingParts> findNameMatchingParts(Map<String, Double> postFilteredGenusOrUninominalWithDis) {
        StringBuilder hql = new StringBuilder();
        List<NameMatchingParts> result = new ArrayList<>();

        if(postFilteredGenusOrUninominalWithDis != null && postFilteredGenusOrUninominalWithDis.size() > 0 ){
            Set<String> generaSet = postFilteredGenusOrUninominalWithDis.keySet();
            List <String> generaList = new ArrayList <>(generaSet);
            hql = prepareFindTaxonNamePartsString("genusOrUninomial","generaList");
            Query<NameMatchingParts> query = getSession().createQuery(hql.toString(), NameMatchingParts.class);
            query.setParameterList("generaList", generaList);
            result = query.list();
        }
        return result;
    }

    private StringBuilder prepareFindTaxonNamePartsString(String column, String values) {

        StringBuilder hql = new StringBuilder();

        hql.append("select new eu.etaxonomy.cdm.persistence.dto.NameMatchingParts(n.id, n.uuid, n.titleCache, n.authorshipCache, "
              + "n.genusOrUninomial, n.infraGenericEpithet, n.specificEpithet, n.infraSpecificEpithet, n.nameCache, n.rank, "
              + "combinationAuthorship.nomenclaturalTitleCache, exCombinationAuthorship.nomenclaturalTitleCache, "
              + "basionymAuthorship.nomenclaturalTitleCache, exBasionymAuthorship.nomenclaturalTitleCache )");
        hql.append(" from TaxonName n");
        hql.append(" LEFT JOIN n.combinationAuthorship AS combinationAuthorship");
        hql.append(" LEFT JOIN n.exCombinationAuthorship AS exCombinationAuthorship");
        hql.append(" LEFT JOIN n.basionymAuthorship AS basionymAuthorship");
        hql.append(" LEFT JOIN n.exBasionymAuthorship AS exBasionymAuthorship");
        hql.append(" where 1 = 1");
        hql.append(" and n."+column+ " in (");
        hql.append(":" + values);
        hql.append(") ");
        return hql;
  }
}