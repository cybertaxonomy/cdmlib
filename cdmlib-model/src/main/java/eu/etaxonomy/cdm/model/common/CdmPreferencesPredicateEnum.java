/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Set;
import java.util.UUID;

/**
 * @author a.mueller
 * @created 2013-09-09
 *
 */
public enum CdmPreferencesPredicateEnum  implements IEnumTerm<CdmPreferencesPredicateEnum>{
	NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural Code", "NC");
	;
	
	
	private CdmPreferencesPredicateEnum(UUID uuid, String defaultString, String key){
		this(uuid, defaultString, key, null);
	}
	
	private CdmPreferencesPredicateEnum(UUID uuid, String defaultString, String key, CdmPreferencesSubject parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

	// *************************** DELEGATE **************************************/	
	
	private static EnumeratedTermVoc<CdmPreferencesPredicateEnum> delegateVoc;
	private IEnumTerm<CdmPreferencesPredicateEnum> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(CdmPreferencesPredicateEnum.class);
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
    public CdmPreferencesPredicateEnum getKindOf() {return delegateVocTerm.getKindOf();}
	
	@Override
    public Set<CdmPreferencesPredicateEnum> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(CdmPreferencesPredicateEnum ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<CdmPreferencesPredicateEnum> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static CdmPreferencesPredicateEnum getByKey(String key){return delegateVoc.getByKey(key);}
    public static CdmPreferencesPredicateEnum getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}
