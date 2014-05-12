/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @created 2013-09-09
 *
 */
public enum PreferencePredicate  implements IEnumTerm<PreferencePredicate>{
	NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural Code", "NC"),
	TaxonNodeOrder(UUID.fromString("ce06bd8e-4371-4ee5-8f57-cf23930cfd12"), "TaxonNode order", "TNO"),
	Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test")
	;
	
	
	private PreferencePredicate(UUID uuid, String defaultString, String key){
		this(uuid, defaultString, key, null);
	}
	
	private PreferencePredicate(UUID uuid, String defaultString, String key, PreferenceSubject parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

	// *************************** DELEGATE **************************************/	
	
	private static EnumeratedTermVoc<PreferencePredicate> delegateVoc;
	private IEnumTerm<PreferencePredicate> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(PreferencePredicate.class);
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
    public PreferencePredicate getKindOf() {return delegateVocTerm.getKindOf();}
	
	@Override
    public Set<PreferencePredicate> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(PreferencePredicate ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<PreferencePredicate> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static PreferencePredicate getByKey(String key){return delegateVoc.getByKey(key);}
    public static PreferencePredicate getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}
