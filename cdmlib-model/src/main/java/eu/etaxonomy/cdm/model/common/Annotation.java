/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author m.doering
 * @created 08-Nov-2007 13:06:10
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Annotation", propOrder = {
    "commentator",
    "annotationType",
    "linkbackUri",
    "intextReferences"
})
@Entity
@Audited
public class Annotation extends LanguageStringBase implements Cloneable, IIntextReferencable {
	private static final long serialVersionUID = -4484677078599520233L;
	@SuppressWarnings("unused")
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

	public static Annotation NewInstance(String text, AnnotationType annotationType, Language lang){
		Annotation annotation = new Annotation(text, lang);
		annotation.setAnnotationType(annotationType);
		return annotation;
	}

	/**
	 * Factory method. Using default language.
	 * @param text
	 * @return
	 */
	public static Annotation NewDefaultLanguageInstance(String text){
		return new Annotation(text, Language.DEFAULT());
	}


    //TODO do we need to add it to JAXB? #4706
    @XmlElementWrapper(name = "IntextReferences", nillable = true)
    @XmlElement(name = "IntextReference")
    @OneToMany(mappedBy="languageString", fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
//  @Merge(MergeMode.ADD_CLONE)
    private Set<IntextReference> intextReferences = new HashSet<>();

	//Human annotation
	@XmlElement(name = "Commentator")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Person commentator;

    @XmlElement(name = "AnnotationType")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private AnnotationType annotationType;

	// for external annotations/comments the URI of these can be set.
	// should be useful to implement trackback, pingback or linkback:
	// http://en.wikipedia.org/wiki/Linkback
	@XmlElement(name = "LinkbackUri")
	@Type(type="uriUserType")
	private URI linkbackUri;


// *********** CONSTRUCTOR **************************************/

	protected Annotation(){
		super();
	}

	/**
	 * Constructor
	 * @param text
	 * @param lang
	 */
	protected Annotation(String text, Language language) {
		super(text, language);
	}

//******************** GETTER /SETTER *************************/


	public AnnotationType getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}

	public Person getCommentator(){
		return this.commentator;
	}
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}


	public URI getLinkbackUri() {
		return linkbackUri;
	}
	public void setLinkbackUri(URI linkbackUri) {
		this.linkbackUri = linkbackUri;
	}


	//*************** INTEXT REFERENCE **********************************************

	@Override
    public Set<IntextReference> getIntextReferences(){
		return this.intextReferences;
	}
	@Override
    public void addIntextReference(IntextReference intextReference){
		if (intextReference != null){
			intextReference.setAnnotation(this);
			getIntextReferences().add(intextReference);
		}
	}

	@Override
    public void removeIntextReference(IntextReference intextReference){
		if(getIntextReferences().contains(intextReference)) {
			getIntextReferences().remove(intextReference);
			intextReference.setAnnotation((Annotation)null);
		}
	}


// ***************************** TO STRING ***********************************


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.CdmBase#toString()
	 */
	@Override
	public String toString() {
		if (StringUtils.isNotBlank(this.text)){
			return "Ann.: " + this.text;
		}else{
			return super.toString();
		}
	}



//****************** CLONE ************************************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		Annotation result = (Annotation)super.clone();
		//TODO do we really need this, it should not change anything
		result.setCommentator(this.getCommentator());
		//TODO do we really need this, it should not change anything
		result.setAnnotationType(this.getAnnotationType());

		try {
			result.setLinkbackUri(this.linkbackUri == null ? null : new URI(this.linkbackUri.toString()));
		} catch (URISyntaxException e) {
			//do nothing
		}
		//IntextReferences
		result.intextReferences = new HashSet<IntextReference>();
		for (IntextReference intextReference : getIntextReferences()){
			IntextReference newIntextReference = (IntextReference)intextReference.clone();
			result.addIntextReference(newIntextReference);
		}

		return result;
	}

}
