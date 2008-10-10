/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.remote.dto.AnnotationElementSTO;
import eu.etaxonomy.cdm.remote.dto.AnnotationSTO;
import eu.etaxonomy.cdm.remote.dto.AnnotationTO;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 * @version 1.0
 */
@Component
public class AnnotationAssembler extends AssemblerBase<AnnotationSTO, AnnotationTO, AnnotatableEntity> {
	private static Logger logger = Logger.getLogger(AnnotationAssembler.class);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public AnnotationSTO getSTO(AnnotatableEntity cdmObj, Enumeration<Locale> locales) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public AnnotationTO getTO(AnnotatableEntity entity, Enumeration<Locale> locales) {
		
		AnnotationTO to = new AnnotationTO();
		
		to.setUuid(entity.getUuid().toString());
		//
		
		Set<Annotation> annotations = entity.getAnnotations();
		
		for (Annotation annotation : annotations){
			to.addAnnotationElement(getAnnotationElementSTO(annotation));		
		}
		
		return to;
	}
	
	private AnnotationElementSTO getAnnotationElementSTO(Annotation annotation){
		AnnotationElementSTO sto = new AnnotationElementSTO();
		
		sto.setText(annotation.getText());
		
		sto.setCreated(annotation.getCreated());
		
		Person commentator = annotation.getCommentator();
		if(commentator != null){
			//sto.setCommentator(annotation.getCommentator());
		}
			
		return sto;
	}
}
