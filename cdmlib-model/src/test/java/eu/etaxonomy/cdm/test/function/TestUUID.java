/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;

import java.util.UUID;


/**
 * @author a.babadshanjan
 * @created 30.10.2008
 */
public class TestUUID {
	
	private static int NBR_OF_UUIDS = 7;
	
	public static void generateRandomUUID(int nbr) {

		System.out.println("Generating " + nbr + " UUID(s):");
		
		for (int i = 0; i < nbr; i++) {
			
			UUID uuid = UUID.randomUUID();
			//int j = i + 1;
			//System.out.println("UUID #" + j + " = " + uuid);
			System.out.println(uuid);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		generateRandomUUID(NBR_OF_UUIDS);
	}

}
