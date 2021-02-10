/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.taxon;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A very preliminary first version.
 *
 * @author a.mueller
 * @since 02.02.2021
 */
public class ClassificationDefaultCacheStrategy
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<Classification> {

    private static final long serialVersionUID = -7268632087215822228L;
    final static UUID uuid = UUID.fromString("d59f30ae-0c23-4f5b-8bfb-b3f795fe7b71");

    @Override
    protected UUID getUuid() {
        return uuid;
    }

    public static ClassificationDefaultCacheStrategy NewInstance() {
        return new ClassificationDefaultCacheStrategy();
    }

    private ClassificationDefaultCacheStrategy(){}

    @Override
    public String getTitleCache(Classification classification) {
        if (classification == null){
            return null;
        }else{
            String result = "";
            if (classification.getName() != null){
                result = classification.getName().getText();
            }
            if (isBlank(result) && !classification.getDescription().isEmpty()){
                //TODO get preferred/default representation
                result = StringUtils.truncate(classification.getDescription().values().iterator().next().getText(), 100);
            }
            if (isBlank(result) && classification.getReference() != null){
                result = classification.getReference().getTitleCache();
            }

            //return
            if (isBlank(result)){
                return classification.toString();
            }else{
                return result;
            }
        }
    }
}