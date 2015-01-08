package eu.etaxonomy.cdm.io.common.mapping;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public interface IRdfMapper {
	/**
	 * @param content
	 * @param parentElement
	 * @return
	 */
	public boolean mapsSource(Resource content, Statement parentElement);
}
