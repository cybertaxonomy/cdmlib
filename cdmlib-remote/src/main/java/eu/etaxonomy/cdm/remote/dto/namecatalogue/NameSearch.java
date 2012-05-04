package eu.etaxonomy.cdm.remote.dto.namecatalogue;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

/**
 * The class representing the response from the CDM Remote Web Service API to a single name search query.
 * All information contained in this class originates from a call to {@link TaxonNameBase taxon names}
 * <P>
 * 
 * @author c.mathew
 * @version 1.0
 * @created 17-Apr-2012 13:00:43
 */


public class NameSearch implements RemoteResponse {
	
	private NameSearch.NameSearchRequest request;
	private List<NameSearch.NameSearchResponse> response;
	
	
 public NameSearch() {		
		this.response = new ArrayList<NameSearch.NameSearchResponse>();
	}
	
	public void setRequest(String q) {
		request = new NameSearchRequest();
		request.setQuery(q);
	}
	
	public NameSearchRequest getRequest() {
		return this.request;
	}
	
	public void addToResponseList(String name, String nameUuid, Set<TaxonBase> taxonBases) {
		
		NameSearch.NameSearchResponse res = responseWithName(name);
		if(res == null) {
			res = new NameSearch.NameSearchResponse();
			res.setName(name);				
			response.add(res);
		} 		
		res.addToNameUuids(nameUuid);
		Iterator it = taxonBases.iterator();
		System.out.println("TB Size : " + taxonBases.size());	
		while(it.hasNext()) {
			TaxonBase tb = (TaxonBase)it.next();
			res.addToTaxonUuids(tb.getUuid().toString());
		}					
	}
	
	public List<NameSearch.NameSearchResponse> getResponse() {
		return this.response;
	}
	
	private NameSearch.NameSearchResponse responseWithName(String name) {
		for(NameSearch.NameSearchResponse nsres : response) {
			if(nsres.getName().trim().equals(name.trim())) {
				return nsres;
			}
		}
		return null;
	}

	public class NameSearchRequest {
		private String query;
		public NameSearchRequest() {
			this.query = "";
		}
		
		public void setQuery(String q) {
			this.query = q;
		}
		
		public String getQuery() {
			return this.query;
		}
	}
	
	public class NameSearchResponse {
		private String name;
		private Set<String> nameUuids;
		private Set<String> taxonUuids;
		
		public NameSearchResponse() {
			name = "";
			nameUuids = new HashSet<String>();
			taxonUuids = new HashSet<String>();
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void addToNameUuids(String nameUuid) {
			nameUuids.add(nameUuid);
		}
		
		public Set<String> getNameUuids() {
			return this.nameUuids;
		}
		
		public void addToTaxonUuids(String taxonUuid) {
			taxonUuids.add(taxonUuid);
		}
		
		public Set<String> getTaxonUuids() {
			return this.taxonUuids;
		}
	}
	
}
	


