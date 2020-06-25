/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import org.hibernate.LazyInitializationException;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since 20.10.2010
 *
 */
public class TeamOrPersonBaseBeanProcessor extends AbstractBeanProcessor<TeamOrPersonBase> {

    @Override
    public List<String> getIgnorePropNames() {
        // nothing to ignore by default
        return null;
    }


    @Override
    public JSONObject processBeanSecondStep(TeamOrPersonBase bean, JSONObject json,
            JsonConfig jsonConfig) {
        //NOTE LazyInitializationException related to author teams are often causing problems
        //     so we catch them here in order to avoid breaking the web service response completely
        //     The team should be initialized by the TeamAutoInitializer but due to the bug #7331
        //     this seems to be broken in specific cases.
        //     The below try-catch should be removed once #7331 but by carefully checking the examples
        //     #9095, #9096. Other tickets related to LIEs and eu.etaxonomy.cdm.model.agent.Team.teamMembers
        //     should also be checked.
        try {
            json.element("titleCache", bean.getTitleCache());
        } catch (LazyInitializationException e) {
           logger.error("Caught LazyInitializationException at getTitleCache()", e);
        }
        try {
            json.element("nomenclaturalTitle", bean.getNomenclaturalTitle());
        } catch (LazyInitializationException e) {
            logger.error("Caught LazyInitializationException at getNomenclaturalTitle()", e);
         }
        return json;
    }

}
