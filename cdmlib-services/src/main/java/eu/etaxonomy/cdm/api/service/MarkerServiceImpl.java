package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.MarkerDaoImpl;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly = true)
public class MarkerServiceImpl extends	VersionableServiceBase<Marker, MarkerDaoImpl> implements IMarkerService {

	@Autowired
	protected void setDao(MarkerDaoImpl dao) {
		this.dao = dao;
	}

	public Pager<Marker> page(MarkerType markerType, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        Integer numberOfResults = dao.count(markerType);
		
		List<Marker> results = new ArrayList<Marker>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(markerType, pageSize, pageNumber, orderHints, propertyPaths);
		}
		
		return new DefaultPagerImpl<Marker>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Marker> list(User creator, MarkerType markerType,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        Integer numberOfResults = dao.count(creator,markerType);
		
		List<Marker> results = new ArrayList<Marker>();
		if(numberOfResults > 0) { // no point checking again  //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.list(creator,markerType, pageSize, pageNumber, orderHints, propertyPaths);
		}
		
		return new DefaultPagerImpl<Marker>(pageNumber, numberOfResults, pageSize, results);
	}

	public int count(User creator, MarkerType markerType) {
        return dao.count(creator,markerType);
	}
}

