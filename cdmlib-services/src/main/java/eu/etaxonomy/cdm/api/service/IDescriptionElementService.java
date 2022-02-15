/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IDescriptionElementService
        extends IAnnotatableService<DescriptionElementBase> {

    /**
     * Return a Pager containing Annotation entities belonging to the DescriptionElementBase instance supplied, optionally filtered by MarkerType
     * @param annotatedObj The object that "owns" the annotations returned
     * @param status Only return annotations which are marked with a Marker of this type (can be null to return all annotations)
     * @param pageSize The maximum number of terms returned (can be null for all annotations)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param orderHints may be null
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of Annotation entities
     */
    //TODO check if this is in base service already, it was copied from DescriptionService
    public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


}