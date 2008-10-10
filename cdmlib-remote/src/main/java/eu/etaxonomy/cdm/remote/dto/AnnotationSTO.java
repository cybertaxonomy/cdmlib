/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 * @version 1.0
 */
public class AnnotationSTO extends BaseTO {
	private static Logger logger = Logger.getLogger(AnnotationSTO.class);
	
	private Set<AnnotationElementSTO> annotationElements = new HashSet<AnnotationElementSTO>();

	/**
	 * @return the annotationElements
	 */
	public Set<AnnotationElementSTO> getAnnotationElements() {
		return annotationElements;
	}

	/**
	 * @param annotationElements the annotationElements to set
	 */
	public void setAnnotationElements(Set<AnnotationElementSTO> annotationElements) {
		this.annotationElements = annotationElements;
	}
	
	public void addAnnotationElement(AnnotationElementSTO annotationElementSTO){
		this.annotationElements.add(annotationElementSTO);
	}

}
