/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * This is helping class to ease the usage of {@link CdmPreference#getSubjectString()
 * subject part} of a {@link CdmPreference} for some typical subjects.
 * Currently only very few non hierarchical basic subjects are supported.
 *
 * For how to use subjects see {@link CdmPreference}. For how to resolve
 * them see {@link PreferenceResolver#resolve(List, PrefKey)}
 *
 * @author a.mueller
 * @since 03.06.2016
 */
public class PreferenceSubject {

    public static final String ROOT = "/";
    public static final String SEP = "/";
    public static final String VAADIN = "vaadin";
    public static final String TAX_EDITOR = "taxeditor";
    public static final String DISTR_EDITOR = "distributionEditor";


    private String subject;

    public static PreferenceSubject NewDatabaseInstance(){
        return new PreferenceSubject(ROOT);
    }

    public static PreferenceSubject NewInstance(Classification classification){
        return NewInstance(classification.getRootNode());
    }

    public static PreferenceSubject NewInstance(TaxonNode taxonNode){
        String result = ROOT + "TaxonNode[" + taxonNode.treeIndex() + "]" + SEP;
        return new PreferenceSubject(result);
    }

    public static PreferenceSubject NewVaadinInstance(){
        return new PreferenceSubject(ROOT +  VAADIN + SEP);
    }

    public static PreferenceSubject NewTaxEditorInstance(){
        return new PreferenceSubject(ROOT +  TAX_EDITOR + SEP);
    }
    public static PreferenceSubject NewDistributionEditorInstance(){
        return new PreferenceSubject(ROOT +  DISTR_EDITOR + SEP);
    }
    public static PreferenceSubject NewInstance(PreferenceSubjectEnum subject){
        return new PreferenceSubject(ROOT +  subject.getKey() + SEP);
    }
    public static PreferenceSubject NewInstance(String subjectString){
        return new PreferenceSubject(subjectString);
    }


// *****************************************************/

    private PreferenceSubject (String subject){
        this.subject = subject;
    }

    /**
     * Adds a sub subject and returns the new PreferenceSubject
     * @param subject
     * @return
     */
    public PreferenceSubject with(PreferenceSubjectEnum subject){
        PreferenceSubject result = new PreferenceSubject(this.subject + subject.getKey() + SEP);
        return result;
    }

    @Override
    public String toString() {
        return subject;
    }

    /**
     * @return
     */
    public List<String> getParts() {
        String[] splits;
        if (ROOT.equals(subject)){
            splits = new String[]{""};
        }else{
            splits = subject.split("/");
        }

        return Arrays.asList(splits);
    }

    /**
     *
     */
    public String getLastPart() {
        List<String> parts = getParts();
        if (parts.isEmpty() || parts.size() == 1 && StringUtils.isBlank(parts.get(0))){
            return "";
        }else{
            return parts.get(parts.size()-1);
        }
    }

    public PreferenceSubject getNextHigher(){
        List<String> parts = getParts();
        if (parts.isEmpty() || parts.size() == 1 && StringUtils.isBlank(parts.get(0))){
            return null;
        }else{
            String subject = CdmUtils.concat(SEP, parts.subList(1, parts.size()-1).toArray(new String[0]));
            return new PreferenceSubject(CdmUtils.concat("", ROOT,subject));
        }
    }

    /**
     * @return
     */
    public boolean isRoot() {
        return ROOT.equals(subject);
    }

    /**
     * @param preference
     * @return
     */
    public static PreferenceSubject fromPreference(CdmPreference preference) {
        PreferenceSubject result = new PreferenceSubject(preference.getKey().getSubject());
        return result;
    }

    /**
     * @param key
     * @return
     */
    public static PreferenceSubject fromKey(PrefKey key) {
        PreferenceSubject result = new PreferenceSubject(key.getSubject());
        return result;
    }
}
