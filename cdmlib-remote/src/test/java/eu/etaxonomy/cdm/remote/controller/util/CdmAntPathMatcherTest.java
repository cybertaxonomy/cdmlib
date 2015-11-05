// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @author a.kohlbecker
 * @date Jul 29, 2014
 *
 */
public class CdmAntPathMatcherTest {


    private static final Logger logger = Logger.getLogger(CdmAntPathMatcherTest.class);

    private final String[][] antPathMatcherExpectations  = new String[][]{
            new String[]{"/hotels",null,"/hotels"},
            new String[]{null,"/hotels","/hotels"},
            new String[]{"/hotels","/bookings","/hotels/bookings"},
            new String[]{"/hotels","bookings","/hotels/bookings"},
            new String[]{"/hotels/*","/bookings","/hotels/bookings"},
            new String[]{"/hotels/**","/bookings","/hotels/**/bookings"},
            new String[]{"/hotels","{hotel}","/hotels/{hotel}"},
            new String[]{"/hotels/*","{hotel}","/hotels/{hotel}"},
            new String[]{"/hotels/**","{hotel}","/hotels/**/{hotel}"},
            new String[]{"/*.html","/hotels.html","/hotels.html"},
            new String[]{"/*.html","/hotels","/hotels.html"},
            new String[]{"/*.html","hotels","hotels.html"},

    };
    private final String[][] cdmAntPathMatcherExpectations  = new String[][]{
            new String[]{"/hotels",null,"/hotels"},
            new String[]{null,"/hotels","/hotels"},
            new String[]{"/hotels","//bookings","/bookings"},                // special for CdmAntPathMatcher
            new String[]{"/hotels","bookings","/hotels/bookings"},
            new String[]{"/hotels/*","bookings","/hotels/bookings"},
            new String[]{"/hotels/*","//bookings","/bookings"},              // special for CdmAntPathMatcher
            new String[]{"/hotels/**","//bookings","/bookings"},             // special for CdmAntPathMatcher
            new String[]{"/hotels/**","bookings","/hotels/**/bookings"},
            new String[]{"/hotels","{hotel}","/hotels/{hotel}"},
            new String[]{"/hotels/*","{hotel}","/hotels/{hotel}"},
            new String[]{"/hotels/**","{hotel}","/hotels/**/{hotel}"},
            new String[]{"/*.html","/hotels.html","/hotels.html"},
            new String[]{"/*.html","hotels","hotels.html"},
            new String[]{"/*.html","//hotels","/hotels"},                   // special for CdmAntPathMatcher
            new String[]{"/*.html","//*.txt","/*.txt"},                     // special for CdmAntPathMatcher
    };


    @Test
    public void testAntPathMatcherExpectations(){
        int i = 0;
        PathMatcher matcher = new AntPathMatcher();
        for(String[] next : antPathMatcherExpectations ){
            try {
                logger.debug(++i + " combine " + next[0] + ", " + next[1] + " = " + next[2]);
                assertEquals( next[2], matcher.combine(next[0], next[1]));
            } catch (IllegalArgumentException e){
                if(next[2].equals("IllegalArgumentException")){
                    /* it its expected,so ignore */
                } else {
                    throw e;
                }
            }
        }
    }

    @Test
    public void testCdmAntPathMatcherExpectations(){
        int i = 0;
        PathMatcher matcher = new CdmAntPathMatcher();
        for(String[] next : cdmAntPathMatcherExpectations ){
            try {
                logger.debug(++i + " combine " + next[0] + ", " + next[1] + " = " + next[2]);
                assertEquals( next[2], matcher.combine(next[0], next[1]));
            } catch (IllegalArgumentException e){
                if(next[2].equals("IllegalArgumentException")){
                    /* it its expected,so ignore */
                } else {
                    throw e;
                }
            }
        }
    }


}
