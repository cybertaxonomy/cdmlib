/**
* Copyright (C) 2017 EDIT
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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 *
 * @author a.mueller
 * @date 13.03.2017
 *
 */
@XmlEnum
public enum RegistrationStatus implements IEnumTerm<RegistrationStatus>{

    /**
     * A new record which is being edited by the Author
     */
    @XmlEnumValue("Preparation")
    PREPARATION(UUID.fromString("032c4f0c-af74-4f10-82f9-0652209bfeac"), "Preparation","PREP"),

    /**
     * A record ready for the curator to be validated.
     */
    @XmlEnumValue("Curation")
    CURATION(UUID.fromString("4b8f972e-06a2-423c-827e-6477a2a9d371"), "Curation","CUR"),

    /**
     * The record has passed the validation by the curator and is ready for publication.
     */
    @XmlEnumValue("Ready")
    READY(UUID.fromString("6fe00e6d-56fc-4150-a1ef-7a47ea764f18"), "Ready","RDY"),

    /**
     * The name or typification has finally been published.
     */
    @XmlEnumValue("Published")
    PUBLISHED(UUID.fromString("9bcc6bea-424a-4bfe-b901-2efeea116c2c"), "Unknown unit type","PUB"),

    /**
     * The registration has been rejected, the process is aborted and the record is preserved.
     */
    @XmlEnumValue("Rejected")
    REJECTED(UUID.fromString("0960e14a-a6b1-41b4-92a9-0d6d1b9e0159"), "Unknown unit type","REJ"),
    ;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RegistrationStatus.class);


    private RegistrationStatus(UUID uuid, String defaultString, String key){
        this(uuid, defaultString, key, null);
        System.out.println("SpecimenOrObservationType hierarchie not yet fully implemented");
    }

    @SuppressWarnings("unchecked")
    private RegistrationStatus(UUID uuid, String defaultString, String key, SpecimenOrObservationType parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

    // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<RegistrationStatus> delegateVoc;
    private IEnumTerm<RegistrationStatus> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(RegistrationStatus.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}


    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public RegistrationStatus getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<RegistrationStatus> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(RegistrationStatus ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<RegistrationStatus> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static RegistrationStatus getByKey(String key){return delegateVoc.getByKey(key);}
    public static RegistrationStatus getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


}
