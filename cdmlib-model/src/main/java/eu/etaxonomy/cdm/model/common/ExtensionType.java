/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;


/**
 * Extension types similar to dynamically defined attributes. These are not data
 * types, but rather content types like "DOI", "2nd nomenclatural reference", "3rd
 * hybrid parent" or specific local identifiers.
 * @author m.doering
 * @created 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionType")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class ExtensionType extends DefinedTermBase<ExtensionType> {
	private static final long serialVersionUID = -7761963794004133427L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExtensionType.class);

	private static final UUID uuidXmlFragment = UUID.fromString("ea109c1c-e69b-4e6d-9079-1941b9ee2991");
	private static final UUID uuidRdfFragment = UUID.fromString("f3684e25-dcad-4c1e-a5d8-16cddf1c4f5b");
	private static final UUID uuidDoi = UUID.fromString("f079aacc-ab08-4cc4-90a0-6d3958fb0dbf");
//	private static final UUID uuid2ndNomRef = UUID.fromString("46a98bfa-f11a-47fe-a6c5-50c7e8289b3d");
	private static final UUID uuid3rdHybridParent = UUID.fromString("5e552b24-5a2d-498d-a4f4-ccd8e5bc2bae");
	private static final UUID uuidAreaOfInterest = UUID.fromString("cefa478e-604f-4db4-8afc-25e06c28ec69");
	private static final UUID uuidNomStandard = UUID.fromString("4a6cbbe9-8d79-4d15-b316-2ff1adeff526");
	private static final UUID uuidAbbreviation = UUID.fromString("5837e34e-b0f5-4736-8083-ff5eaecd8c43");
	private static final UUID uuidOrder = UUID.fromString("ecb7770d-a295-49ee-a88f-e9e137a7cabb");
	private static final UUID uuidInformalCategory = UUID.fromString("11bbc52f-a085-43d3-9f9b-cbe0d1eb9a91");
	private static final UUID uuidUrl = UUID.fromString("d769fa9f-51ee-4e11-8152-b5ce72b7f413");

	protected static Map<UUID, ExtensionType> termMap = null;

	/**
	 * Creates a new empty extension type instance.
	 *
	 * @see #NewInstance(String, String, String)
	 */
	public static ExtensionType NewInstance() {
		return new ExtensionType();
	}

	/**
	 * Creates a new extension type instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new extension type to be created
	 * @param	label  		 the string identifying the new extension type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new extension type to be created
	 * @see 				 #readCsvLine(List, Language)
	 * @see 				 #NewInstance()
	 */
	public static ExtensionType NewInstance(String term, String label, String labelAbbrev){
		return new ExtensionType(term, label, labelAbbrev);
	}

//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected ExtensionType() {
		super(TermType.ExtensionType);
	}

	/**
	 * Class constructor: creates a new extension type instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new extension type to be created
	 * @param	label  		 the string identifying the new extension type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new extension type to be created
	 * @see 				 #ExtensionType()
	 */
	protected ExtensionType(String term, String label, String labelAbbrev) {
		super(TermType.ExtensionType, term, label, labelAbbrev);
	}

//************************** METHODS *******************************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	protected static ExtensionType getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
	        return getTermByClassAndUUID(ExtensionType.class, uuid);
	    } else {
	        return termMap.get(uuid);
	    }
	}


	public static final ExtensionType XML_FRAGMENT(){
		return getTermByUuid(uuidXmlFragment);
	}

	public static final ExtensionType RDF_FRAGMENT(){
		return getTermByUuid(uuidRdfFragment);
	}

	public static final ExtensionType DOI(){
		return getTermByUuid(uuidDoi);
	}


//	public static final ExtensionType SECOND_NOM_REF(){
//		return getTermByUuid(uuid2ndNomRef);
//	}


	public static final ExtensionType THIRD_HYBRID_PARENT(){
		return getTermByUuid(uuid3rdHybridParent);
	}

	public static final ExtensionType AREA_OF_INTREREST(){
		return getTermByUuid(uuidAreaOfInterest);
	}

	public static final ExtensionType NOMENCLATURAL_STANDARD(){
		return getTermByUuid(uuidNomStandard);
	}

	public static final ExtensionType ABBREVIATION(){
		return getTermByUuid(uuidAbbreviation);
	}

	public static final ExtensionType ORDER(){
		return getTermByUuid(uuidOrder);
	}

	public static final ExtensionType INFORMAL_CATEGORY(){
		return getTermByUuid(uuidInformalCategory);
	}

	public static final ExtensionType URL(){
		return getTermByUuid(uuidUrl);
	}


	@Override
	protected void setDefaultTerms(TermVocabulary<ExtensionType> termVocabulary) {
		termMap = new HashMap<UUID, ExtensionType>();
		for (ExtensionType term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
