package eu.etaxonomy.cdm.test.function;

import java.util.UUID;


/**
 * @author a.babadshanjan
 * @created 30.10.2008
 */
public class TestUUID {
	
	private static int NBR_OF_UUIDS = 6;
	
	public static void generateRandomUUID(int nbr) {

		System.out.println("Generating " + nbr + " UUID(s)");
		
		for (int i = 0; i < nbr; i++) {
			
			UUID uuid = UUID.randomUUID();
			int j = i + 1;
			System.out.println("UUID #" + j + " = " + uuid);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		generateRandomUUID(NBR_OF_UUIDS);
	}

}
