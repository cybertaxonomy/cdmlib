/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;

public interface IDescriptionElementDao extends IAnnotatableDao<DescriptionElementBase>,ISearchableDao<DescriptionElementBase> {
	
    /**
     * Returns a List of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a List of media instances
     */
    public List<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
    /**
     * Returns a count of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
     * @return a count of media instances
     */
	public int countMedia(DescriptionElementBase descriptionElement);
}
