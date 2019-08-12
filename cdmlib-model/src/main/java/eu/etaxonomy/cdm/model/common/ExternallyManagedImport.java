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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;


/**
 * The type of authority for an externally loaded entity.
 *
 * @author a.mueller
 * @since 21.09.2017
 */
@XmlEnum
public enum ExternallyManagedImport implements IEnumTerm<ExternallyManagedImport>{

	/**
	 * The entity is managed externally.
	 */
	@XmlEnumValue("Extern")
	CDM_TERMS(UUID.fromString("09a5a700-a057-4de8-b932-86ca241c4ca4"), "CDM_Terms", "CDM_Terms", null),

	;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExternallyManagedImport.class);


	private ExternallyManagedImport(UUID uuid, String defaultString, String key, ExternallyManagedImport parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<ExternallyManagedImport> delegateVoc;
	private IEnumTerm<ExternallyManagedImport> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(ExternallyManagedImport.class);
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
    public ExternallyManagedImport getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<ExternallyManagedImport> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(ExternallyManagedImport ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<ExternallyManagedImport> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static ExternallyManagedImport getByKey(String key){return delegateVoc.getByKey(key);}
    public static ExternallyManagedImport getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
