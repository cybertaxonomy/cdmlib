/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.agent;

import java.util.Arrays;
import java.util.List;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A formatter to format search strings for {@link AgentBase}s (persons, teams,
 * or institutions). It concatenates all 3 representations (titleCache, abbrevCache,
 * collectorCache), if available. Representations are deduplicated if possible, to
 * keep the result as short as possible.
 *
 * @author muellera
 * @since 05.03.2026
 */
public class AgentSearchFormatter {

    public enum CacheType {
        BIBLIOGRAPHIC_TITLE,
        NOMENCLATURAL_TITLE,
        COLLECTOR_TITLE;
    }

    private boolean withId = true;

    public static List<CacheType> TITLECACHE_ONLY = Arrays.asList(new CacheType[] {CacheType.BIBLIOGRAPHIC_TITLE});
    public static List<CacheType> NOMENCLATURAL_TITLE_ONLY = Arrays.asList(new CacheType[] {CacheType.NOMENCLATURAL_TITLE});
    public static List<CacheType> COLLECTOR_TITLE_ONLY = Arrays.asList(new CacheType[] {CacheType.COLLECTOR_TITLE});

    public static List<CacheType> TITLECACHE_FIRST = Arrays.asList(new CacheType[] {CacheType.BIBLIOGRAPHIC_TITLE, CacheType.NOMENCLATURAL_TITLE, CacheType.COLLECTOR_TITLE});
    public static List<CacheType> NOMENCLATURAL_TITLE_FIRST = Arrays.asList(new CacheType[] {CacheType.NOMENCLATURAL_TITLE, CacheType.BIBLIOGRAPHIC_TITLE, CacheType.COLLECTOR_TITLE});
    public static List<CacheType> COLLECTOR_TITLE_FIRST = Arrays.asList(new CacheType[] {CacheType.COLLECTOR_TITLE, CacheType.BIBLIOGRAPHIC_TITLE, CacheType.NOMENCLATURAL_TITLE});

    public static List<CacheType> TITLECACHE_AND_NOM_TITLE = Arrays.asList(new CacheType[] {CacheType.BIBLIOGRAPHIC_TITLE, CacheType.NOMENCLATURAL_TITLE});
    public static List<CacheType> NOMENCLATURAL_TITLE_AND_TITLECACHE = Arrays.asList(new CacheType[] {CacheType.NOMENCLATURAL_TITLE, CacheType.BIBLIOGRAPHIC_TITLE});


// ***************************** FACTORY ****************************************/

    private static AgentSearchFormatter instance;

    private static AgentSearchFormatter instance_without_id;

    public static AgentSearchFormatter INSTANCE() {
        if (instance == null) {
            instance = new AgentSearchFormatter(true);
        }
        return instance;
    }

    public static AgentSearchFormatter INSTANCE_WITHOUT_ID() {
        if (instance_without_id == null) {
            instance_without_id = new AgentSearchFormatter(false);
        }
        return instance_without_id;
    }


//******************************* CONSTRUCTOR *************************************/

    private AgentSearchFormatter(boolean withId) {
        this.withId = withId;
    }

//****************************** FORMAT **********************************************/

    public String format(AgentBase<?> agent){
        return format(agent, null);
    }

    public String format(AgentBase<?> agent, List<CacheType> cacheTypes){

        if (cacheTypes == null){
            cacheTypes = TITLECACHE_FIRST;
        }

        agent = CdmBase.deproxy(agent);
        String titleCache = agent.getTitleCache();
        String abbrevTitleCache = agent instanceof TeamOrPersonBase ? ((TeamOrPersonBase<?>)agent).getNomenclaturalTitleCache() : null;
        String collectorTitleCache = agent instanceof TeamOrPersonBase ? ((TeamOrPersonBase<?>)agent).getCollectorTitleCache() : null;

        String[] caches = new String[cacheTypes.size()];
        for (int i = 0; i < cacheTypes.size(); i++) {
            caches[i] = cacheByCacheType(cacheTypes.get(i), titleCache, abbrevTitleCache, collectorTitleCache);
        }

        String result = CdmUtils.concatWithDedup(" - ", caches);
        if (CdmUtils.isBlank(result)) {
            result = titleCache;
        }

        if (withId) {
            result += " ["+agent.getId()+ "]";
        }

        //from TaxEditor AgentLabelProvider.AgentLabelProvider  not yet implemented
//        if (element instanceof EntityDTOBase){
//            titleCache += "(" + ((IdentifiedEntityDTO)element).getIdentifier().getTypeLabel() +": " + ((IdentifiedEntityDTO)element).getIdentifier().getIdentifier() + ")";
//        }

        return result;

    }

    private String cacheByCacheType(CacheType cacheType, String titleCache, String abbrevTitleCache,
            String collectorTitleCache) {
        switch(cacheType){
            case NOMENCLATURAL_TITLE:
                return abbrevTitleCache;
            case COLLECTOR_TITLE:
                return collectorTitleCache;
            case BIBLIOGRAPHIC_TITLE:
            default:
                return titleCache;
        }
    }

}
