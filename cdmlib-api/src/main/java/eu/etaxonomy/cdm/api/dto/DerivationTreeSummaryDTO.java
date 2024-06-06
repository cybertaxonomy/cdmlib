/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * Provides summary information on media, DNA, molecular data and scans
 * found in the derivation tree including the current {@link SpecimenOrObservationBase}
 * instance.
 *
 * @author pplitzner
 * @since Mar 26, 2015
 */
public class DerivationTreeSummaryDTO implements Serializable {

    private static final long serialVersionUID = 8329871164348514709L;

    private List<Link> specimenScans = new ArrayList<>();
    private List<MolecularData> molecularDataList = new ArrayList<>();
    private List<Link> detailImages = new ArrayList<>();
    private List<UUID> specimenScanUuids = new ArrayList<>();
    private List<UUID> detailImageUuids = new ArrayList<>();

    public List<MolecularData> getMolecularDataList() {
        return molecularDataList;
    }

    public MolecularData addProviderLink(Link providerLink) {
        MolecularData molecularData = new MolecularData(providerLink);
        this.molecularDataList.add(molecularData);
        return molecularData;
    }

    public List<Link> getSpecimenScans() {
        return specimenScans;
    }
    public void addSpecimenScan(URI uri, String linkText){
        specimenScans.add(new Link(uri, linkText));
    }

    public List<Link> getDetailImages() {
        return detailImages;
    }
    public void addDetailImage(URI uri, String motif){
        detailImages.add(new Link(uri, motif));
    }

    public List<UUID> getSpecimenScanUuids() {
        return specimenScanUuids;
    }
    public void addSpecimenScanUuid(UUID uuid){
        specimenScanUuids.add(uuid);
    }

    /**
     * @return the detailImageUuids
     */
    public List<UUID> getDetailImageUuids() {
        return detailImageUuids;
    }

    public void addDetailImageUuid(UUID uuid){
        detailImageUuids.add(uuid);
    }

    public class MolecularData implements Serializable{

        private static final long serialVersionUID = -3509828381796659200L;

        private final Link providerLink;
        private List<ContigFile> contigFiles;

        public MolecularData(Link providerLink) {
            super();
            this.providerLink = providerLink;
        }

        public ContigFile addContigFile(Link contigLink){
            if(contigFiles==null){
                contigFiles = new ArrayList<ContigFile>();
            }
            ContigFile contigFile = new ContigFile(contigLink);
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

        private static final long serialVersionUID = -2577994302931726028L;

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

    public static class Link implements Serializable{

        private static final long serialVersionUID = 6635385359662624579L;

        private String linkText;
        private URI uri;

        public Link(URI uri, String linkText) {
            super();
            this.linkText = linkText;
            this.uri = uri;
        }

        public synchronized String getLinkText() {
            return linkText;
        }
        public synchronized void setLinkText(String linkText) {
            this.linkText = linkText;
        }

        public synchronized URI getUri() {
            return uri;
        }
        public synchronized void setUri(URI uri) {
            this.uri = uri;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((linkText == null) ? 0 : linkText.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

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