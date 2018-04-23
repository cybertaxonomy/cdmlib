/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.pager;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;

/**
 * @see also class <code>eu.etaxonomy.cdm.remote.controller.util.PagerParameter</code>
 *
 * @author a.kohlbecker
 * @since Dec 10, 2012
 *
 */
public class PagerUtils {

    /**
     * Returns the start index for the given page size and page index values
     * by multiplying both values (startFor = pageSize * pageIndex).<BR>
     * If page size is <code>null</code>, either <code>null</code> is returned
     * or an exception is thrown if page index is > 0.
     * If page index is null it is assumed as 0.
     *
     * @param pageSize the page size
     * @param pageIndex the page index
     * @return the start index
     * @throws PagerRangeException if pageSize == null and pageIndex > 0
     */
    public static Integer startFor(Integer pageSize, Integer pageIndex) throws PagerRangeException {
        if(pageIndex == null) {
            pageIndex = 0;
        }
        if(pageSize == null){
            if (pageIndex > 0 ){
                throw new PagerRangeException("Page index must be undefined or 0 for undefined page size but was " + pageIndex);
            }else{
                return null;
            }
        }
        return pageSize *  pageIndex;
    }

    /**
     * Returns the limit value as used in SQL/HQL statements for the given
     * page size.<BR>
     * Earlier, for <code>null</code> the value 0 was returned but as
     * it was decided that pageSize = <code>null</code> should not set any limitation
     * it was changed to return value <code>null</code>. This way the current implementation
     * is the identity function and has no effect except for documentation.
     * <code>null</code> should be handled in dao-s or specialized code such as
     * {@link #pageList(List, Integer, Integer)}.
     *
     * @param pageSize
     * @return
     */
    public static Integer limitFor(Integer pageSize) {
        if(pageSize == null){
            return null;
        }else{
            return pageSize;
        }
    }

    /**
     * The original method {@link AbstractPagerImpl#hasResultsInRange(Long, Integer, Integer)} is
     * hard to find, therefore this method has been wired also into this Utility class
     * this this is looked up more frequently.
     *
     * @param numberOfResults
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public static boolean hasResultsInRange(Long numberOfResults, Integer pageIndex, Integer pageSize) {
        return AbstractPagerImpl.hasResultsInRange(numberOfResults, pageIndex, pageSize);
    }

    /**
     * Returns a synchronized (sub)list of fullList which contains only the items
     * for the given page
     * @param fullList
     * @param pageIndex page index
     * @param pageSize page size
     * @return a synchronized list holding the page items
     */
    public  static  <T extends Object> List<T> pageList(List<T> fullList, Integer pageIndex, Integer pageSize ){
        Integer start = startFor(pageSize, pageIndex);
        if (start == null){
            return fullList;
        }
        Integer limit = limitFor(pageSize); //no effect and never null at this point due to startFor semantics
        limit = Math.min(Integer.MAX_VALUE - start, limit);

        if (fullList.size() > start) {
            Integer toIndex = Math.min(fullList.size(), start + limit);
            return fullList.subList(start, toIndex);
        }else{
            return fullList.subList(fullList.size(), fullList.size()); //empty list
        }
    }

}
