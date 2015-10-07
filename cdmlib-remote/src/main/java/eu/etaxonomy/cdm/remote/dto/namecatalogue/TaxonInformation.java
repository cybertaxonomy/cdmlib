/**
 *
 */
package eu.etaxonomy.cdm.remote.dto.namecatalogue;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;

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

    public void setResponseTaxon(String title,
            String name,
            String rank,
            String taxonStatus,
            Map<String, String> flags,
            Map<String, Map> classificationMap,
            String sourceUrl,
            String sourceDatasetID,
            String sourceDatasetName,
            String accordingTo,
            String modified,
            String lsid) {
        if(response == null) {
            response = new TaxonInformation.TaxonInformationResponse();
        }
        response.setTaxon(title,
                name,
                rank,
                taxonStatus,
                flags,
                classificationMap,
                sourceUrl,
                sourceDatasetID,
                sourceDatasetName,
                accordingTo,
                modified,
                lsid);
    }

    public TaxonInformation.TaxonInformationResponse getResponse() {
        return response;
    }

    public void addToResponseRelatedTaxa(String taxonUuid,
            String title,
            String name,
            String rank,
            String taxonStatus,
            String relationshipType,
            String sourceUrl,
            String sourceDatasetID,
            String sourceDatasetName,
            String accordingTo,
            String modified) {
        response.addToRelatedTaxa(taxonUuid,
                title,
                name,
                rank,
                taxonStatus,
                relationshipType,
                sourceUrl,
                sourceDatasetID,
                sourceDatasetName,
                accordingTo,
                modified);
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

        public void setTaxon(String title,
                String name,
                String rank,
                String taxonStatus,
                Map<String, String> flags,
                Map<String, Map> classificationMap,
                String sourceUrl,
                String sourceDatasetID,
                String sourceDatasetName,
                String accordingTo,
                String modified,
                String lsid) {
            this.taxon = new TaxonInformationResponse.TaxonInfo();
            this.taxon.setTitle(title);
            this.taxon.setName(name);
            this.taxon.setRank(rank);
            this.taxon.setTaxonStatus(taxonStatus);
            this.taxon.setFlags(flags);
            this.taxon.setLsid(lsid);
            this.taxon.setClassification(classificationMap);
            SourceInfo source = new SourceInfo();
            source.setUrl(sourceUrl);
            source.setDatasetID(sourceDatasetID);
            source.setDatasetName(sourceDatasetName);
            this.taxon.setSource(source);
            TaxonomicScrutiny scrutiny = new TaxonomicScrutiny();
            scrutiny.setAccordingTo(accordingTo);
            scrutiny.setModified(modified);
            this.taxon.setTaxonomicScrutiny(scrutiny);
        }

        public TaxonInformationResponse.TaxonInfo getTaxon() {
            return this.taxon;
        }

        public void addToRelatedTaxa(String taxonUuid,
                    String title,
                    String name,
                    String rank,
                    String taxonStatus,
                    String relationshipType,
                    String sourceUrl,
                    String sourceDatasetID,
                    String sourceDatasetName,
                    String accordingTo,
                    String modified) {
            RelatedTaxonInfo rti = new RelatedTaxonInfo();
            rti.setTaxonUuid(taxonUuid);
            rti.setTitle(title);
            rti.setName(name);
            rti.setRank(rank);
            rti.setTaxonStatus(taxonStatus);
            rti.setRelationshipType(relationshipType);
            SourceInfo source = new SourceInfo();
            source.setUrl(sourceUrl);
            source.setDatasetID(sourceDatasetID);
            source.setDatasetName(sourceDatasetName);
            rti.setSource(source);
            TaxonomicScrutiny scrutiny = new TaxonomicScrutiny();
            scrutiny.setAccordingTo(accordingTo);
            scrutiny.setModified(modified);
            rti.setTaxonomicScrutiny(scrutiny);
            relatedTaxa.add(rti);
        }

        public List<RelatedTaxonInfo> getRelatedTaxa() {
            return this.relatedTaxa;
        }

        public class TaxonInfo {
            private String title;
            private String name;
            private String rank;
            private String taxonStatus;
            private Map<String, String> flags;
            private Map<String, Map> classification;
            private SourceInfo source;
            private String lsid;
            private TaxonomicScrutiny scrutiny;

            public TaxonInfo() {
                title = "";
                name = "";
                rank = "";
                taxonStatus = "";
                flags = new Hashtable<String,String>();
                classification = null;
                source = null;
                scrutiny = null;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getTitle() {
                return this.title;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return this.name;
            }

            public void setRank(String rank) {
                this.rank = rank;
            }

            public String getRank() {
                return this.rank;
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

            public void setClassification(Map<String, Map> classificationMap) {
                    this.classification = classificationMap;
            }

            public Map<String, Map> getClassification() {
                return this.classification;
            }

            public void setSource(SourceInfo source) {
                this.source = source;
            }

            public SourceInfo getSource() {
                return this.source;
            }

            public void setTaxonomicScrutiny(TaxonomicScrutiny scrutiny) {
                this.scrutiny = scrutiny;
            }

            public TaxonomicScrutiny getTaxonomicScrutiny() {
                return this.scrutiny;
            }

            /**
             * @return the lsid
             */
            public String getLsid() {
                return lsid;
            }

            /**
             * @param lsid the lsid to set
             */
            public void setLsid(String lsid) {
                this.lsid = lsid;
            }
        }

        public class RelatedTaxonInfo {
            String taxonUuid;
            String title;
            String name;
            String rank;
            String taxonStatus;
            SourceInfo source;
            String relationshipType;
            TaxonomicScrutiny scrutiny;

            public RelatedTaxonInfo() {
                taxonUuid = "";
                title =  "";
                name = "";
                rank = "";
                taxonStatus = "";
                source = null;
                relationshipType = "";
                scrutiny = null;
            }

            public void setTaxonUuid(String taxonUuid) {
                this.taxonUuid = taxonUuid;
            }

            public String getTaxonUuid() {
                return this.taxonUuid;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getTitle() {
                return this.title;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return this.name;
            }


            public void setRank(String rank) {
                this.rank = rank;
            }

            public String getRank() {
                return this.rank;
            }

            public void setTaxonStatus(String taxonStatus) {
                this.taxonStatus = taxonStatus;
            }

            public String getTaxonStatus() {
                return this.taxonStatus;
            }

            public void setSource(SourceInfo source) {
                this.source = source;
            }

            public SourceInfo getSource() {
                return this.source;
            }

            public void setRelationshipType(String relationshipType) {
                this.relationshipType = relationshipType;
            }

            public String getRelationshipType() {
                return this.relationshipType;
            }

            public void setTaxonomicScrutiny(TaxonomicScrutiny scrutiny) {
                this.scrutiny = scrutiny;
            }

            public TaxonomicScrutiny getTaxonomicScrutiny() {
                return this.scrutiny;
            }
        }

        public class SourceInfo {
            String url;
            String datasetID;
            String datasetName;

            public SourceInfo() {
                url = "";
                datasetID = "";
                datasetName = "";
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUrl() {
                return this.url;
            }

            public void setDatasetID(String datasetID) {
                this.datasetID = datasetID;
            }

            public String getDatasetID() {
                return this.datasetID;
            }

            public void setDatasetName(String datasetName) {
                this.datasetName = datasetName;
            }

            public String getDatasetName() {
                return this.datasetName;
            }
        }

        public class TaxonomicScrutiny {
            String accordingTo;
            String modified;

            public TaxonomicScrutiny() {
                accordingTo = "";
                modified = "";
            }

            public void setAccordingTo(String accordingTo) {
                this.accordingTo = accordingTo;
            }

            public String getAccordingTo() {
                return this.accordingTo;
            }

            public void setModified(String modified) {
                this.modified = modified;
            }

            public String getModified() {
                return this.modified;
            }
        }
    }

}
