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
	NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural code", "name.NC"),
	TaxonNodeOrder(UUID.fromString("ce06bd8e-4371-4ee5-8f57-cf23930cfd12"), "Taxon node order", "taxon.TNO"),
	Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test"),
	NameDetailsView(UUID.fromString("3c4ec5f5-feb5-44a8-8533-c3c3484a6869"), "NameDetailsView", "editor.NDV"),
	IsRedList(UUID.fromString("aaf79c57-b2a9-48a6-b037-fc1b4a75a6e9"), "isRedList", "editor.RL"),
	DeterminationOnlyForFieldUnits(UUID.fromString("91b9224b-6610-4cf1-b3da-d60d6f9d59b1"), "DeterminationOnlyForFieldUnit", "editor.DOFU"),
	ShowCollectingAreasInGeneralSection(UUID.fromString("578a1195-64ce-4dfb-9be9-6f2823288678"), "ShowCollectingAreaInGeneralSection", "editor.SCAGS"),
	ShowTaxonAssociations(UUID.fromString("849c24f9-b62b-4f70-b0a0-1b02182b3433"), "ShowTaxonAssociations", "editor.STA"),
	ShowLifeForm(UUID.fromString("85870e7d-a6a3-4c9b-97d6-eb27e6516860"), "ShowLifeForm", "editor.SLF"),
	AbcdImportConfig(UUID.fromString("65380375-d041-458c-8275-c36cdc1f34df"), "AbcdImportConfig", "import.ABCD"),
	BioCaseProvider(UUID.fromString("bd22c85c-f4e8-4771-ae7b-5750868762c4"), "BioCaseProvider", "import.BP")
	;


    private static final String KEY_PREFIX = "model.";

	private PreferencePredicate(UUID uuid, String defaultString, String key){
		this(uuid, defaultString, key, null);
	}

	private PreferencePredicate(UUID uuid, String defaultString, String modelKey, PreferencePredicate parent){

	    delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, KEY_PREFIX + modelKey, parent);
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
