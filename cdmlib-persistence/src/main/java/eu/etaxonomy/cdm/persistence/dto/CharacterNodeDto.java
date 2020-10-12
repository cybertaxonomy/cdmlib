/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author k.luther
 * @since Oct 9, 2020
 */
public class CharacterNodeDto extends TermNodeDto<Feature> {

    private static final long serialVersionUID = 7635704848569122836L;

    /**
     * @param termDto
     * @param parent
     * @param position
     */
    public CharacterNodeDto(CharacterDto characterDto, TermNodeDto parent, int position, TermTreeDto treeDto) {
        super(characterDto, parent, position, treeDto);
    }



}
