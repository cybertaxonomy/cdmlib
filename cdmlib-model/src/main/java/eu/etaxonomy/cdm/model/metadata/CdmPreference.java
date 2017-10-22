/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;


/**
 * This class may hold all prefrences data for a CDM database.
 * E.g. one may store what the default nomenclatural code is,
 * or which default formatter (cache strategy) to use for a
 * certain class.
 * The structure represents a triple where the first item
 * (subject) defines for which object the given information is valid.
 * The second item (predicate) describes the type of information
 * and the third item (value) represents the actual value.
 *
 *  E.g. for defining a database wide default nomenclatural code
 *  you may define a triple ("database", "eu.etaxonomy.cdm.model.name.NomenclaturalCode", "ICZN").
 *  The set of allowed values and semantics for each combination
 *  is up to implementing classes.
 *  The only restrictions we have is the length of the fields and
 *  the fact that the first two items (subject, predicate) do
 *  create a unique key.
 *
 *  Size of single fields may be enlarged in future versions. "Value" may
 *  become a CLOB.
 *
 * @author a.mueller
 * @created 03.07.2013
 */
@Entity
public final class CdmPreference implements Serializable {
	private static final long serialVersionUID = 4307599154287181582L;

    public static final CdmPreference NewInstance(PreferenceSubject subject, PreferencePredicate predicate, String value){
        return new CdmPreference(subject, predicate, value);
    }

    /**
     * @param predicate
     * @param value
     * @return
     */
    public static CdmPreference NewDatabaseInstance(PreferencePredicate predicate, String value) {
        return new CdmPreference(PreferenceSubject.NewDatabaseInstance(), predicate, value);
    }

    /**
     * @param predicate
     * @param value
     * @return
     */
    public static CdmPreference NewVaadinInstance(PreferencePredicate predicate, String value) {
        return new CdmPreference(PreferenceSubject.NewVaadinInstance(), predicate, value);
    }

    public static PrefKey NewKey(PreferenceSubject subject, PreferencePredicate predicate){
      return new PrefKey(subject, predicate);
    }

//	public static final CdmPreference NewInstance(PreferenceSubjectEnum subject, PreferencePredicate predicate, String value){
//		return new CdmPreference(subject, predicate, value);
//	}
//
//	public static PrefKey NewKey(PreferenceSubjectEnum subject, PreferencePredicate predicate){
//		return new PrefKey(subject, predicate);
//	}


	@EmbeddedId
	private PrefKey key;

	@Column(length=1023)
	private String value;

    //if false, the preference should not be overridden by local preferences,
	//if true existing local preferences override database preferences
	    //and the database preference only defines the default.
    private boolean allowOverride = false;

    @Embeddable
    public static class PrefKey implements Serializable{

        private static final long serialVersionUID = 9019957853773606194L;

        //required syntax:  /([A-Za-z]+\[.*\])?
        //examples /  ,  /TaxonNode[#t10#44#1456#]  for a taxon node preference
        @Column(name="key_subject", length=100) //for now we keep the combined key short as indizes for such keys are very limited in size in some DBMS. Size may be increased later
        private String subject;

        @Column(name="key_predicate", length=100) //for now we keep the combined key short as indizes for such keys are very limited in size in some DBMS. Size may be increased later
        private String predicate;

        //for hibernate use only
        private PrefKey(){}

        private PrefKey(PreferenceSubject subject, PreferencePredicate predicate){
            this(subject.toString(), predicate.getKey());
        }
//      private PrefKey(PreferenceSubjectEnum subject, PreferencePredicate predicate){
//          this(subject.getKey(), predicate.getKey());
//      }

