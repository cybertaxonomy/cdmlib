package eu.etaxonomy.cdm.api.service.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.envers.tools.Pair;

public class DerivateHierarchyDTO {

	//Filter Flags
	private boolean hasDna;
	private boolean hasDetailImage;

	//Row Attributes
	private String country;
	private String collection;
	private String date;
	private String herbarium;
	private boolean hasType;
	private boolean hasSpecimenScan;

	//Detail pop-down
	private String taxonName;
	private String protologue;
	private String citation;
	private Map<String, List<String>> types;
	private List<Pair<String, String>> specimenScans;
	private List<MolecularData> molecularDataList;
	private List<Pair<String, String>> detailImages;

	/**
     * @return the molecularData
     */
    public List<MolecularData> getMolecularDataList() {
        return molecularDataList;
    }

    public MolecularData addProviderLink(URI uri, String linkText) {
        if(this.molecularDataList==null){
            molecularDataList = new ArrayList<DerivateHierarchyDTO.MolecularData>();
        }
        MolecularData molecularData = new MolecularData(new Link(uri, linkText));
        this.molecularDataList.add(molecularData);
        return molecularData;
    }

    /**
     * @return the hasDna
     */
    public boolean isHasDna() {
        return hasDna;
    }
    /**
     * @param hasDna the hasDna to set
     */
    public void setHasDna(boolean hasDna) {
        this.hasDna = hasDna;
    }
    /**
     * @return the hasDetailImage
     */
    public boolean isHasDetailImage() {
        return hasDetailImage;
    }
    /**
     * @param hasDetailImage the hasDetailImage to set
     */
    public void setHasDetailImage(boolean hasDetailImage) {
        this.hasDetailImage = hasDetailImage;
    }
    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }
    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
    /**
     * @return the collection
     */
    public String getCollection() {
        return collection;
    }
    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * @return the herbarium
     */
    public String getHerbarium() {
        return herbarium;
    }
    /**
     * @param herbarium the herbarium to set
     */
    public void setHerbarium(String herbarium) {
        this.herbarium = herbarium;
    }
    /**
     * @return the hasType
     */
    public boolean isHasType() {
        return hasType;
    }
    /**
     * @param hasType the hasType to set
     */
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }
    /**
     * @return the hasSpecimenScan
     */
    public boolean isHasSpecimenScan() {
        return hasSpecimenScan;
    }
    /**
     * @param hasSpecimenScan the hasSpecimenScan to set
     */
    public void setHasSpecimenScan(boolean hasSpecimenScan) {
        this.hasSpecimenScan = hasSpecimenScan;
    }
    /**
     * @return the taxonName
     */
    public String getTaxonName() {
        return taxonName;
    }
    /**
     * @param taxonName the taxonName to set
     */
    public void setTaxonName(String taxonName) {
        this.taxonName = taxonName;
    }
    /**
     * @return the protologue
     */
    public String getProtologue() {
        return protologue;
    }
    /**
     * @param protologue the protologue to set
     */
    public void setProtologue(String protologue) {
        this.protologue = protologue;
    }
    /**
     * @return the citation
     */
    public String getCitation() {
        return citation;
    }
    /**
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(Map<String, List<String>> types) {
        this.types = types;
    }
    /**
     * @return the types
     */
    public Map<String, List<String>> getTypes() {
        return types;
    }
    public void addTypes(String typeStatus, String accessionNumber){
        if(types==null){
            types = new HashMap<String, List<String>>();
        }
        List<String> list = types.get(typeStatus);
        if(list==null){
            list = new ArrayList<String>();
        }
        list.add(accessionNumber);
        types.put(typeStatus, list);
    }

    /**
     * @param specimenScans the specimenScans to set
     */
    public void setSpecimenScans(List<Pair<String, String>> specimenScans) {
        this.specimenScans = specimenScans;
    }
    /**
     * @return the specimenScans
     */
    public List<Pair<String, String>> getSpecimenScans() {
        return specimenScans;
    }

    public void addSpecimenScan(String uri, String herbarium){
        if(specimenScans==null){
            specimenScans = new ArrayList<Pair<String,String>>();
        }
        specimenScans.add(new Pair<String, String>(uri, herbarium));
    }

    /**
     * @return the detailImages
     */
    public List<Pair<String, String>> getDetailImages() {
        return detailImages;
    }
    /**
     * @param detailImages the detailImages to set
     */
    public void setDetailImages(List<Pair<String, String>> detailImages) {
        this.detailImages = detailImages;
    }

    public void addDetailImage(String uri, String motif){
        if(detailImages==null){
            detailImages = new ArrayList<Pair<String,String>>();
        }
        detailImages.add(new Pair<String, String>(uri, motif));
    }

    public class MolecularData{
        private final Link providerLink;
        private List<ContigFile> contigFiles;

        public MolecularData(Link providerLink) {
            super();
            this.providerLink = providerLink;
        }

        public ContigFile addContigFile(URI uri, String linkText){
            if(contigFiles==null){
                contigFiles = new ArrayList<DerivateHierarchyDTO.ContigFile>();
            }
            ContigFile contigFile = new ContigFile(new Link(uri, linkText));
            contigFiles.add(contigFile);
            return contigFile;
        }

        public synchronized Link getProviderLink() {
            return providerLink;
        }

        public List<ContigFile> getContigFiles() {
            return contigFiles;
        }

    }

    public class ContigFile{
        private final Link contigLink;
        private List<Link> primerLinks;

        public ContigFile(Link contigLink) {
            this.contigLink = contigLink;
        }

        public void addPrimerLink(URI uri, String linkText){
            if(primerLinks==null){
                primerLinks = new ArrayList<DerivateHierarchyDTO.Link>();
            }
            primerLinks.add(new Link(uri, linkText));
        }

        public Link getContigLink() {
            return contigLink;
        }

        public List<Link> getPrimerLinks() {
            return primerLinks;
        }

    }

    public class Link{
        private String linkText;
        private URI uri;

        public Link(URI uri, String linkText) {
            super();
            this.linkText = linkText;
            this.uri = uri;
        }
        /**
         * @return the linkText
         */
        public synchronized String getLinkText() {
            return linkText;
        }
        /**
         * @param linkText the linkText to set
         */
        public synchronized void setLinkText(String linkText) {
            this.linkText = linkText;
        }
        /**
         * @return the uri
         */
        public synchronized URI getUri() {
            return uri;
        }
        /**
         * @param uri the uri to set
         */
        public synchronized void setUri(URI uri) {
            this.uri = uri;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((linkText == null) ? 0 : linkText.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Link other = (Link) obj;
            if (linkText == null) {
                if (other.linkText != null) {
                    return false;
                }
            } else if (!linkText.equals(other.linkText)) {
                return false;
            }
            if (uri == null) {
                if (other.uri != null) {
                    return false;
                }
            } else if (!uri.equals(other.uri)) {
                return false;
            }
            return true;
        }
    }

}
