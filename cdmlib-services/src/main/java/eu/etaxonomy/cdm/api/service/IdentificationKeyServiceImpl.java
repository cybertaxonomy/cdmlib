package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;

@Service
@Transactional(readOnly = true)
public class IdentificationKeyServiceImpl implements IIdentificationKeyService {

    IIdentificationKeyDao dao;

    @Autowired
    public void setDao(IIdentificationKeyDao dao) {
        this.dao = dao;
    }

    @Override
    public Pager<IIdentificationKey> page(Integer pageSize, Integer pageNumber,	List<String> propertyPaths) {
        Integer numberOfResults = dao.count();
        List<IIdentificationKey> results = new ArrayList<IIdentificationKey>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            Integer limit = PagerUtils.limitFor(pageSize);
            Integer start = PagerUtils.startFor(pageSize, pageNumber);
            results = dao.list(limit, start, propertyPaths);
        }
        return new DefaultPagerImpl<IIdentificationKey>(pageNumber, numberOfResults, pageSize, results);
    }


    @Override
    public <T extends IIdentificationKey> Pager<T> findKeysConvering(TaxonBase taxon,
            Class<T> type, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {

        Long numberOfResults = dao.countByTaxonomicScope(taxon, type);
        List<T> results = new ArrayList<T>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)){
            results = dao.findByTaxonomicScope(taxon, type, pageSize, pageNumber, propertyPaths);
        }
        return new DefaultPagerImpl<T>(pageNumber, numberOfResults.intValue(), pageSize, results);
    }

}
