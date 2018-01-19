package eu.etaxonomy.cdm.cache;

public class CdmClientCacheException extends RuntimeException {
	
	public CdmClientCacheException(String message) {
		super(message);
	}

	public CdmClientCacheException(Exception e) {
		super(e);
	}
}
