// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author pplitzner
 * @date Mar 26, 2015
 *
 */
public class DerivateDataDTO implements Serializable{

    private static final long serialVersionUID = -658838085193175924L;

    private List<Pair<String, String>> specimenScans;
    private List<MolecularData> molecularDataList;
    private List<Pair<String, String>> detailImages;
    private List<UUID> specimenScanUuids;
    private List<UUID> detailImageUuids;

    /**
     * @return the molecularData
     */
    public List<MolecularData> getMolecularDataList() {
        return molecularDataList;
    }

    public MolecularData addProviderLink(URI uri, String linkText) {
        if(this.molecularDataList==null){
            molecularDataList = new ArrayList<MolecularData>();
        }
        MolecularData molecularData = new MolecularData(new Link(uri, linkText));
        this.molecularDataList.add(molecularData);
        return molecularData;
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

    public void addDetailImage(String uri, String motif){
        if(detailImages==null){
            detailImages = new ArrayList<Pair<String,String>>();
        }
        detailImages.add(new Pair<String, String>(uri, motif));
    }

    /**
     * @return the specimenScanUuids
     */
    public List<UUID> getSpecimenScanUuids() {
        return specimenScanUuids;
    }

    public void addSpecimenScanUuid(UUID uuid){
        if(specimenScanUuids==null){
            specimenScanUuids = new ArrayList<UUID>();
        }
        specimenScanUuids.add(uuid);
    }

    /**
     * @return the detailImageUuids
     */
    public List<UUID> getDetailImageUuids() {
        return detailImageUuids;
    }

    public void addDetailImageUuid(UUID uuid){
        if(detailImageUuids==null){
            detailImageUuids = new ArrayList<UUID>();
        }
        detailImageUuids.add(uuid);
    }

    public class MolecularData implements Serializable{

        private static final long serialVersionUID = -7862757780862001282L;

        private final Link providerLink;
        private List<ContigFile> contigFiles;

        public MolecularData(Link providerLink) {
            super();
            this.providerLink = providerLink;
        }

        public ContigFile addContigFile(URI uri, String linkText){
            if(contigFiles==null){
                contigFiles = new ArrayList<ContigFile>();
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

    public class ContigFile implements Serializable{

        private static final long serialVersionUID = 2042117606325543621L;

        private final Link contigLink;
        private List<Link> primerLinks;

        public ContigFile(Link contigLink) {
            this.contigLink = contigLink;
        }

        public void addPrimerLink(URI uri, String linkText){
            if(primerLinks==null){
                primerLinks = new ArrayList<Link>();
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

    public class Link implements Serializable{

        private static final long serialVersionUID = 1905858106647665462L;

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
