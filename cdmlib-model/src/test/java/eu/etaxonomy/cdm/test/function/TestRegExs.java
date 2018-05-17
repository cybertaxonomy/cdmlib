/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;


/**
 * @author a.babadshanjan
 * @since 30.10.2008
 */
public class TestRegExs {
	
	private static int NBR_OF_UUIDS = 7;
	
	public static void generateRandomUUID(int nbr) {

		String x = "ESPÈCE·TYPE:";
		x = "ESPÈCE TYPE:";
		String re = "(?i)^Esp[\u00E8\u00C8]ce[·\\-\\s]type:$";
		System.out.println(x.matches(re));
		
//		"(?i)^Espèce[·\\-\\s]type\\:$"
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		generateRandomUUID(NBR_OF_UUIDS);
	}

}
