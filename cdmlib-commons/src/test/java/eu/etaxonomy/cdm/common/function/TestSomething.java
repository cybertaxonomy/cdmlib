/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.function;

/**
 * @author a.mueller
 * @since 07.06.2021
 */
public class TestSomething {

    public static void main(String[] args){
        try{
            throw new RuntimeException();
        }catch(Exception e){
            for (StackTraceElement ste: e.getStackTrace()){
                System.out.println(ste.getClassName());
                System.out.println(ste.getMethodName());
                System.out.println(e.getMessage());
                System.out.println(e.getClass().getCanonicalName());
            }
            System.out.println(e.getStackTrace());
        }
    }
}
