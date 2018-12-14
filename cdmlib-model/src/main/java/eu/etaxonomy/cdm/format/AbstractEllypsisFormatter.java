/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Dec 14, 2018
 *
 */
public abstract class AbstractEllypsisFormatter<T extends CdmBase> implements EllypsisFormatter<T> {

    private static final String DELIM = " ";
    private String MORE = " \u2026";

    @Override
    public String ellypsis(T entity, String preserveString) {


        EllipsisData ed = entityEllypsis(entity, preserveString);
        String label = ed.truncated;

        return label;
    }

    protected abstract EllipsisData entityEllypsis(T entity, String filterString);

    public String stringEllypsis(String text, int maxCharsVisible, int minNumOfWords) {
        String ellipsedText = "";
        StringTokenizer tokenizer = new StringTokenizer(text, DELIM);
        int wordCount = 0;
        while(tokenizer.hasMoreElements()){
            String token = tokenizer.nextToken();
            if(ellipsedText.length() + token.length() + DELIM.length() <= maxCharsVisible || wordCount < minNumOfWords){
                ellipsedText = ellipsedText + (ellipsedText.isEmpty() ? "" : DELIM) + token;
            } else {
                break;
            }
            wordCount++;
        }
        return ellipsedText + MORE;
    }

    public String preserveString(String preserveString, String text, Pattern pattern, String textEllipsed) {
        String matchingSubstring = null;
        if(!preserveString.isEmpty()){
            Matcher m = pattern.matcher(text);
            if(m.find()){
                matchingSubstring = m.group(1);
            }
        }
        if(matchingSubstring != null && !textEllipsed.toLowerCase().contains(preserveString)){
            textEllipsed += matchingSubstring + MORE;
        }
        return textEllipsed;
    }

    static class EllipsisData {
        String original;
        String truncated;
        /**
         * @param original
         * @param truncated
         */
        public EllipsisData(String original, String truncated) {
            super();
            this.original = original;
            this.truncated = truncated;
        }
    }

}
