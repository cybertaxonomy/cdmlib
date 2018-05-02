/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;


/**
 * The type of authority for an externally loaded entity.
 *
 * @author a.mueller
 * @since 21.09.2017
 */

@XmlEnum
public enum AuthorityType implements IEnumTerm<AuthorityType>{

	/**
	 * The entity is managed externally.
	 */
	@XmlEnumValue("Extern")
	EXTERN(UUID.fromString("9a20f0ac-fe97-4b88-aa08-8d34a6d99359"), "Extern", "EXT", null),

	//0
	/**
	 * The entity has been imported from an external source but is now
	 * managed locally. However, the link to the external source is
	 * still important e.g. to compare the 2 versions.
	 */
	@XmlEnumValue("Local")
	LOCAL(UUID.fromString("47a38db6-b8b0-4fd5-8752-677408fba4f8"), "Local", "LOC", null),

	;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AuthorityType.class);


	private AuthorityType(UUID uuid, String defaultString, String key, AuthorityType parent){
		delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
	}

// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<AuthorityType> delegateVoc;
	private IEnumTerm<AuthorityType> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(AuthorityType.class);
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
    public AuthorityType getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<AuthorityType> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(AuthorityType ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<AuthorityType> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static AuthorityType getByKey(String key){return delegateVoc.getByKey(key);}
    public static AuthorityType getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}


}
