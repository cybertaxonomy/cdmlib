/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
@Service
@Transactional(readOnly = true)
public class PortalDtoServiceImpl implements IPortalDtoService {

    @Autowired
    private ICdmGenericDao dao;

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired()
    @Qualifier("cdmRepository")
    private ICdmRepository repository;

    @Override
    public TaxonPageDto taxonPageDto(TaxonPageDtoConfiguration config) {

        PortalDtoLoader loader = new PortalDtoLoader(repository, dao);
        Taxon taxon = (Taxon)taxonDao.load(config.getTaxonUuid());
        TaxonPageDto dto = null;
        try {
            dto = loader.load(taxon, config);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (dto != null) {
            return dto;
        }else if (true) {
            return dto;
        }

        dto = new TaxonPageDto();

        //taxon data
        String taxonHql = " SELECT t.id as id, t.uuid as uuid, t.titleCache as taxonLabel,"
                + "    n.titleCache as nameLabel "
                + " FROM Taxon t JOIN t.name as n "
                + " WHERE t.uuid = :uuid ";
        //TODO singleResult
        List<Object[]> hqlResult = dao.getHqlResult(taxonHql, new Object[] {config.getTaxonUuid()}, Object[].class);
        dto.setId((int)hqlResult.get(0)[0]);
        dto.setUuid((UUID)hqlResult.get(0)[1]);
        dto.setLabel((String)hqlResult.get(0)[2]);
        dto.setNameLabel((String)hqlResult.get(0)[3]);
//        dto.setTypedTaxonLabel(null);
//        dto.setTypedNameLabel(null);
        dto.setTaggedLabel(null);

        //taxonNodes

        //synonyms
        //TODO homotypic group
        //TODO homotypic group sorting !!!
        String synonymsHql = " SELECT s.id as id, s.uuid as uuid, t.titleCache as taxonLabel,"
                + "    n.titleCache as nameLabel, n.homotypicalGroup.uuid "
                + " FROM Synonym s JOIN s.acceptedTaxon t JOIN s.name as n "
                + " WHERE t.uuid = :uuid ";
        List<Object[]> sysnonymsResult = dao.getHqlResult(synonymsHql, new Object[] {config.getTaxonUuid()}, Object[].class);

        //facts

        //specimens

        //media

        //keys

        return dto;
    }
}
