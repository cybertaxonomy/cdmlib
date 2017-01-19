/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Initializes the various title and name caches of cdm types.
 * <p>
 * Title caches are constructed by a cascade of methods which are calling other
 * title cache methods. If a more general cache is protected we still need to
 * check if the more special caches are not protected and initialize those.
 * Otherwise unexpected <code>LazyLoadingExceptions</code> may occur. Since this
 * cascade is different in the various name types we must check each of them
 * individually.
 *
 * @author a.kohlbecker
 * @date 30.07.2010
 *
 */
public class TitleAndNameCacheAutoInitializer extends AutoPropertyInitializer<IdentifiableEntity<?>> {


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanAutoInitializer#initialize(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public void initialize(IdentifiableEntity<?> bean) {

        bean = HibernateProxyHelper.deproxy(bean, IdentifiableEntity.class);
        // we will implement a bit of redundancy here in order
        // to avoid too much casting
        if(bean instanceof NonViralName){
            // ---> NonViralName
            NonViralName<?> n = (NonViralName<?>)bean;
            if(!n.isProtectedFullTitleCache())  {
                n.getFullTitleCache();
            } else if(!bean.isProtectedTitleCache()){
                n.getTitleCache();
            } else if(!n.isProtectedNameCache())  {
                // n.getNameCache(); not needed here
                // since this is covered by
                // getTaggedName special case  below
            } else if(!n.isProtectedAuthorshipCache()){
                n.getAuthorshipCache();
            }

            if(!n.isProtectedFullTitleCache() || !bean.isProtectedTitleCache() || !n.isProtectedNameCache()){
                /* getTaggedName special case
                 *
                 * if the name cache already is non null the generateNameCache()
                 * method will not be executed and no initialization of the name cache
                 * cascade will happen, therefore me must call the getTaggedName()
                 * explicitly in order to trigger the cascade. Otherwise a
                 * LazyInitializationException can occur:
                 *   > failed to lazily initialize a collection of role:
                 *   > eu.etaxonomy.cdm.model.name.TaxonNameBase.relationsToThisName
                 * --------------------------------------------
                 * according code snipped from cachestrategy:
                 *  if (nameCache == null){
                 *    this.nameCache = generateNameCache();
                 *  }
                 * --------------------------------------------
                 */
                n.getTaggedName();
            }
        } else if(bean instanceof TaxonNameBase) {
             // ---> TaxonNameBase
            TaxonNameBase<?,?> n = (TaxonNameBase<?,?>)bean;
            if(!n.isProtectedFullTitleCache())  {
                n.getFullTitleCache();
            } else if(!bean.isProtectedTitleCache()){
                n.getTitleCache();
            }
        } else if(bean instanceof TaxonBase)  {
            ((TaxonBase)bean).getTaggedTitle();
        } else if(!bean.isProtectedTitleCache()){
            // ---> all other IdentifiableEntity
            bean.getTitleCache();
        }
    }

    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{


        String result = "";
        if (TaxonNameBase.class.isAssignableFrom(clazz)){
            result += String.format(" LEFT JOIN FETCH %s.rank ", beanAlias);
            result += String.format(" LEFT JOIN FETCH %s.relationsToThisName relTo LEFT JOIN FETCH relTo.relatedFrom ", beanAlias);
            if (NonViralName.class.isAssignableFrom(clazz)){
                result += String.format(" LEFT JOIN FETCH %s.combinationAuthorship ", beanAlias);
                result += String.format(" LEFT JOIN FETCH %s.exCombinationAuthorship ", beanAlias);
                result += String.format(" LEFT JOIN FETCH %s.basionymAuthorship ", beanAlias);
                result += String.format(" LEFT JOIN FETCH %s.exBasionymAuthorship ", beanAlias);
            }
        }
        if (TaxonBase.class.isAssignableFrom(clazz)){
            // TODO with the TaxonBase.getTaggedTtile() this is getting much more complicated
            result += String.format(" JOIN FETCH %s.sec secRef LEFT JOIN FETCH secRef.authorship ", beanAlias);
            // TODO initialize all instances related to the name titleCache, see above
        }

        // throw an exception since LEFT JOIN FETCH is not really working for titleCaches
        // TODO test if the LEFT JOIN FETCHes are at least working for TaxonNameBase and NonViralName
        throw new Exception();

//        return result;
    }


}
