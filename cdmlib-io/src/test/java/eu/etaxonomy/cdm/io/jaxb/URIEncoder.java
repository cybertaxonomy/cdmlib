package eu.etaxonomy.cdm.io.jaxb;

public class URIEncoder {
    public static String encode(String string) {
    	
		return string.replace(" ","%20");
    }
}
