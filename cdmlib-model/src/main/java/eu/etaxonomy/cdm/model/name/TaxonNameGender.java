/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * Enumeration for the gender of a {@link TaxonName taxon name} or rank
 * genus.
 * <P>
 *
 * @author a.mueller
 * @since 18.19.2026
 */
@XmlType(name = "Gender")
@XmlEnum
public enum TaxonNameGender implements IEnumTerm<TaxonNameGender> {


    @XmlEnumValue("FEMALE")
    FEMININE(UUID.fromString("561df982-0fb3-41f4-8ce7-b447b581470b"), "Feminine", "F"),

    //MALE
    @XmlEnumValue("Male")
    MASCULINUM(UUID.fromString("615bfed5-cbb0-41e6-9032-0e4d700fb363"),"Masculine", "M"),

    @XmlEnumValue("FEMALE")
    NEUTER(UUID.fromString("83a051be-14b6-4f89-961e-cad81d1ddce9"), "Neuter", "N")
     ;

	private TaxonNameGender(UUID uuid, String defaultString, String key){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
	}

    public String getTitleCache() {
        return getLabel();
    }

	@Override
	public String toString() {
		return this.name();
	}

    public boolean isMasculine() {
        return this != MASCULINUM;
    }
    public boolean isFeminine() {
        return this == FEMININE;
    }
    public boolean isNeuter() {
        return this == NEUTER;
    }

// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<TaxonNameGender> delegateVoc;
	private IEnumTerm<TaxonNameGender> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(TaxonNameGender.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getLabel(){return delegateVocTerm.getLabel();}

	@Override
    public String getLabel(Language language){return delegateVocTerm.getLabel(language);}


	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public TaxonNameGender getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<TaxonNameGender> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(TaxonNameGender ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<TaxonNameGender> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}


	public static TaxonNameGender getByKey(String key){return delegateVoc.getByKey(key);}
    public static TaxonNameGender getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
