/**
 * 
 */
package eu.etaxonomy.cdm.remote.dto.namecatalogue;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.model.common.RemoteResponse;

/**
 * @author c.mathew
 *
 */
public class TaxonInformation implements RemoteResponse {
	private TaxonInformation.TaxonInformationRequest request;
	private TaxonInformation.TaxonInformationResponse response;
	
	public TaxonInformation() {
		response = null;
	}
	
	public void setRequest(String taxonUuid) {
		request = new TaxonInformation.TaxonInformationRequest();
		request.setTaxonUuid(taxonUuid);
	}
	
	public TaxonInformation.TaxonInformationRequest getRequest() {
		return request;
	}
	
	public void setResponseTaxon(String name, 
			String taxonStatus, 
			Map<String, String> flags,
			Map<String, Map> classificationMap) {
		if(response == null) {
			response = new TaxonInformation.TaxonInformationResponse();
		}		
		response.setTaxon(name, taxonStatus, flags, classificationMap);		
	}
	
	public TaxonInformation.TaxonInformationResponse getResponse() {
		return response;
	}
	
	public void addToResponseRelatedTaxa(String taxonUuid, String name, String taxonStatus, String sourceUrl) {
		response.addToRelatedTaxa(taxonUuid, name, taxonStatus, sourceUrl);
	}
	

	public class TaxonInformationRequest {
		private String taxonUuid;
		
		public TaxonInformationRequest() {
			this.taxonUuid = "";
		}
		public void setTaxonUuid(String taxonUuid) {
			this.taxonUuid = taxonUuid;
		}
		
		public String getTaxonUuid() {
			return this.taxonUuid;
		}
	}
	
	public class TaxonInformationResponse {
		private TaxonInformationResponse.TaxonInfo taxon;
		private List<RelatedTaxonInfo> relatedTaxa;
		
		public TaxonInformationResponse() {			
			relatedTaxa = new ArrayList<RelatedTaxonInfo>();
		}
		
		public void setTaxon(String name, 
				String taxonStatus, 
				Map<String, String> flags,
				Map<String, Map> classificationMap) {
			this.taxon = new TaxonInformationResponse.TaxonInfo();
			this.taxon.setName(name);
			this.taxon.setTaxonStatus(taxonStatus);
			this.taxon.setFlags(flags);
			this.taxon.setClassificationMap(classificationMap);
		}
			
		public TaxonInformationResponse.TaxonInfo getTaxon() {
			return this.taxon;
		}
		
		public void addToRelatedTaxa(String taxonUuid, String name, String taxonStatus, String sourceUrl) {
			RelatedTaxonInfo rti = new RelatedTaxonInfo();
			rti.setTaxonUuid(taxonUuid);
			rti.setName(name);
			rti.setTaxonStatus(taxonStatus);
			rti.setSourceUrl(sourceUrl);		
			relatedTaxa.add(rti);
		}
		
		public List<RelatedTaxonInfo> getRelatedTaxa() {
			return this.relatedTaxa;
		}
		
		public class TaxonInfo {
			private String name;
			private String taxonStatus;
			private Map<String, String> flags;
			private Map<String, Map> classificationMap;
			
			
			public TaxonInfo() {
				name = "";
				taxonStatus = "";
				flags = new Hashtable<String,String>();
				classificationMap = new Hashtable<String,Map>();				
			}
			
			public void setName(String name) {
				this.name = name;
			}
			
			public String getName() {
				return this.name;
			}
			
			public void setTaxonStatus(String taxonStatus) {
				this.taxonStatus = taxonStatus;
			}
			
			public String getTaxonStatus() {
				return this.taxonStatus;
			}
			
			public void setFlags(Map<String, String> flags) {
				if(flags != null) {
					this.flags = flags;
				}
			}
			
			public Map<String, String> getFlags() {
				
				return this.flags;
			}
			
			public void setClassificationMap(Map<String, Map> classificationMap) {
				
				if(classificationMap != null) {
					this.classificationMap = classificationMap;
				}
			}
			
			public Map<String, Map> getClassificationMap() {
				return this.classificationMap;
			}
		}
		
		public class RelatedTaxonInfo {
			String taxonUuid;
			String name;
			String taxonStatus;
			String sourceUrl;
			
			public RelatedTaxonInfo() {
				taxonUuid = "";
				name =  "";
				taxonStatus = "";
				sourceUrl = "";
			}
			
			public void setTaxonUuid(String taxonUuid) {
				this.taxonUuid = taxonUuid;				
			}
			
			public String getTaxonUuid() {
				return this.taxonUuid;
			}
			
			public void setName(String name) {
				this.name = name;
			}
			
			public String getName() {
				return this.name;
			}
			
			public void setTaxonStatus(String taxonStatus) {
				this.taxonStatus = taxonStatus;
			}
			
			public String getTaxonStatus() {
				return this.taxonStatus;
			}
			
			public void setSourceUrl(String sourceUrl) {
				this.sourceUrl = sourceUrl;
			}
			
			public String getSourceUrl() {
				return this.sourceUrl;
			}
		}
	}

}
