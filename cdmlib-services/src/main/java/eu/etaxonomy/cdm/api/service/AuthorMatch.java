/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.common.NameMatchingUtils;

/**
 * @author andreabee90
 * @since 21.11.2023
 */
public class AuthorMatch {

    public static List<SingleNameMatchingResult> compareAuthor (List<SingleNameMatchingResult> resultList, String authorshipQuery, Integer distance) {
        List<SingleNameMatchingResult> result = new ArrayList<>();
        authorNormalization(resultList);
        AuthorMatch.etal(authorshipQuery);
        NameMatchingUtils.replaceSpecialCharacters(authorshipQuery);
        for (int i = 0 ; i < resultList.size(); i++) {
            int distanceAuthorComparison = NameMatchingUtils.modifiedDamerauLevenshteinDistance(authorshipQuery, resultList.get(i).getAuthorshipCache());
            resultList.get(i).setDistance(distanceAuthorComparison+resultList.get(i).getDistance());
        }
        for (int i = 0 ; i < resultList.size(); i++) {
            if (resultList.get(i).getDistance() <= distance) {
                result.add(resultList.get(i));
            }
        }

        return result;
    }

    private static void authorNormalization(List<SingleNameMatchingResult> results) {
        int size = results.size();
        for (int i = 0; i < size; i++) {
            String author = results.get(i).getAuthorshipCache();
            AuthorMatch.etal(author);
            NameMatchingUtils.replaceSpecialCharacters(author);
        }
    }

    public static String etal(String authorshipCache) {
        String etal = "et al";
        String and = "and";

        if (authorshipCache.contains(and) || authorshipCache.contains(etal)) {
            authorshipCache.replace(and, "&");
            authorshipCache.replace(etal, "&");
        }
        return authorshipCache;
    }
}