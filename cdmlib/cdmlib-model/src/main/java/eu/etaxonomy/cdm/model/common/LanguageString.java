/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

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
@Indexed(index = "eu.etaxonomy.cdm.model.common.LanguageString")
@Audited
public class LanguageString  extends LanguageStringBase implements Cloneable {
	private static final long serialVersionUID = -1502298496073201104L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LanguageString.class);

	public static LanguageString NewInstance(String text, Language language){
		return new LanguageString(text, language);
	}
	
	protected LanguageString() {
		super();
	}
	
	protected LanguageString(String text, Language language) {
		super(text, language);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.CdmBase#toString()
	 */
	@Override
	public String toString() {
		if (text == null){
			return super.toString() + "null";
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
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		LanguageString result = (LanguageString)super.clone();
		return result;
	}
	
}