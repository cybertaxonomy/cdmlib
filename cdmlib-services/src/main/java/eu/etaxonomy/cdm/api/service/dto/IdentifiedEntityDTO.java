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
public class IdentifiedEntityDTO<T extends IdentifiableEntity> extends EntityDTOBase<T> {

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

	private AlternativeIdentifier identifier;


	public IdentifiedEntityDTO(DefinedTerm identifierType, String identifier, T entity){
	    super(entity);
	    this.identifier = new AlternativeIdentifier(identifierType, identifier);
	}

	public IdentifiedEntityDTO(DefinedTerm identifierType, String identifier, UUID entityUuid, String titleCache){
	    super(entityUuid, titleCache);
	    this.identifier = new AlternativeIdentifier(identifierType, identifier);
	}

	public AlternativeIdentifier getIdentifier() {
		return identifier;
	}


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + identifier.typeLabel + "; "  + cdmEntity.getTitleCache() + "; " + cdmEntity.cdmUuid +  ")";
    }
}
