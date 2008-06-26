/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@Entity
public class HybridRelationshipType extends RelationshipTermBase<HybridRelationshipType> {
  
	static Logger logger = Logger.getLogger(HybridRelationshipType.class);

	private static final UUID uuidFirstParent = UUID.fromString("83ae9e56-18f2-46b6-b211-45cdee775bf3");
	private static final UUID uuidSecondParent = UUID.fromString("0485fc3d-4755-4f53-8832-b82774484c43");
	private static final UUID uuidFemaleParent = UUID.fromString("189a3ed9-6860-4943-8be8-a1f60133be2a");
	private static final UUID uuidMaleParent = UUID.fromString("8b7324c5-cc6c-4109-b708-d49b187815c4");

	
	public HybridRelationshipType() {
		super();
	}
	public HybridRelationshipType(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev, symmetric, transitive);
	}


	public static final HybridRelationshipType getbyUuid(UUID uuid){
		return (HybridRelationshipType) findByUuid(uuid);
	}

	public static final HybridRelationshipType FIRST_PARENT(){
		return getbyUuid(uuidFirstParent);
	}

	public static final HybridRelationshipType SECOND_PARENT(){
		return getbyUuid(uuidSecondParent);
	}

	public static final HybridRelationshipType FEMALE_PARENT(){
		return getbyUuid(uuidFemaleParent);
	}

	public static final HybridRelationshipType MALE_PARENT(){
		return getbyUuid(uuidMaleParent);
	}

}