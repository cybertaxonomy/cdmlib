/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

/**
 * This class is an instantiatable class for the base class {@link LanguageStringBase}.
 * No further functionality is added.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LanguageString")
@XmlRootElement(name = "LanguageString")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.LanguageString")
@Audited
public class LanguageString  extends LanguageStringBase implements Cloneable, IIntextReferencable {
	private static final long serialVersionUID = -1502298496073201104L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LanguageString.class);

	//TODO do we need to add it to JAXB? #4706
	@XmlElementWrapper(name = "IntextReferences", nillable = true)
	@XmlElement(name = "IntextReference")
	@OneToMany(mappedBy="languageString", fetch=FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
//	@Merge(MergeMode.ADD_CLONE)
    private Set<IntextReference> intextReferences = new HashSet<IntextReference>();

	//*************** INTEXT REFERENCE **********************************************

	@Override
    public Set<IntextReference> getIntextReferences(){
		return this.intextReferences;
	}
    private void setIntextReferences(Set<IntextReference> intextReferences){
        this.intextReferences = intextReferences;
    }


    public IntextReference addIntextReference(IIntextReferenceTarget target, String start, String inner, String end){
        return IntextReferenceHelper.addIntextReference(target, this, start, inner, end);
    }
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
			intextReference.setReferencedEntity(null);
		}
	}



//********************* FACTORY *******************************************/

	public static LanguageString NewInstance(String text, Language language){
		return new LanguageString(text, language);
	}

// ********************* CONSTRUCTOR ********************************/

	protected LanguageString() {
		super();
	}

	protected LanguageString(String text, Language language) {
		super(text, language);
	}


//*************** TO STRING ***********************************/
	@Override
	public String toString() {
		if (text == null){
			return super.toString() + ":null";
		}else{
			String languagePart = "";
			if (this.language != null){
				languagePart = "(" + this.language.toString() + ")";
			}
			if (text.length() > 20){
				return text.substring(0, 20) + "..." + languagePart;
			}else{
				return text + languagePart;
			}
		}
	}

// ************************ CLONE ********************************/
	@Override
	public Object clone() throws CloneNotSupportedException {
		LanguageString result = (LanguageString)super.clone();
		result.setIntextReferences(new HashSet<IntextReference>());

		for (IntextReference ref : this.intextReferences) {
		    IntextReference newRef = (IntextReference)ref.clone();
		    result.addIntextReference(newRef);
		}

		return result;
	}

}
