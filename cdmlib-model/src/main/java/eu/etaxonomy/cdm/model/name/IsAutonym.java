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

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * A tri-state boolean to indicate if a {@link TaxonName} is an autonym or not.
 * Only relevant for infraspecific taxa below rank of subspecies.
 * See #10924
 *
 * @author muellera
 * @since 11.05.2026
 */
public enum IsAutonym implements IEnumTerm<IsAutonym> {

    @XmlEnumValue("INDETERMINATE")
    INDETERMINATE(UUID.fromString("bf33c5a0-1dbb-454a-ba05-e3c69c622e2e"), "Indeterminate", "I"),

    @XmlEnumValue("SELECTED")
    SELECTED(UUID.fromString("18844a0a-e499-43cc-b5e4-cdefc0a095c6"), "Selected", "S"),

    @XmlEnumValue("UNSELECTED")
    UNSELECTED(UUID.fromString("45549c4d-be7f-4b0a-a138-777dae3b35c4"), "Unselected", "U"),

    ;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private IsAutonym(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<IsAutonym> delegateVoc;
    private IEnumTerm<IsAutonym> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(IsAutonym.class);
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
    public IsAutonym getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<IsAutonym> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(IsAutonym ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<IsAutonym> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static IsAutonym getByKey(String key){return delegateVoc.getByKey(key);}
    public static IsAutonym getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
