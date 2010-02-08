package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;

@Service
@Transactional(readOnly = true)
public class IdentificationKeyServiceImpl implements IIdentificationKeyService {
	
	IIdentificationKeyDao dao;
	
	@Autowired
	public void setDao(IIdentificationKeyDao dao) {
		this.dao = dao;
	}

	public Pager<IIdentificationKey> page(Integer pageSize, Integer pageNumber,	List<String> propertyPaths) {
		Integer numberOfResults = dao.count();
		List<IIdentificationKey> results = new ArrayList<IIdentificationKey>();
		pageNumber = pageNumber == null ? 0 : pageNumber;
		if(numberOfResults > 0) { // no point checking again
			Integer start = pageSize == null ? 0 : pageSize * pageNumber;
			results = dao.list(pageSize, start, propertyPaths);
		}
		return new DefaultPagerImpl<IIdentificationKey>(pageNumber, numberOfResults, pageSize, results);
	}

}
