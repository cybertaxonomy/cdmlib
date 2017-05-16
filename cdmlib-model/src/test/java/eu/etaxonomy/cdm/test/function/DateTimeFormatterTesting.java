/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.function;

import java.time.Instant;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author a.mueller
 * @date 03.05.2017
 *
 */
public class DateTimeFormatterTesting {
    public static void main (String[] args){
        DateTime dt = new DateTime();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd");

        Instant.now();

        System.out.println(formatter.print(dt));

    }
}
