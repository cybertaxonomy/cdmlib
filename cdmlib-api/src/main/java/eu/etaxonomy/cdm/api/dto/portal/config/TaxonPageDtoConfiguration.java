/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDtoConfiguration implements Serializable {

    private static final long serialVersionUID = -3017154740995350103L;

    //TODO should this be part of the configuration??
    public UUID taxonUuid;

    //data
    boolean withFacts = true;
    boolean withSynonyms = true;
    boolean withSpecimens = true;
    boolean withKeys = true;
    boolean withMedia = true;
    boolean withTaxonNodes = true;

    //synonymy
    //TODO taxonrelations
    //should withSynonyms includeProparte and missapplications
    // => yes as long as there are no specific parameters in dataportals to handle them differently
    boolean withTaxonRelationships = true;
    UUID taxonRelationshipTypeTree = null;

    //facts
    UUID featureTree = null;
    boolean condensedDistribution = false;  //!!
    //TODO CondensedDistributionConfiguration is still in cdmlib-ext
    boolean mapUriParams = true;
    boolean distributionTree = true;

    private DistributionInfoConfiguration distributionInfoConfiguration = new DistributionInfoConfiguration();


    //formatting
    public List<Locale> locales = new ArrayList<>();  //is this data or formatting??
    public boolean formatSec = false;  //!!


// ******************************* GETTER / SETTER ***********************************/

    public DistributionInfoConfiguration getDistributionInfoConfiguration() {
        return distributionInfoConfiguration;
    }
    public void setDistributionInfoConfiguration(DistributionInfoConfiguration distributionInfoConfiguration) {
        this.distributionInfoConfiguration = distributionInfoConfiguration;
    }

}
