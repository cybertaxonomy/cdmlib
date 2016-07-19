// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.util.Map;

import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;

/**
 * @author k.luther
 * @date 15.07.2016
 *
 */
public abstract class SpecimenImportConfiguratorBase<STATE extends SpecimenImportStateBase, InputStream>  extends ImportConfiguratorBase<STATE, InputStream> {

    private boolean ignoreImportOfExistingSpecimen;
    private boolean reuseExistingTaxaWhenPossible;
    private Map<String, Team> teams;
    private Map<String, Person> persons;
    private boolean ignoreAuthorship;
    private boolean removeCountryFromLocalityText;
    private OccurenceQuery query ;

    /**
     * @param transformer
     */
    public SpecimenImportConfiguratorBase(IInputTransformer transformer) {
        super(transformer);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return
     */
    public boolean isReuseExistingTaxaWhenPossible() {

        return reuseExistingTaxaWhenPossible;
    }

    /**
     * @param titleCacheTeam
     */
    public void setTeams(Map<String, Team> titleCacheTeam) {
       this.teams  = titleCacheTeam;

    }

    /**
     * @param titleCachePerson
     */
    public void setPersons(Map<String, Person> titleCachePerson) {
        this.persons = titleCachePerson;
    }

    /**
     * @return
     */
    public boolean isIgnoreAuthorship() {
        return ignoreAuthorship;
    }

    /**
     * @return
     */
    public boolean isRemoveCountryFromLocalityText() {
        return removeCountryFromLocalityText;
    }

    public boolean isIgnoreImportOfExistingSpecimens(){
        return ignoreImportOfExistingSpecimen;
    }

    public OccurenceQuery getOccurenceQuery(){
        return query;
    }

    public void setOccurenceQuery(OccurenceQuery query){
        this.query = query;
    }

}
