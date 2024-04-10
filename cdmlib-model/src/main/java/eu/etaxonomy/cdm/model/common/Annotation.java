/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.FieldBridge;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:06:10
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
public class Annotation extends LanguageStringBase implements IIntextReferencable {

	private static final long serialVersionUID = -4484677078599520233L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

// ***************************** ATTRIBUTES **************************/

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
    @FieldBridge(impl = UriBridge.class)
    @Type(type="uriUserType")
    private URI linkbackUri;

// ******************************* FACTORY *******************************/

	/**
	 * Factory method.
	 */
	public static Annotation NewInstance(String text, Language lang){
		return new Annotation(text, lang);
	}

	public static Annotation NewInstance(String text, AnnotationType annotationType, Language lang){
		Annotation annotation = new Annotation(text, lang);
		annotation.setAnnotationType(annotationType);
		return annotation;
	}

    public static Annotation NewEditorialDefaultLanguageInstance(String text){
        Annotation annotation = new Annotation(text, Language.DEFAULT());
        annotation.setAnnotationType(AnnotationType.EDITORIAL());
        return annotation;
    }

	/**
	 * Factory method. Using default language.
	 */
	public static Annotation NewDefaultLanguageInstance(String text){
		return new Annotation(text, Language.DEFAULT());
	}

// *********** CONSTRUCTOR **************************************/

	protected Annotation(){}

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
    public IntextReference addIntextReference(IIntextReferenceTarget target, String start, String inner, String end){
        return IntextReferenceHelper.addIntextReference(target, this, start, inner, end);
    }
    @Override
    public IntextReference addIntextReference(IIntextReferenceTarget target, int start, int end){
        return IntextReferenceHelper.addIntextReference(target, this, start, end);
    }

	@Override
    public void addIntextReference(IntextReference intextReference){
		if (intextReference != null){
			intextReference.setReferencedEntity(this);
			getIntextReferences().add(intextReference);
		}
	}

	@Override
    public void removeIntextReference(IntextReference intextReference){
		if(getIntextReferences().contains(intextReference)) {
			getIntextReferences().remove(intextReference);
			intextReference.setReferencedEntity((Annotation)null);
		}
	}

// ***************************** TO STRING ***********************************

	@Override
	public String toString() {
		if (isNotBlank(this.text)){
			return "Ann.: " + this.text;
		}else{
			return super.toString();
		}
	}

//****************** CLONE ************************************************/

	@Override
	public Annotation clone() throws CloneNotSupportedException{

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
		result.intextReferences = new HashSet<>();
		for (IntextReference intextReference : getIntextReferences()){
			IntextReference newIntextReference = (IntextReference)intextReference.clone();
			result.addIntextReference(newIntextReference);
		}

		return result;
	}
}