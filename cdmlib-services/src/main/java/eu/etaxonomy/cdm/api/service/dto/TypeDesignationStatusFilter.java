/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;

/**
 *
 * @author a.kohlbecker
 * @since Apr 26, 2019
 *
 */
public class TypeDesignationStatusFilter {

    private Set<TypeDesignationStatusBase> status = new HashSet<>();
    private List<Language> languages;
    private boolean ignoreCase = true;

    public static final TypeDesignationStatusFilter NULL_ELEMENT = new TypeDesignationStatusFilter(null, Arrays.asList(Language.DEFAULT()), true);

    public TypeDesignationStatusFilter(TypeDesignationStatusBase status, List<Language> language, boolean ignoreCase){
        this.languages = language;
        this.status.add(status);
        this.ignoreCase = ignoreCase;

        if(language == null){
            language = Arrays.asList(Language.DEFAULT());
        }
    }

    public void addStatus(TypeDesignationStatusBase status){
        String representation = getLabel();
        if(equalByLabel(status)){
            this.status.add(status);
        } else {
            throw new RuntimeException("You must not add a status with different label");
        }
    }

    public boolean equalByLabel(TypeDesignationStatusBase status){

        String thisRepresentation = getLabel();
        String otherRepresentation = status.getPreferredRepresentation(languages).getLabel();
        if(ignoreCase){
            thisRepresentation = thisRepresentation.toLowerCase();
            otherRepresentation = otherRepresentation.toLowerCase();
        }
        return thisRepresentation.equals(otherRepresentation);
    }

    /**
     * The localized label of the status terms represented by this filter term
     *
     * @return
     */
    public String getLabel() {
        if(null == this.status.iterator().next()){
            return "- none -";
        }
        return this.status.iterator().next().getPreferredRepresentation(languages).getLabel();
    }

    public String getKey() {
        if(ignoreCase){
            return getLabel().toLowerCase();
        } else {
            return getLabel();
        }
    }

    public Collection<UUID> getUuids() {
        List<UUID> uuids = new ArrayList<>();
        for(TypeDesignationStatusBase sb : status){
            uuids.add(sb.getUuid());
        }
        return Collections.unmodifiableList(uuids);
    }

    @Override
    public String toString(){
        return getLabel();
    }

    /**
     * @return
     */
    private boolean isEmpty() {
        return this.status.isEmpty();
    }

    private Set<TypeDesignationStatusBase> status() {
        return status;
    }

    public static Set<TypeDesignationStatusBase> toTypeDesignationStatus(Set<TypeDesignationStatusFilter> filterTerms){
        Set<TypeDesignationStatusBase> statusSet = new HashSet<>();
        for(TypeDesignationStatusFilter f : filterTerms){
            if(f == null){
                statusSet.add(null);
            } else {
                statusSet.addAll(f.status);
            }
        }
        return statusSet;
    }

    public static Set<UUID> toTypeDesignationStatusUuids(Set<TypeDesignationStatusFilter> filterTerms){
        Set<UUID> uuids = new HashSet<>();
        for(TypeDesignationStatusFilter f : filterTerms){
            if(f != null){
                uuids.addAll(f.getUuids());
            } else {
                uuids.add(null);
            }
        }
        return uuids;
    }

}
