package eu.etaxonomy.cdm.io.common.mapping;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public interface IRdfMapper {
	/**
	 * @param content
	 * @param parentElement
	 * @return
	 */
	public boolean mapsSource(Resource content, Statement parentElement);
}
