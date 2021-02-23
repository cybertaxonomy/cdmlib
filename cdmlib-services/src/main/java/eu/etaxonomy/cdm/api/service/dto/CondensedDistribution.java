/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.text.html.HTML;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * A class representing a condensed distribution stored as as list of {@link TaggedText}
 * with TaggedText being extended by an isBold indicator.
 *
 * The class offers a method representing a string representation which uses {@link HTML}
 * tag < b> to indicate bold text.
 *
 * @author a.kohlbecker
 * @author a.mueller
 * @since Jun 25, 2015
 */
public class CondensedDistribution {

    private List<TaggedText> taggedText = new ArrayList<>();

    public static class DistributionTaggedText extends TaggedText{
        private static final long serialVersionUID = 3904027767908153947L;
        private boolean bold = false;

        public static DistributionTaggedText NewInstance(TagEnum type, String text){
            return new DistributionTaggedText(type, text, false);
        }

        private DistributionTaggedText(TagEnum type, String text, boolean bold) {
            super(type, text);
            this.bold = bold;
        }
        public boolean isBold() {
            return bold;
        }
        public void setBold(boolean bold) {
            this.bold = bold;
        }

        @Override
        public SortedSet<String> htmlTags(){
            SortedSet<String> result = super.htmlTags();
            if (bold){
                result.add("b");
            }
            return result;
        }
    }

    public void addStatusAndAreaTaggedText(String status, String area, boolean bold) {
        addTaggedText(TagEnum.symbol, status, false);
        if (CdmUtils.isNotBlank(status) && CdmUtils.isNotBlank(area)){
            addTaggedText(TagEnum.separator,"",false);
        }
        addTaggedText(TagEnum.label, area, bold);
    }

    public void addSeparatorTaggedText(String sep){
        addTaggedText(TagEnum.separator, sep, false);
    }
    public void addSeparatorTaggedText(String sep, boolean bold) {
        addTaggedText(TagEnum.separator, sep, bold);
    }

    /**
     * @see TagEnum#postSeparator
     */
    public void addPostSeparatorTaggedText(String sep){
        addTaggedText(TagEnum.postSeparator, sep, false);
    }

    public void addTaggedText(TagEnum type, String text, boolean bold) {
        if (CdmUtils.isNotBlank(text)|| type.isSeparator() ){
            DistributionTaggedText taggedText = DistributionTaggedText.NewInstance(type, text);
            taggedText.bold = bold;
            this.taggedText.add(taggedText);
        }
    }

    /**
     * @return <code>true</code> if no tagged text was added yet (the inner tagged text list is still empty).
     */
    public boolean isEmpty() {
        return this.taggedText.isEmpty();
    }

    public String getHtmlText(){
        return toString();
    }

//***************** STRING **************************************/

    @Override
    public String toString(){
        HTMLTagRules htmlTagRules = new HTMLTagRules();
        htmlTagRules.setIncludeSingleInstanceHtml(true);
        return TaggedCacheHelper.createString(this.taggedText, htmlTagRules);
    }
}