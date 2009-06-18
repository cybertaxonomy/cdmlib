// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;

/**
 * @author a.kohlbecker
 * @date 18.06.2009
 *
 */
@Service
@Transactional(readOnly=true)
public class EditGeoService implements IEditGeoService{
	
	@Autowired
	private IDescriptionDao dao;
	
	private static final Set<Feature> distributionFeature;

	static {
		distributionFeature = new HashSet<Feature>();
		distributionFeature.add(Feature.DISTRIBUTION());
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IEditGeoService#getEditGeoServiceUrlParameterString(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.Map, int, int, java.lang.String, java.lang.String)
	 */
	public String getEditGeoServiceUrlParameterString(Taxon taxon,
			Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceTermColors, int width, int height, String bbox,
			String backLayer) {
		
		List<TaxonDescription> taxonDescriptions = dao.getTaxonDescriptions(taxon, null, null, null, null, null);
		
		Set<Distribution> distCollection = new HashSet<Distribution>();
		// get descriptions elements for each description
		for (TaxonDescription td : taxonDescriptions) {
			List<Distribution> dists = dao.getDescriptionElements(td, distributionFeature,
				Distribution.class, null, null, null);
			distCollection.addAll(dists);
		}
		// generate the uri parameter string
		String uriParams = EditGeoServiceUtilities.getEditGeoServiceUrlParameterString(distCollection,
			presenceAbsenceTermColors, 0, 0, null, "tdwg4");

		return uriParams;
	}
	
}
