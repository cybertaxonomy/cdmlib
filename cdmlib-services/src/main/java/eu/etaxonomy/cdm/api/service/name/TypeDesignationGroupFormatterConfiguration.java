/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * @author muellera
 * @since 23.04.2024
 */
public class TypeDesignationGroupFormatterConfiguration {

    private boolean withCitation = true;
    private boolean withStartingTypeLabel = true;
    private boolean withPrecedingMainType = true;
    private boolean withNameIfAvailable = false;
    private boolean withAccessionNoType = false; //TODO should this be true by default?
    private boolean ignoreSyntypesWithLectotype = false;
    //should become List<Locale>
    private List<Language> languages = new ArrayList<>();
    private EnumSet<OriginalSourceType> sourceTypeFilter = OriginalSourceType.allPublicTypes(); //null means there is no filter, empty means filter all

    private boolean withBrackets = true;  //still unclear if this should become a parameter or should be always true


    //with citation
    public boolean isWithCitation() {
        return withCitation;
    }
    public TypeDesignationGroupFormatterConfiguration setWithCitation(boolean withCitation) {
        this.withCitation = withCitation;
        return this;
    }

    //with starting type label
    public boolean isWithStartingTypeLabel() {
        return withStartingTypeLabel;
    }
    public TypeDesignationGroupFormatterConfiguration setWithStartingTypeLabel(boolean withStartingTypeLabel) {
        this.withStartingTypeLabel = withStartingTypeLabel;
        return this;
    }

    //with name if available
    public boolean isWithNameIfAvailable() {
        return withNameIfAvailable;
    }
    public TypeDesignationGroupFormatterConfiguration setWithNameIfAvailable(boolean withNameIfAvailable) {
        this.withNameIfAvailable = withNameIfAvailable;
        return this;
    }

    //preceding main type
    public boolean isWithPrecedingMainType() {
        return withPrecedingMainType;
    }
    public TypeDesignationGroupFormatterConfiguration setWithPrecedingMainType(boolean withPrecedingMainType) {
        this.withPrecedingMainType = withPrecedingMainType;
        return this;
    }

    //with accession not type
    public boolean isWithAccessionNoType() {
        return withAccessionNoType;
    }
    public TypeDesignationGroupFormatterConfiguration setWithAccessionNoType(boolean withAccessionNoType) {
        this.withAccessionNoType = withAccessionNoType;
        return this;
    }

    //ignore syntypes with lectotype
    public boolean isIgnoreSyntypesWithLectotype() {
        return ignoreSyntypesWithLectotype;
    }
    public TypeDesignationGroupFormatterConfiguration setIgnoreSyntypesWithLectotype(boolean ignoreSyntypesWithLectotype) {
        this.ignoreSyntypesWithLectotype = ignoreSyntypesWithLectotype;
        return this;
    }

    //with brackets
    public boolean isWithBrackets() {
        return withBrackets;
    }
    //do not allow setting for now

    //languages
    public List<Language> getLanguages() {
        return languages;
    }
    public TypeDesignationGroupFormatterConfiguration setLanguages(List<Language> languages) {
        if (languages == null) {
            languages = new ArrayList<>();
        }
        this.languages = languages;
        return this;
    }

    //source types
    public EnumSet<OriginalSourceType> getSourceTypeFilter() {
        return sourceTypeFilter;
    }
    public TypeDesignationGroupFormatterConfiguration setSourceTypeFilter(EnumSet<OriginalSourceType> sourceTypeFilter) {
        this.sourceTypeFilter = sourceTypeFilter;
        return this;
    }
}