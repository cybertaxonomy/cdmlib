/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IRightsDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @since 15.02.2017
 *
 */
@Repository
public class RightsDaoImpl extends  LanguageStringBaseDaoImpl<Rights> implements IRightsDao  {
    private static final Logger logger = Logger.getLogger(RightsDaoImpl.class);
    /**
     * @param type
     */
    public RightsDaoImpl(Class rightsClass) {
        super(rightsClass);

    }

    public RightsDaoImpl(){
        super(Rights.class);
    }

    @Override
    public List<UuidAndTitleCache<Rights>> getUuidAndTitleCache(Integer limit, String pattern) {
        List<UuidAndTitleCache<Rights>> list = new ArrayList<UuidAndTitleCache<Rights>>();
        Session session = getSession();

        String queryString = "SELECT " +"r.uuid, r.id, r.text, agent.titleCache FROM " + type.getSimpleName() + " AS r LEFT OUTER JOIN r.agent AS agent ";

        if (pattern != null){
            queryString += " WHERE ";
            queryString += " r.text LIKE :pattern";
            queryString += " OR agent.titleCache LIKE :pattern";
        }



         Query query;
        //if (pattern != null){
            query = session.createQuery(queryString);
//      }else{
//          query = session.createQuery("SELECT " +"r.uuid, r.id, r.titleCache, ab.titleCache FROM " + type.getSimpleName() + " AS r LEFT OUTER JOIN r.authorship AS ab ");//"select uuid, titleCache from " + type.getSimpleName());
//      }

        if (limit != null){
            query.setMaxResults(limit);
        }
        if (pattern != null){
              pattern = pattern.replace("*", "%");
              pattern = pattern.replace("?", "_");
              pattern = pattern + "%";
              query.setParameter("pattern", pattern);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        for(Object[] object : result){
            String rightsText = (String) object[2];

            if(rightsText != null){
                String agentTitle = (String) object[3];
                rightsText = rightsText + " - " + agentTitle;

                list.add(new UuidAndTitleCache<Rights>(Rights.class, (UUID) object[0],(Integer)object[1], rightsText));
            }else{
                logger.error("text of rights is null. UUID: " + object[0]);
            }
        }

        return list;
    }


}
