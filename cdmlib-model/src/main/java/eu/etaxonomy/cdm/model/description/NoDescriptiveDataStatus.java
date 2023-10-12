/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnumValue;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * Enumeration describing why descriptive data are not available.
 * The enumeration values are taken from SDD.
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/2975
 *
 * @author a.mueller
 * @date 27.06.2023
 */
public enum NoDescriptiveDataStatus implements IEnumTerm<NoDescriptiveDataStatus> {

    @XmlEnumValue("NotApllicable")
    NotApllicable(UUID.fromString("d8c7eedd-cd84-41e3-954f-910d73a367b0"), "not applicable","NA"),

    @XmlEnumValue("DataUnavailable")
    DataUnavailable(UUID.fromString("9826baa6-0bdb-4efa-957a-b5d4f0f4d7cc"), "data unavailable","DU"),

    @XmlEnumValue("ToBeIgnored")
    ToBeIgnored(UUID.fromString("47d3a9e6-22b5-4d12-bb53-c8a92afe5e83"), "to be ignored","TBI"),

    @XmlEnumValue("ToBeChecked")
    ToBeChecked(UUID.fromString("52952fe5-cb3d-4ab9-80a9-5cc93587cd0c"), "to be checked","TBC"),

    @XmlEnumValue("NotInterpretable")
    NotInterpretable(UUID.fromString("22305490-8ea5-487c-9068-b90d16045c8c"), "not interpretable","NI"),

    @XmlEnumValue("DataWithheld")
    DataWithheld(UUID.fromString("05b2abcf-0d41-463c-bd83-30a268fa32ae"), "data withheld","DW");

    private NoDescriptiveDataStatus(UUID uuid, String defaultString, String key) {
        this(uuid, defaultString, key, null);
    }
    private NoDescriptiveDataStatus(UUID uuid, String defaultString, String key, NoDescriptiveDataStatus parent) {
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

    // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<NoDescriptiveDataStatus> delegateVoc;
    private IEnumTerm<NoDescriptiveDataStatus> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(NoDescriptiveDataStatus.class);
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
    public NoDescriptiveDataStatus getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<NoDescriptiveDataStatus> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(NoDescriptiveDataStatus ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<NoDescriptiveDataStatus> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static NoDescriptiveDataStatus getByKey(String key){return delegateVoc.getByKey(key);}
    public static NoDescriptiveDataStatus getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}