        private PrefKey(String subject, String predicate){
            if (subject == null) {
                throw new IllegalArgumentException("Subject must not be null for preference");
            }
            if (predicate == null) {
                throw new IllegalArgumentException("Predicate must not be null for preference");
            }
            if (subject.length() > 255) {
                throw new IllegalArgumentException("Subject must not be longer then 255 for preference");
            }
            if (predicate.length() > 255) {
                throw new IllegalArgumentException("Predicate must not be longer then 255 for preference");
            }
            if (!subject.matches(PreferenceSubject.ROOT + "(([A-Za-z]+\\[.*\\]|"+PreferenceSubject.VAADIN+")"+PreferenceSubject.SEP+")?")){
                throw new IllegalArgumentException("Subject does not follow the required syntax");
            }


            this.subject = subject;
            this.predicate = predicate;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
            result = prime * result + ((subject == null) ? 0 : subject.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            } else if (obj == null){
                return false;
            }else if (getClass() != obj.getClass()){
                return false;
            }else{
                PrefKey other = (PrefKey) obj;
                return ( predicate.equals(other.predicate) && subject.equals(other.subject));
            }
        }

    }

//****************** CONSTRUCTOR **********************/

	//for hibernate use only
	@SuppressWarnings("unused")
	private CdmPreference(){}


	public CdmPreference(PreferenceSubject subject, PreferencePredicate predicate, String value){
		this.key = new PrefKey(subject, predicate);
		//TODO are null values allowed?		assert predicate != null : "value must not be null for preference";
		if (value != null && value.length() > 1023) {throw new IllegalArgumentException(
				String.format("value must not be longer then 1023 characters for preference. Value = %s", value));
		}
		this.value = value;
	}

    public CdmPreference(PreferenceSubject subject, PreferencePredicate predicate, List<UUID> value){
        this(subject, predicate, uuidListStr(value));
    }

    public CdmPreference(PreferenceSubject subject, PreferencePredicate predicate, UUID value){
        this(subject, predicate, value.toString());
    }

    /**
     * @param value
     * @return
     */
    protected static String uuidListStr(List<UUID> value) {
        String valueStr = "";
        for (UUID uuid : value){
            valueStr = CdmUtils.concat(",",valueStr, uuid.toString());
        }
        return valueStr;
    }


	/**
	 * Constructor.
	 * @param subject must not be null and must not be longer then 255 characters.
	 * @param predicate must not be null and must not be longer then 255 characters.
	 * @param value must not be longer then 1023 characters.
	 */
	public CdmPreference(String subject, String predicate, String value){
		this.key = new PrefKey(subject, predicate);
		//TODO are null values allowed?		assert predicate != null : "value must not be null for preference";
		if (value != null && value.length() > 1023) {throw new IllegalArgumentException(
			String.format("value must not be longer then 1023 characters for preference. Value = %s", value));
		}
		this.value = value;

	}

//************************ GETTER / SETTER ***************************/

	public boolean isDatabasePref(){
	    return PreferenceSubject.ROOT.equals(key.subject);
	}


	/**
	 * @return the subject of the preference
	 */
	public String getSubject() {
		return key.subject;
	}

	/**
	 * @return the predicate of the preference
	 */
	public String getPredicate() {
		return key.predicate;
	}

	/**
	 * @return the value of the preference
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the {@link #getValue() value} as {@link UUID} List.
	 * Throws an exception if the value can not be parsed as UUID list.
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<UUID> getValueUuidList() throws IllegalArgumentException {
	    List<UUID> result = new ArrayList<>();
	    if (StringUtils.isBlank(value)){
	        return result;
	    }
	    String[] splits = getValue().split("[,;]");
	    for (String split : splits ){
	        try {
                if (StringUtils.isBlank(split)){
                    continue; //neglect trailing separators
                }
	            UUID uuid = UUID.fromString(split.trim());
                result.add(uuid);
            } catch (IllegalArgumentException e) {
                throw e;
            }
	    }
	    return result;
    }


//
//  we try to avoid setting of values
//  public void setValue(String value) {
//      this.value = value;
//  }

	public PrefKey getKey() {
		return key;
	}

    public boolean isAllowOverride() {
        return allowOverride;
    }

    public void setAllowOverride(boolean allowOverride) {
        this.allowOverride = allowOverride;
    }

}
