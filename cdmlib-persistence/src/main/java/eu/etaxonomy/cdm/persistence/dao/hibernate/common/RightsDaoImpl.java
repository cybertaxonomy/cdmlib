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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IRightsDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author k.luther
 * @since 15.02.2017
 */
@Repository
public class RightsDaoImpl extends  LanguageStringBaseDaoImpl<Rights> implements IRightsDao  {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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

        String queryString = "SELECT " +"r.uuid, r.id, r.text, r.abbreviatedText, r.uri,  agent.titleCache, type.titleCache FROM " + type.getSimpleName() + " AS r LEFT OUTER JOIN r.agent AS agent LEFT OUTER JOIN r.type as type";

        if (pattern != null){
            queryString += " WHERE ";
            queryString += " r.text LIKE :pattern";
            queryString += " OR agent.titleCache LIKE :pattern";
            queryString += " OR r.abbreviatedText LIKE :pattern";
//            queryString += " OR r.uri LIKE :pattern";
//            queryString += " OR type.titleCache LIKE :pattern";
        }

        Query<Object[]> query;
        //if (pattern != null){
            query = session.createQuery(queryString, Object[].class);
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

        List<Object[]> result = query.list();
        for(Object[] object : result){
            if (object[2] == null && object[3] == null && object[4] == null &&  object[5] == null &&  object[6] == null){
                continue;
            }
            String rightsText = "";
            String text = (String) object[2];
            String abbrev = (String) object[3];
            String uri = object[4]!= null?((URI)object[4]).toString(): null;
            String agentTitle = (String) object[5];
            String typeLabel = (String) object[6];

            boolean isFirst = true;

            if (StringUtils.isNotBlank(text)){
                rightsText = text;
            }
            if (StringUtils.isNotBlank(agentTitle)){
                rightsText = rightsText + (StringUtils.isBlank(rightsText)? "":" - ") + agentTitle ;
            }
            if (StringUtils.isNotBlank(typeLabel)){

                rightsText = rightsText+ (StringUtils.isBlank(rightsText) ?"":" - ") + typeLabel;
            }

            if (StringUtils.isNotBlank(abbrev)){
                rightsText = rightsText + (StringUtils.isBlank(rightsText) ?"":" - ") + abbrev;
            }
            if (StringUtils.isNotBlank(uri)){
                rightsText = rightsText + (StringUtils.isBlank(rightsText) ?"":" - ") + uri;
            }

            list.add(new UuidAndTitleCache<Rights>(Rights.class, (UUID) object[0],(Integer)object[1], rightsText));
        }

        return list;
    }
}