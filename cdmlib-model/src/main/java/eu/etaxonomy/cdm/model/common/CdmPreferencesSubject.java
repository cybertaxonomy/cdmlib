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
public enum CdmPreferencesSubject implements IEnumTerm<CdmPreferencesSubject>{
	Database(UUID.fromString("50b25ae6-62fe-46a1-830e-545702e895f7"), "Database", "DB", null),
	Classification(UUID.fromString("1e103ff5-c58f-4e40-8a60-6a36c32c08cf"),"Classification","CL", Database),
	TaxonSubTree(UUID.fromString("2f2c0fd4-9c49-4584-89ba-53f8d201d37c"),"Taxonomic Subtree","TST", Classification)
	;
	
	private CdmPreferencesSubject(UUID uuid, String defaultString, String key, CdmPreferencesSubject parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

	// *************************** DELEGATE **************************************/	
	
	private static EnumeratedTermVoc<CdmPreferencesSubject> delegateVoc;
	private IEnumTerm<CdmPreferencesSubject> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(CdmPreferencesSubject.class);
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
    public CdmPreferencesSubject getKindOf() {return delegateVocTerm.getKindOf();}
	
	@Override
    public Set<CdmPreferencesSubject> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(CdmPreferencesSubject ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<CdmPreferencesSubject> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static CdmPreferencesSubject getByKey(String key){return delegateVoc.getByKey(key);}
    public static CdmPreferencesSubject getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
