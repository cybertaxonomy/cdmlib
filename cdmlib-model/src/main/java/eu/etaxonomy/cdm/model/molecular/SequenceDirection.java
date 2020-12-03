/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * A Sequence Direction defines the direction in which a DNA part was read by a {@link Primer}
 * for a {@link SingleRead sequencing process}.
 * This can be either {@link #Forward} or {@link #Reverse}.
 *
 * @author a.mueller
 * @since 2013-07-11
 */
public enum SequenceDirection implements IEnumTerm<SequenceDirection> {
	Forward(UUID.fromString("e611de24-09bf-468f-b6ee-e34124022912"), "Forward", "FWD"),
	Reverse(UUID.fromString("d116fb2c-00e7-46a4-86b4-74c46ca2afa0"), "Reverse", "REV")
	;

    private SequenceDirection(UUID uuid, String defaultString, String key){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, null);
    }

	// *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<SequenceDirection> delegateVoc;
    private IEnumTerm<SequenceDirection> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(SequenceDirection.class);
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
    public SequenceDirection getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<SequenceDirection> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(SequenceDirection ancestor) {return delegateVocTerm.isKindOf(ancestor); }

    @Override
    public Set<SequenceDirection> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static SequenceDirection getByKey(String key){return delegateVoc.getByKey(key);}
    public static SequenceDirection getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}