/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author k.luther
 * @since 07.01.2020
 */
public class TermVocabularyDto extends TermCollectionDto {

    private static final long serialVersionUID = 7667822208618658310L;
    /**
     * @param uuid
     * @param representations
     * @param termType
     */
    public TermVocabularyDto(UUID uuid, Set<Representation> representations, TermType termType, String titleCache, boolean isAllowDuplicate, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, termType, titleCache, isAllowDuplicate, isOrderRelevant, isFlat);
        // TODO Auto-generated constructor stub
    }

    public static TermVocabularyDto fromVocabulary(TermVocabulary voc) {
        TermVocabularyDto dto = new TermVocabularyDto(voc.getUuid(), voc.getRepresentations(), voc.getTermType(), voc.getTitleCache(), voc.isAllowDuplicates(), voc.isOrderRelevant(), voc.isFlat());
        return dto;
    }

    public static List<TermVocabularyDto> termVocabularyDtoListFrom(List<Object[]> results) {
        List<TermVocabularyDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations because of LEFT JOIN
        Map<UUID, TermCollectionDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                // multiple results for one voc -> multiple (voc) representation
                if(elements[1]!=null){
                    dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
                }

            } else {
                // voc representation
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                }


                TermVocabularyDto termVocDto = new TermVocabularyDto(
                        uuid,
                        representations,
                        (TermType)elements[2],
                        (String)elements[3],
                        (boolean)elements[4],
                        (boolean)elements[5],
                        (boolean)elements[6]);


                dtoMap.put(uuid, termVocDto);
                dtos.add(termVocDto);
            }
        }
        return dtos;
    }

}
