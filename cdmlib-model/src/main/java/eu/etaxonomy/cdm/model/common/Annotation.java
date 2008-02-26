/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.agent.Person;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@Entity
public class Annotation extends LanguageString {
	public Annotation(String text, Language lang) {
		super(text, lang);
	}

	static Logger logger = Logger.getLogger(Annotation.class);
	//Human annotation
	private Person commentator;
	private AnnotatableEntity annotatedObj;

	@Transient
	public AnnotatableEntity getAnnotatedObj() {
		return annotatedObj;
	}
	protected void setAnnotatedObj(AnnotatableEntity newAnnotatedObj) {
		// Hibernate bidirectional cascade hack: 
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1054
		if(this.annotatedObj == newAnnotatedObj) return;
		if (annotatedObj != null) { 
			annotatedObj.annotations.remove(this);
		}
		if (newAnnotatedObj!= null) { 
			newAnnotatedObj.annotations.add(this);
		}
		this.annotatedObj = newAnnotatedObj;		
	}

	@ManyToOne
	public Person getCommentator(){
		return this.commentator;
	}
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}

}