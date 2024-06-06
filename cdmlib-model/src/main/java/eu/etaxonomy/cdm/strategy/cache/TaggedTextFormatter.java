/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Formatter class to create Strings from TaggedText Lists.
 *
 * @author a.mueller
 * @since 09.09.2015
 */
public class TaggedTextFormatter {

    private static final TaggedText WHITESPACE_TAG = TaggedText.NewWhitespaceInstance();

    /**
     * Creates a string from tagged text by concatenating all tags with a whitespace.
     * @param tags
     * @return the concatenated string
     * @see #createString(List, HTMLTagRules)
     */
    public static String createString(List<? extends TaggedText> tags) {
        return createString(tags, null);
    }
    /**
     * Creates a string from tagged text by concatenating all tags with a whitespace.
     * @param tags
     *  The TaggedText elements
     * @param excludes
     *  Set up {@link TagEnum}s to remove from the resulting text
     * @return the concatenated string
     * @see #createString(List, HTMLTagRules)
     */
    public static String createString(List<? extends TaggedText> tags, EnumSet<TagEnum> excludes) {
        StringBuilder result = new StringBuilder();

        boolean isSeparator;
        boolean wasSeparator = true;  //true for start tag
        int index = 0;
        for (index = 0; index < tags.size(); index++){
            TaggedText tag = tags.get(index);
            if(excludes != null && excludes.contains(tag.getType())) {
                continue;
            }
            isSeparator = tag.getType().isSeparator();
            if (! wasSeparator && ! isSeparator ){
                result.append(" ");
            }
            if (index < tags.size() -1 || tag.getType() != TagEnum.postSeparator ){
                result.append(tag.getText());
            }
            wasSeparator = isSeparator;
        }
        return result.toString().trim();
    }

    /**
     * Creates a string from tagged text by concatenating all tags. If no separator tag is defined
     * tags are separated by simple whitespace.
     */
    public  static String createString(List<TaggedText> tagsOrigin, HTMLTagRules htmlRules) {
        if (htmlRules == null){
            return createString(tagsOrigin);
        }
        List<TaggedText> tags = new ArrayList<>(tagsOrigin);

        //add whitespace separators
        int index = 0;
        boolean wasSeparator = true;
        while (index < tags.size()){

            if (! tags.get(index).getType().isSeparator()){
                if (wasSeparator == false){
                    tags.add(index++, WHITESPACE_TAG);
                }else{
                    wasSeparator = false;
                }
            }else{
                wasSeparator = true;
            }
            index++;
        }

        //create String
        StringBuffer result = new StringBuffer();

        Stack<String> htmlStack = new Stack<>();
        for (int i = 0;  i < tags.size(); i++  ){
            TaggedText tag = tags.get(i);
            TaggedText lastTag = (i == 0? null : tags.get(i - 1));
            TaggedText nextTag = (i + 1 >= tags.size() ? null : tags.get(i + 1));
            TagEnum lastType = (lastTag == null ? null : lastTag.getType());
            TagEnum nextType = (nextTag == null ? null : nextTag.getType());

            boolean isSeparator = tag.getType().isSeparator();
            boolean isBlankSeparator = isSeparator && StringUtils.isBlank(tag.getText());

            //compute list of rules (tags)
            SortedSet<String> separatorRules;
            if (isBlankSeparator){
                separatorRules = getCommonRules(htmlRules.getRule(lastTag), htmlRules.getRule(nextTag));
            }else{
                separatorRules = htmlRules.getRule(tag);
            }

            //Close all tags not used anymore and remove all common tags from list of rules
            for (int j = 0 ;  j < htmlStack.size() ; j++){
                String html = htmlStack.get(j);
                if (! separatorRules.contains(html)){
                    closeHtml(result, htmlStack, j);
                    break;
                }else{
                    separatorRules.remove(html);
                }
            }

            //open all tags not yet existing
            if (! isBlankSeparator){
                for (String rule : separatorRules){
                    htmlStack.add(rule);
                    result.append("<" +  rule + ">");
                }
            }

            //add whitespace
            if (!isSeparator && lastType != null && !lastType.isSeparator() && nextType != null){
                result.append(" ");
            }
            result.append(tag.getText());
        }
        closeHtml(result, htmlStack, 0);
        return result.toString();
    }

    /**
     * tries to find a tagged text elements that matches the <code>type</code> and <code>textRegex</code> and
     * returns all preceding elements as sublist.
     */
    public static List<? extends TaggedText> cropAt(List<? extends TaggedText> tags, TagEnum type, String textRegex) {
        int pos = 0;
        for (TaggedText taggedText : tags) {
            if(type.equals(taggedText.getType()) && taggedText.getText().matches(textRegex)) {
                break;
            }
            pos++;
        }
        return tags.subList(0, pos);
    }


    private static void closeHtml(StringBuffer result, Stack<String> htmlStack, int index) {
        while (htmlStack.size() > index){
            String closeHtml = htmlStack.pop();
            result.append("</" +  closeHtml + ">");
        }
    }

    private static SortedSet<String> getCommonRules(SortedSet<String> rules1,SortedSet<String> rules2) {
        SortedSet<String> result = new TreeSet<>();
        for (String str : rules1){
            if (rules2.contains(str)){
                result.add(str);
            }
        }
        return result;
    }
}