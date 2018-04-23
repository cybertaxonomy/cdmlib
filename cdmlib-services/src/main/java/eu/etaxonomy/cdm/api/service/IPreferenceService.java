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
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
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
    public int count();

     /**
      * Returns all CDM preferences.
      * @return
      */
     public List<CdmPreference> list();


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
      * @param key
      * @return
      */
     public CdmPreference findDatabase(PreferencePredicate predicate);

     /**
      * Retrieve the vaadin wide preference for the given predicate.
      * @param key
      * @return
      */
     public CdmPreference findVaadin(PreferencePredicate predicate);

     /**
      * Returns the best matching preference that matches the given
      * predicate and the taxon node filter. Only DB preferences and
      * preferences defined on a TaxonNode are considered.
      * @param taxonNode
      * @param predicate
      * @return
      */
     public Object find(TaxonNode taxonNode, String predicate);

     public CdmPreference find(TaxonNode taxonNode, PreferencePredicate predicate);

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


     //we need to decide if we want to keep this method
//     //returns old value
//     String setCdmPrefs(CdmBase cdmBase, String predicate, String value );
//
////     String setEditorPrefs();




}
