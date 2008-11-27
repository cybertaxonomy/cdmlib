package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

public class Datasource {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Datasource.class);

	private void test(){
		System.out.println("Start Datasource");

		System.out.println("\nEnd Datasource");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		Datasource cc = new Datasource();
    	cc.test();
	}

}
