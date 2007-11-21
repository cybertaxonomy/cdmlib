                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        /**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package org.bgbm.model;


import org.apache.log4j.Logger;
import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */

public abstract class AnnotatableEntity {
	static Logger logger = Logger.getLogger(AnnotatableEntity.class);
	private Set<Annotation> annotations = new HashSet();
	
	@OneToMany
	public Set<Annotation> getAnnotations(){
		return this.annotations;
	}
	public void addAnnotations(Annotation annotation){

	}
	public void removeAnnotations(Annotation annotation){

	}
	public void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}
}