// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @date 2015-01-19
 *
 */
public class FindByIdentifierDTO<T extends IdentifiableEntity> {

	public class AlternativeIdentifier{
		UUID typeUuid;
		String typeLabel;
		String identifier;
		public AlternativeIdentifier(DefinedTerm identifierType, String identifier) {
			this.typeUuid = identifierType.getUuid();
			this.typeLabel = identifierType.getTitleCache();
			this.identifier = identifier;
		}
		public UUID getTypeUuid() {return typeUuid;}
		public String getTypeLabel() {return typeLabel;}
		public String getIdentifier() {return identifier;}
	}

	public class CdmEntity{
		UUID cdmUuid;
		String titleCache;
		T entity;
		public CdmEntity(UUID cdmUuid, String titleCache, T entity) {
			this.cdmUuid = cdmUuid;
			this.titleCache = titleCache;
			this.entity = entity;
		}
		public UUID getCdmUuid() {return cdmUuid;}
		public String getTitleCache() {return titleCache;}
		public T getEntity() {return entity;}

	}

	private AlternativeIdentifier identifier;

	private CdmEntity cdmEntity;


//	public class IdentifierMapping{
//		DefinedTerm identifierType;
//		String Identifier;
////		AlternativeIdentifier identifier;
//		UUID cdmUuid;
//		T entity;
//
//		public IdentifierMapping(DefinedTerm identifierType, String identifier,
//						UUID cdmUuid, T entity) {
//			this.identifierType = identifierType;
//			Identifier = identifier;
//			this.cdmUuid = cdmUuid;
//			this.entity = entity;
//		}
//	}

//	private List<IdentifierMapping> identifierMappings = new ArrayList<IdentifierMapping>();

	public FindByIdentifierDTO(DefinedTerm identifierType, String identifier, T entity){
		this.identifier = new AlternativeIdentifier(identifierType, identifier);
		this.cdmEntity = new CdmEntity(entity.getUuid(), entity.getTitleCache(), entity);
	}

	public FindByIdentifierDTO(DefinedTerm identifierType, String identifier, UUID entityUuid, String titleCache){
		this.identifier = new AlternativeIdentifier(identifierType, identifier);
		this.cdmEntity = new CdmEntity(entityUuid, null, null);
	}

	public AlternativeIdentifier getIdentifier() {
		return identifier;
	}

	public CdmEntity getCdmEntity() {
		return cdmEntity;
	}


}
