/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * @author a.kohlbecker
 * @since Feb 23, 2018
 *
 */
public class SpringProxyBeanHelper {


    @SuppressWarnings({"unchecked"})
    public static <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
      if (AopUtils.isJdkDynamicProxy(proxy)) {
        return (T) ((Advised)proxy).getTargetSource().getTarget();
      } else {
        return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
      }
    }
}
