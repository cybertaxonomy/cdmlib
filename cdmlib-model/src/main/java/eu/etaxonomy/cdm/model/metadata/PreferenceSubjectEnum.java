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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * UNDER CONSTRUCTION
 *
 * @author a.mueller
 * @since 2013-09-09
 *
 */
public enum PreferenceSubjectEnum implements IEnumTerm<PreferenceSubjectEnum>{
	Database(UUID.fromString("50b25ae6-62fe-46a1-830e-545702e895f7"), "Database", "DB", null),
	Classification(UUID.fromString("1e103ff5-c58f-4e40-8a60-6a36c32c08cf"),"Classification","CL", Database),
	TaxonSubTree(UUID.fromString("2f2c0fd4-9c49-4584-89ba-53f8d201d37c"),"Taxonomic Subtree","TST", Classification),
	TaxEditor(UUID.fromString("b41354b1-ebf7-4259-a3c6-87ef65ba453c"),"Taxonomic Editor","taxeditor",Database),
	DistributionEditor(UUID.fromString("052ca3b2-155e-4b95-a294-82e8af4e9b59"),"Distribution Editor","distributionEditor",Database)
    ;

	private PreferenceSubjectEnum(UUID uuid, String defaultString, String key, PreferenceSubjectEnum parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

	// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<PreferenceSubjectEnum> delegateVoc;
	private IEnumTerm<PreferenceSubjectEnum> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(PreferenceSubjectEnum.class);
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
    public PreferenceSubjectEnum getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<PreferenceSubjectEnum> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(PreferenceSubjectEnum ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<PreferenceSubjectEnum> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static PreferenceSubjectEnum getByKey(String key){return delegateVoc.getByKey(key);}
    public static PreferenceSubjectEnum getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
