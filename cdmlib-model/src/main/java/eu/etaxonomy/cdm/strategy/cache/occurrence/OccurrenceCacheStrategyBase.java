/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @since 09.01.2021
 */
public abstract class OccurrenceCacheStrategyBase<T extends DerivedUnit>
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<T>{

    private static final long serialVersionUID = -6044178882022038807L;

    @Override
    public String getTitleCache(T specimen) {
        if (specimen == null){
            return null;
        }
        String result = doGetTitleCache(specimen);
        if (isBlank(result)){
            result = specimen.toString();
        }
        return result;
    }

    protected abstract String doGetTitleCache(T specimen);

    protected String getCollectionAndAccession(T specimen){
        String result = null;
        if (specimen.getCollection() != null){
            Collection collection = specimen.getCollection();
            if (isNotBlank(collection.getCode())){
                result = collection.getCode();
            }
        }
        result = CdmUtils.concat(" ", result, CdmUtils.Ne(specimen.getAccessionNumber()));
        return result;
    }

    protected String truncate(String str, int length){
        if (str == null){
            return null;
        }else{
            if (str.length() > length){
                if (length <3){
                    return str.substring(0, str.length());
                }else{
                    return str.substring(0, str.length()-3)+"...";
                }
            }else{
                return str;
            }
        }
    }
}
