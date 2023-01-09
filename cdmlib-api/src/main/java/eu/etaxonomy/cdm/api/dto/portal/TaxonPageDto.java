/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.List;

/**
 * A DTO including all data relevant for a CDM Dataportal taxon page.
 *
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDto extends TaxonBaseDto {

    public HomotypicGroupDTO homotypicSynonyms;

    public ContainerDto<TaxonNodeDTO> taxonNodes;

    public ContainerDto<HomotypicGroupDTO> heterotypicSynonyms;

    public ContainerDto<FactualDataDTO> factualData;

    public ContainerDto<KeyDTO> keys;

    public ContainerDto<SpecimenDTO> specimens;

    public ContainerDto<MediaDTO> media;

    public List<MessagesDto> messages;

//******************** subclasses *********************************/

    public class TaxonNodeDTO extends CdmBaseDto {

    }

    public class HomotypicGroupDTO  extends CdmBaseDto{

        ContainerDto<TaxonBaseDto> synonyms;   //Synonym has no relevant extra information therefore no more specific DTOS

        //TODO
        //typification   //see also TypeDesignationWorkingSet implementation

    }

    public class SpecimenDTO extends CdmBaseDto{

    }

    public class FactualDataDTO extends CdmBaseDto{

    }

    public class KeyDTO extends CdmBaseDto{

    }

    public class MediaDTO extends CdmBaseDto{

    }




}
