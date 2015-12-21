package eu.etaxonomy.cdm.remote.dto.namecatalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

public class AcceptedNameSearch implements RemoteResponse {

	private AcceptedNameSearch.Request request;
	private List<AcceptedNameSearch.Response> response;


	public AcceptedNameSearch() {		
		this.response = new ArrayList<AcceptedNameSearch.Response>();
	}

	public void setRequest(String q) {
		request = new Request();
		request.setQuery(q);
	}

	public Request getRequest() {
		return this.request;
	}

	public void addToResponseList(String acceptedName, String authorship, String rank, Map<String, Map> classificationMap) {
        AcceptedNameSearch.Response res =  new AcceptedNameSearch.Response();            
        res.setAcceptedName(acceptedName);
        res.setAuthorship(authorship);
        res.setRank(rank);
        res.setClassification(classificationMap);
        response.add(res);
	}
	
	public List<AcceptedNameSearch.Response> getResponse() {
		return response;
	}
	
    public class Request {
        private String query;
        public Request() {
            this.query = "";
        }

        public void setQuery(String q) {
            this.query = q;
        }

        public String getQuery() {
            return this.query;
        }
    }
	
    public class Response {
        private String acceptedName;       
        private String authorship;      
        private String rank;
		private Map<String, Map> classification;	

        public Response() {
        	acceptedName = "";
        	authorship = "";
        	rank = "";
			classification = null;	
        }

        public void setAcceptedName(String acceptedName) {
            this.acceptedName = acceptedName;
        }

        public String getAcceptedName() {
            return this.acceptedName;
        }
        
        public void setAuthorship(String authorship) {
        	this.authorship = authorship;
        }
        
        public String getAuthorship() {
        	return this.authorship;
        }

        public void setRank(String rank) {
        	this.rank = rank;
        }
        
        public String getRank() {
        	return this.rank;
        }
        
        public void setClassification(Map<String, Map> classificationMap) {
        	this.classification = classificationMap;
        }

        public Map<String, Map> getClassification() {
        	return this.classification;
        }
    }

}
