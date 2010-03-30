// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IAnnotatableService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;

/**
 * based on org.cateproject.controller.common
 * 
 * @author a.kohlbecker
 *
 * @param <T>
 * @param <DAO>
 */
public abstract class AnnotatableController<T extends AnnotatableEntity, SERVICE extends IAnnotatableService<T>> extends BaseController<T,SERVICE> {
	
	protected static final List<String> ANNOTATION_INIT_STRATEGY = Arrays.asList(new String []{
			"$"
	});

	@RequestMapping(value = "/*/*/annotation", method = RequestMethod.GET)
	public Pager<Annotation> getAnnotations(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		T annotatableEntity;
		try {
			UUID uuid = readValueUuid(request, null);
			Assert.notNull(uuid, HttpStatusMessage.UUID_NOT_FOUND.toString());
			
			annotatableEntity = service.find(uuid);
			Assert.notNull(annotatableEntity, HttpStatusMessage.UUID_NOT_FOUND.toString());
		} catch (IllegalArgumentException iae) {
			HttpStatusMessage.fromString(iae.getMessage()).send(response);
			return null;
		}
		
		Pager<Annotation> annotations = service.getAnnotations(annotatableEntity, null, null, 0, null, ANNOTATION_INIT_STRATEGY);
		return annotations;
	}


}
