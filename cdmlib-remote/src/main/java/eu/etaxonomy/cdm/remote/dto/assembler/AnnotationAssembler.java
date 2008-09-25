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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.remote.dto.AnnotationSTO;
import eu.etaxonomy.cdm.remote.dto.AnnotationTO;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 * @version 1.0
 */
@Component
public class AnnotationAssembler extends AssemblerBase<AnnotationSTO, AnnotationTO, Annotation> {
	private static Logger logger = Logger.getLogger(AnnotationAssembler.class);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public AnnotationSTO getSTO(Annotation cdmObj, Enumeration<Locale> locales) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public AnnotationTO getTO(Annotation annotation, Enumeration<Locale> locales) {
		
		AnnotationTO to = new AnnotationTO();
		
		to.setText(annotation.getText());
		
		return to;
	}
}
