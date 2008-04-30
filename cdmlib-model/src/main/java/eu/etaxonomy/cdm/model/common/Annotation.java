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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.net.MalformedURLException;
import java.net.URL;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@Entity
public class Annotation extends LanguageStringBase {
	private static final Logger logger = Logger.getLogger(Annotation.class);
	
	
	/**
	 * Factory method.
	 * @param text
	 * @param lang
	 * @return
	 */
	public static Annotation NewInstance(String text, Language lang){
		return new Annotation(text, lang);
	}
	
	/**
	 * Constructor
	 * @param text
	 * @param lang
	 */
	protected Annotation(String text, Language language) {
		super(text, language);
	}

	//Human annotation
	private Person commentator;
	private AnnotatableEntity annotatedObj;
	// for external annotations/comments the URL of these can be set.
	// should be useful to implement trackback, pingback or linkback:
	// http://en.wikipedia.org/wiki/Linkback
	private URL linkbackUrl;
	
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
	@Cascade({CascadeType.SAVE_UPDATE})
	public Person getCommentator(){
		return this.commentator;
	}
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}
	
	@Transient
	public URL getLinkbackUrl() {
		return linkbackUrl;
	}
	public void setLinkbackUrl(URL linkbackUrl) {
		this.linkbackUrl = linkbackUrl;
	}
	
	/**
	 * private get/set methods for Hibernate that allows us to save the URL as strings
	 * @return
	 */
	private String getLinkbackUrlStr() {
		if (linkbackUrl == null){
			return null;
		}
		return linkbackUrl.toString();
	}
	private void setLinkbackUrlStr(String linkbackUrlString) {
		if (linkbackUrlString == null){
			this.linkbackUrl = null;
		}else{
			try {
				this.linkbackUrl = new URL(linkbackUrlString);
			} catch (MalformedURLException e) { //can't be thrown as otherwise Hibernate throws PropertyAccessExceptioin
				logger.warn("Runtime error occurred in setLinkbackUrlStr");
				e.printStackTrace();
			}
		}
	}
}