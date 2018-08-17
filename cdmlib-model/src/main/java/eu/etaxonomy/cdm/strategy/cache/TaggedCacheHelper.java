/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache;

import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * @author a.mueller
 * @since 09.09.2015
 *
 */
public class TaggedCacheHelper {

    /**
     * Creates a string from tagged text by concatenating all tags with a whitespace.
     * @param tags
     * @return the concatenated string
     * @see #createString(List, HTMLTagRules)
     */
    public static String createString(List<TaggedText> tags) {
        StringBuilder result = new StringBuilder();

        boolean isSeparator;
        boolean wasSeparator = true;  //true for start tag
        for (TaggedText tag: tags){
            isSeparator = tag.getType().isSeparator();
            if (! wasSeparator && ! isSeparator ){
                result.append(" ");
            }
            result.append(tag.getText());
            wasSeparator = isSeparator;
        }
        return result.toString().trim();
    }


    /**
     * Creates a string from tagged text by concatenating all tags. If no separator tag is defined
     * tags are seperated by simple whitespace.
     * @param tags
     * @return
     */
    public  static String createString(List<TaggedText> tags, HTMLTagRules htmlRules) {
        if (htmlRules == null){
            return createString(tags);
        }
        //add whitespace separators
        int index = 0;
        boolean wasSeparator = true;
        while (index < tags.size()){

            if (! tags.get(index).getType().isSeparator()){
                if (wasSeparator == false){
                    tags.add(index++, TaggedText.NewWhitespaceInstance());
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

        Stack<String> htmlStack = new Stack<String>();
        for (int i = 0;  i < tags.size(); i++  ){
            TaggedText tag = tags.get(i);
            TagEnum thisType = tag.getType();
            TagEnum lastType = (i == 0? null : tags.get(i - 1).getType());
            TagEnum nextType = (i + 1 >= tags.size() ? null : tags.get(i + 1).getType());

            boolean isSeparator = tag.getType().isSeparator();
//            boolean lastEqual = tag.getType().equals(lastType);
//            boolean nextEqual = tag.getType().equals(nextType);
//            boolean bothEqual = lastEqual && nextEqual;

            //compute list of rules (tags)
            SortedSet<String> separatorRules;
            if (isSeparator){
                separatorRules = getCommonRules(htmlRules.getRule(lastType), htmlRules.getRule(nextType));
            }else{
                separatorRules = htmlRules.getRule(thisType);
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
            if (! isSeparator){
                for (String rule : separatorRules){
                    htmlStack.add(rule);
                    result.append("<" +  rule + ">");
                }
            }

            //add whitespace
            if (lastType != null && ! lastType.isSeparator() && ! isSeparator && nextType != null){
                result.append(" ");
            }
            result.append(tag.getText());
        }
        closeHtml(result, htmlStack, 0);
        return result.toString();
    }


    private static void closeHtml(StringBuffer result, Stack<String> htmlStack, int index) {
        while (htmlStack.size() > index){
            String closeHtml = htmlStack.pop();
            result.append("</" +  closeHtml + ">");
        }
    }

    private static SortedSet<String> getCommonRules(SortedSet<String> rules1,SortedSet<String> rules2) {
        SortedSet<String> result = new TreeSet<String>();
        for (String str : rules1){
            if (rules2.contains(str)){
                result.add(str);
            }
        }
        return result;
    }
}
