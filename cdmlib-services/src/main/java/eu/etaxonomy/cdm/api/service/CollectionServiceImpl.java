package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly = true)
public class CollectionServiceImpl extends	IdentifiableServiceBase<Collection, ICollectionDao> implements	ICollectionService {
	
	static private final Logger logger = Logger.getLogger(CollectionServiceImpl.class);

    @Autowired
	@Override
	protected void setDao(ICollectionDao dao) {
		this.dao = dao;
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
	}

	public Pager<Collection> search(Class<? extends Collection> clazz,	String query, Integer pageSize, Integer pageNumber,	List<OrderHint> orderHints, List<String> propertyPaths) {
		Integer numberOfResults = dao.count(clazz,query);
			
		List<Collection> results = new ArrayList<Collection>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,query, pageSize, pageNumber, orderHints, propertyPaths); 
		}
			
		return new DefaultPagerImpl<Collection>(pageNumber, numberOfResults, pageSize, results);
	}
}
