/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.hibernate.search.DateTimeBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;

/**
 * The class keeps track of versions via a full linked list to different version objects, or a simple updated/updatedBy property in the same object.
 *
 * Full versioning allows concrete subclasses to keep track of previous or later versions of an object.
 * A different version is another (persistent) java object, but with the same UUID.
 * The version history is established as a linked list of the version objects in time.
 * If versioning via the linked list is used, updated/updatedBy is the same as created/createdBy (better NULL?).
 *
 * Versioning can be turned off and in this case this class provides updated/updatedBy to keep track of the latest change event.
 *
 * @author m.doering
 * @created 08-Nov-2007 13:07:01
 *
 * @param <T>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionableEntity", propOrder = {
    "updated",
    "updatedBy"
})
@XmlJavaTypeAdapter(value=DateTimeAdapter.class,type=DateTime.class)
@MappedSuperclass
@Audited
public abstract class VersionableEntity extends CdmBase implements IVersionableEntity{
	private static final long serialVersionUID = 1409299200302758513L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VersionableEntity.class);

	@XmlElement(name ="Updated", type = String.class)
	@XmlJavaTypeAdapter(DateTimeAdapter.class)
	//@XmlElement(name ="Updated")
	//@XmlElement(name ="Updated")
	@Type(type="dateTimeUserType")
	@Basic(fetch = FetchType.LAZY)
	@Match(MatchMode.IGNORE)
	@Field(analyze = Analyze.NO)
	@FieldBridge(impl = DateTimeBridge.class)
	private DateTime updated;

	@XmlElement(name = "UpdatedBy")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch=FetchType.LAZY)
	@Match(MatchMode.IGNORE)
	private User updatedBy;

	@Override
    public User getUpdatedBy(){
		return this.updatedBy;
	}
	@Override
    public void setUpdatedBy(User updatedBy){
		this.updatedBy = updatedBy;
	}

	@Override
    public DateTime getUpdated(){
		return this.updated;
	}
	@Override
    public void setUpdated(DateTime updated){
		this.updated = updated;
	}



//********************** CLONE *****************************************/

	/**
	 * Clones this versionable entity.
	 * Set fields for nextVersion, previousVersion, updated, updatedBy and createdBy are set to <tt>null</tt>
	 * The id is set to 0.
	 * The uuid is created new.
	 * The createdWhen is set to the current date.
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		VersionableEntity result = (VersionableEntity)super.clone();

		result.setUpdated(null);
		result.setUpdatedBy(null);

		//no changes to: -
		return result;
	}


    /**
     * Convenience method to clone a LanguageString map
     * @param oldMap
     * @return
     * @throws CloneNotSupportedException
     */
    protected Map<Language,LanguageString> cloneLanguageString(Map<Language,LanguageString> oldMap) throws CloneNotSupportedException{
        Map<Language,LanguageString> result = new HashMap<>();
        for (Language language : oldMap.keySet()){
            LanguageString newLanguageString = (LanguageString)oldMap.get(language).clone();
            result.put(language, newLanguageString);
        }
        return result;
    }
}
