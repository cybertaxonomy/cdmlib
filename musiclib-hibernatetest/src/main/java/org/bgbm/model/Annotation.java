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
@Entity
public class Annotation extends MetaBase{
	public Annotation() {
		super();
	}
	static Logger logger = Logger.getLogger(Annotation.class);
	//Human annotation
	private Person commentator;
	private String note;

	public Annotation(String text) {
		super();
		note=text;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	@ManyToOne
	public Person getCommentator(){
		return this.commentator;
	}
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}
	public String toString(){
		String n = "";
		if (note!=null){
			n = note; 
		}
		return "<"+n+">";
	}
}