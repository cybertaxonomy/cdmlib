/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Dec 14, 2018
 */
public abstract class AbstractEllypsisFormatter<T extends CdmBase> implements EllypsisFormatter<T> {

    protected static final String DELIM = " ";
    protected String MORE = " \u2026";

    @Override
    public String ellypsis(T entity, String preserveString) {

        EllipsisData ed = entityEllypsis(entity, preserveString);
        String label = ed.truncated;

        return label;
    }

    protected abstract EllipsisData entityEllypsis(T entity, String filterString);

    protected String stringEllypsis(String text, int maxCharsVisible, int minNumOfWords) {

        if(text.length() < maxCharsVisible){
            return text;
        }

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

    protected String preserveString(String preserveString, String text, Pattern pattern, String textEllipsed) {
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

    protected boolean isEllypsis(String label) {
        return label.contains(MORE);
    }


    protected String titleCacheOnlyEllypsis(String titleCache, int maxCharsVisible, int minNumOfWords) {
        // tokens = titleCache.split("\\s");
        String head = titleCache.substring(0, Math.round(titleCache.length() / 2));
        String tail = titleCache.substring(Math.round(titleCache.length() / 2), titleCache.length());

        head = stringEllypsis(head, maxCharsVisible, minNumOfWords);
        tail = stringEllypsis(StringUtils.reverse(tail), maxCharsVisible, minNumOfWords).replace(MORE, "");
        return head + StringUtils.reverse(tail);
    }


    public void applyAndSplit(LinkedList<EllipsisData> edList, String textpart, String textpartEllypsis) {
        // apply on last element in list
        EllipsisData last = edList.getLast();
        int pos1 = last.original.indexOf(textpart);
        if(pos1 > -1){
            if(pos1 > 0){
                String textPartBefore = last.original.substring(0, pos1);
                if(textPartBefore.matches(".*\\w+.*")){ // eliminate non word clutter
                    edList.add(edList.size() - 1, new EllipsisData(textPartBefore, null)); // to be processed later
                }
            }
            edList.add(edList.size() - 1, new EllipsisData(textpart, textpartEllypsis));
            // replace the original with the part which comes after the matching textpart
            String newOriginal = last.original.substring(pos1 + textpart.length());
            last.original = newOriginal;
            if(StringUtils.isEmpty(last.original)){
                edList.removeLast();
            }
        }
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
