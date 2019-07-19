/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.IPreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceResolver;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Service API for CDM Preferences.
 *
 * STATE: UNDER CONSTRUCTION
 *
 * @author a.mueller
 * @since 03.06.2016
 */
public interface IPreferenceService {




 // ********************* GETTING **********************/


    /**
     * Return the number of all existing cdm preferences
     * @return
     */
    public long count();

     /**
      * Returns all CDM preferences.
      * @return
      */
     public List<CdmPreference> list();

     /**
     * Returns all matching preferences for the given predicate. Use
     * {@link #find(PrefKey)} to find the best matching preference
     * or use {@link PreferenceResolver} to resolve the best matching
     * preference on client side.
     * @param predicate
     * @return
     */
    public List<CdmPreference> list(IPreferencePredicate<?> predicate);

     /**
     * Retrieve all matching values for the given preference key.
     * @param subject the {@link PreferenceSubject} represented as string
     * @param predicate the predicate to retrieve
     * @return
     */
     public List<CdmPreference> list(String subject, String predicate);


     /**
      * Retrieve the best matching value for the given preference key.
      * @param key the key defining the data to retrieve
      * @return
      */
     public CdmPreference find(PrefKey key);

    /**
     * Retrieve the preference that has a key exactly matching the given key.
     * @param key
     * @return
     */
    public CdmPreference findExact(PrefKey key);


  // Can not yet be created as we allow only PreferencePredicate for predicate key creation now.
//     /**
//      * Retrieve the best matching value for the given preference key.
//      * @param subject
//      * @param predicate
//      * @return
//      */
//     public Object find(String subject, String predicate);

// Can not yet be created as we allow only PreferencePredicate for predicate key creation now.
//     /**
//      * Retrieve the database wide preference for the given predicate.
//      * @param key
//      * @return
//      */
//     public CdmPreference findDatabase(String predicate);

     /**
      * Retrieve the database wide preference for the given predicate.
      * @param predicate
      * @return
      */
     public CdmPreference findDatabase(IPreferencePredicate<?> predicate);

     /**
      * Retrieve the vaadin wide preference for the given predicate.
      * @param predicate
      * @return
      */
     public CdmPreference findVaadin(IPreferencePredicate<?> predicate);


     /**
      * Retrieve the TaxEditor wide preference for the given predicate.
      * @param predicate
      * @return
      */
     public CdmPreference findTaxEditor(IPreferencePredicate<?> predicate);

     /**
      * Returns the best matching preference that matches the given
      * predicate and the taxon node filter. Only DB preferences and
      * preferences defined on a TaxonNode are considered.
      * @param taxonNode
      * @param predicate
      * @return
      */
     public Object find(TaxonNode taxonNode, String predicate);

     public CdmPreference find(TaxonNode taxonNode, IPreferencePredicate<?> predicate);

     /**
      * Returns the best matching preference that matches the given
      * predicate and the filter. Only DB preferences and preferences
      * defined on a TaxonNode are considered.
      *
      * NOTE: This is not yet implemented!
      *
      * @param taxonNode
      * @param predicate
      * @return
      */
     public CdmPreference find(CdmBase taxonNodeRelatedCdmBase, String predicate);




 //******************* SETTING **********************/


     /**
      * Write the value for the preference's key
      * @param preference
      */
     public void set(CdmPreference preference);


    /**
     * @param preference
     */
    void remove(PrefKey preference);



     //we need to decide if we want to keep this method
//     //returns old value
//     String setCdmPrefs(CdmBase cdmBase, String predicate, String value );
//
////     String setEditorPrefs();




}
