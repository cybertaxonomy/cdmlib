package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.persistence.dao.common.IMarkerDao;

@Repository
public class MarkerDaoImpl extends CdmEntityDaoBase<Marker> implements IMarkerDao {

	public MarkerDaoImpl() {
		super(Marker.class);
	}

}
