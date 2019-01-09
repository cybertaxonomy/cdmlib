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
 * @since 2013-09-09
 *
 */
public enum PreferencePredicate implements IEnumTerm<PreferencePredicate>, IPreferencePredicate<Object>{
    Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test", "Test"),

    //names
    NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural code", "model.name.NC", eu.etaxonomy.cdm.model.name.NomenclaturalCode.ICNAFP),

    //taxonGraph
    TaxonGraphSecRefUuid(UUID.fromString("86ba874c-7491-4f4a-a3b4-aa2d1ea9c411"), "TaxonGraph SecReference Uuid", "model.taxonGraph.secRefUuid", null),

    //taxon
    //TODO needs "taxon" in modelKey as behavior might be different for specimen and future publishable classes
    DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "defaultBehaviourForPublishFlag", null),  //default inherit from parent


    //vaadin + distribution editor
	AvailableDistributionStatus(UUID.fromString("6721599e-686b-460e-9d57-cfd364f4b626"), "Available Distribution Status", "distribution.status.term", null),
	AvailableDistributionAreaVocabularies(UUID.fromString("dd1f35d5-dbf3-426b-9ed3-8b5992cb2e27"), "Available Distribution Area Vocabularies", "distribution.area.voc", null),


    //common name area vocabularies

    CommonNameAreaVocabularies(UUID.fromString("59d68062-b4ff-4c3a-b29d-66bf850c1d82"), "Common Names: Available Vocabularies for Areas of Common Names", "commonname.area.voc", null),
    CommonNameReferencesWithMarker(UUID.fromString("41402495-96a8-47be-9129-cf9b2a4bc189"), "Common Names: Use only References with Common Name Marker", "commonname.reference.useMarked", Boolean.FALSE),

    ;

    private Object defaultValue;

	private PreferencePredicate(UUID uuid, String messageString, String key, Object defaultValue){
		this(uuid, messageString, key, null, defaultValue);
	}

	private PreferencePredicate(UUID uuid, String messageString, String modelKey, PreferencePredicate parent, Object defaultValue){
	    delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, messageString, modelKey, parent);
	    this.defaultValue = defaultValue;

	}

	// *************************** DELEGATE **************************************/

	/**
     * @return the delegateVocTerm
     */
    public IEnumTerm<PreferencePredicate> getDelegateVocTerm() {
        return delegateVocTerm;
    }

    /**
     * @param delegateVocTerm the delegateVocTerm to set
     */
    public void setDelegateVocTerm(IEnumTerm<PreferencePredicate> delegateVocTerm) {
        this.delegateVocTerm = delegateVocTerm;
    }

    private static EnumeratedTermVoc<PreferencePredicate> delegateVoc;
	private IEnumTerm<PreferencePredicate> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(PreferencePredicate.class);
	}


    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

	/**
     * {@inheritDoc}
     */
    @Override
	public String getKey(){return delegateVocTerm.getKey();}

	/**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	/**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}


	/**
     * {@inheritDoc}
     */
    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	/**
     * {@inheritDoc}
     */
    @Override
    public PreferencePredicate getKindOf() {return delegateVocTerm.getKindOf();}

	/**
     * {@inheritDoc}
     */
    @Override
    public Set<PreferencePredicate> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	/**
     * {@inheritDoc}
     */
    @Override
	public boolean isKindOf(PreferencePredicate ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	/**
     * {@inheritDoc}
     */
    @Override
    public Set<PreferencePredicate> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static IPreferencePredicate getByKey(String key){return delegateVoc.getByKey(key);}
    public static IPreferencePredicate getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
