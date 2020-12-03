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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * This class holds all preference data for a CDM database.
 * E.g. one may store what the default nomenclatural code in a database is,
 * or which formatter (cache strategy) to use in a certain context.
 * <BR><BR>
 * The structure represents a triple where the first item
 * (subject) defines in which context or for which object the given
 * information is valid. The second item (predicate) describes the
 * type of information and the third item (value) represents the actual value.
 * <BR><BR>
 * E.g. given the usecase of formatting terms in a UI.
 * The predicate maybe something like "preferredTermFormatting".
 * While the subject may define the context, e.g. "/" for the project
 * wide default formatting while "/vaadin" may define the formatting
 * used in a vaadin context and "/taxeditor" may define the formatting
 * in TaxEditor context. Even more specific "/taxeditor/detailsview/dropdown"
 * may define the correct formatting in dropdown elements in the detailsview
 * while "/taxeditor/termeditor" may define the correct formatting in the taxeditors
 * term editor.
 * <BR><BR>
 * Another more data centric example from checklists is the preference which distribution status terms
 * should be available for which area. The predicate used here is
 * {@link PreferencePredicate#AvailableDistributionStatus} for both preferences. The preference
 * which defines the default list of distribution status terms uses "/" as subject and
 * a list of UUIDs as value.
 * <BR><BR>
 * The preference defining the specific distribution status terms only available for the
 * top level area, e.g. "Euro+Med" uses the subject "/NamedArea[111bdf38-7a32-440a-9808-8af1c9e54b51]/"
 * (with 111bd... being the UUID of the top level area) and another UUID list as value.
 * <BR><BR>
 * Subjects by convention should be defined hierarchical following a slash syntax like
 * "/level1/level2/level3". For how to resolve a subject see also
 * {@link PreferenceResolver#resolve(List, PrefKey)}.<BR>
 * Generally the subject is String based but clients may implement classes or enumerations
 * to ease the hierarchical handling of subjects. With {@link PreferenceSubject} there is already
 * a very basic implementation to allow to distinguish project wide, vaadin and taxeditor subjects.
 * This may be further developed in future.
 * <BR><BR>
 * If doubt exists on best practice for how to use subjects please contact the author.
 * <BR>
 * <BR>
 *  The set of allowed values and semantics for each combination
 *  will be defined by convention  over time and by implementing classes.
 *  The only real restrictions we have is the length of the fields and
 *  the fact that the first two items (subject, predicate) must be unique
 *  key in the given database.
 *
 *  Size of single fields may be enlarged in future versions.
 *
 * @author a.mueller
 * @since 03.07.2013
 */
@Entity
public final class CdmPreference implements Serializable {

    private static final String STRING_LIST_SEPARATOR = "[,;\\s]";

    private static final int VALUE_LENGTH = 65536; //= CdmBase.CLOB_LENGTH;

    private static final long serialVersionUID = 4307599154287181582L;

    public static final CdmPreference NewInstance(PreferenceSubject subject,
            IPreferencePredicate<?> predicate, String value){
        return new CdmPreference(subject, predicate, value);
    }

    public static final CdmPreference NewInstance(PrefKey key, String value){
        return new CdmPreference(key.subject, key.predicate, value);
    }

    public static final CdmPreference NewInstance(PreferenceSubject subject, IPreferencePredicate<?> predicate, List<UUID> value){
        return new CdmPreference(subject, predicate, uuidListStr(value));
    }
    public static final CdmPreference NewInstance(PreferenceSubject subject, IPreferencePredicate<?> predicate, UUID ... value){
        return new CdmPreference(subject, predicate, uuidListStr(Arrays.asList(value)));
    }

    public static final CdmPreference NewInstance(PreferenceSubject subject, IPreferencePredicate<?> predicate, UUID value){
        return new CdmPreference(subject, predicate, value.toString());
    }

    public static CdmPreference NewDatabaseInstance(IPreferencePredicate<?> predicate, String value) {
        return new CdmPreference(PreferenceSubject.NewDatabaseInstance(), predicate, value);
    }

    public static CdmPreference NewVaadinInstance(IPreferencePredicate<?> predicate, String value) {
        return new CdmPreference(PreferenceSubject.NewVaadinInstance(), predicate, value);
    }

    public static CdmPreference NewTaxEditorInstance(IPreferencePredicate<?> predicate, String value) {
        return new CdmPreference(PreferenceSubject.NewTaxEditorInstance(), predicate, value);
    }

    public static PrefKey NewKey(PreferenceSubject subject, IPreferencePredicate<?> predicate){
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

	@Column(length=VALUE_LENGTH)
	@Lob
	private String value;

    //if false, the preference should not be overridden by local preferences,
	//if true existing local preferences override database preferences
	    //and the database preference only defines the default.
    private boolean allowOverride = true;

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

        private PrefKey(PreferenceSubject subject, IPreferencePredicate<?> predicate){
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
            if (!(subject.matches(PreferenceSubject.ROOT + "(([A-Za-z]+(\\[.*\\])?|"+PreferenceSubject.VAADIN+")"+PreferenceSubject.SEP+")*")
                    || subject.matches(PreferenceSubject.ROOT + "(([A-Za-z]+(\\[.*\\])?|"+PreferenceSubject.TAX_EDITOR+")"+PreferenceSubject.SEP+")*")
                    )){
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

        public String getSubject() {
            return subject;
        }

        public String getPredicate() {
            return predicate;
        }
    }

//****************** CONSTRUCTOR **********************/

	//for hibernate use only
	@SuppressWarnings("unused")
	private CdmPreference(){}


	private CdmPreference(PreferenceSubject subject, IPreferencePredicate<?> predicate, String value){
		this.key = new PrefKey(subject, predicate);
		checkValue(value);
		this.value = value;
	}


	/**
	 * Constructor.
	 *
	 * @param subject must not be null and must not be longer then 255 characters.
	 * @param predicate must not be null and must not be longer then 255 characters.
	 * @param value must not be longer then 1023 characters.
	 */
	public CdmPreference(String subject, String predicate, String value){
		this.key = new PrefKey(subject, predicate);
		checkValue(value);
		this.value = value;

	}

    private void checkValue(String value) {
        //TODO are null values allowed?     assert predicate != null : "value must not be null for preference";
        if (value != null && value.length() > VALUE_LENGTH -1 ) {
		    throw new IllegalArgumentException(
		            String.format("Preference value must not be longer then "+VALUE_LENGTH+" characters for preference. Value = %s", value));
		}
    }

//************************ GETTER / SETTER ***************************/

	public boolean isDatabasePref(){
	    return PreferenceSubject.ROOT.equals(key.subject);
	}

	/**
	 * @return the subject of the preference as String.
	 */
	public String getSubjectString() {
		return key.subject;
	}
	/**
     * @return the subject of the preference
     */
    public PreferenceSubject getSubject() {
        return PreferenceSubject.fromKey(key);
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
	    for (String split : splitStringListValue()){
            UUID uuid = UUID.fromString(split.trim());
            result.add(uuid);
	    }

	    return result;
    }

	/**
	 * Splits the <code>value</code> into tokens by the separators defined in {@link #STRING_LIST_SEPARATOR}
	 *
	 * @return
	 */
    public  List<String> splitStringListValue() {
        List<String> tokens;
        if (StringUtils.isNotBlank(value)){
	        tokens = Arrays.stream(getValue().split(STRING_LIST_SEPARATOR)).filter(t -> !StringUtils.isBlank(t)).collect(Collectors.toList());
	    } else {
	        tokens = new ArrayList<>();
	    }
        return tokens;
    }

    protected static String uuidListStr(List<UUID> value) {
        String valueStr = "";
        for (UUID uuid : value){
            valueStr = CdmUtils.concat(",",valueStr, uuid.toString());
        }
        return valueStr;
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
