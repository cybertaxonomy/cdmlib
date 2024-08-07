/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistribution;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * This service has been extracted from cdmlib-ext IEditGeoService
 * to allow access to composeDistributionInfoFor also from within
 * service layer. This was needed e.g. by TaxonPageDtoLoader.
 *
 * @author a.mueller
 * @date 08.02.2023
 */
public interface IDistributionService {

    /**
     * @param parts
     * @param taxonUUID
     * @param subAreaPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param statusOrderPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
     * @param omitLevels see {@link DescriptionUtility#orderDistributions(Set, Collection)}
     * @param presenceAbsenceTermColors
     * @param languages
     * @param propertyPaths
     * @param ignoreDistributionStatusUndefined workaround until #9500 is implemented to ignore status "undefined"
     * @return
     */
    public DistributionInfoDto composeDistributionInfoFor(DistributionInfoConfiguration config,
            UUID taxonUUID,
            Map<UUID,Color> presenceAbsenceTermColors,
            List<Language> languages, List<String> propertyPaths);

    /**
    * @param distributions
    * @param statusOrderPreference see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean, Set)}
    * @param fallbackAreaMarkerTypes marker types to make areas hidden, this includes fallback-areas which are defined to have visible sub-areas
    * @param config the {@link CondensedDistributionConfiguration condensed distribution configuration}
    * @param languages
    * @return
    */
    public CondensedDistribution getCondensedDistribution(Set<Distribution> distributions,
            TermTree<NamedArea> areaTree,
            TermTree<PresenceAbsenceTerm> statusTree,
            boolean statusOrderPreference,
            Set<MarkerType> fallbackAreaMarkerTypes,
            CondensedDistributionConfiguration config,
            List<Language> langs);

    /**
     * Adds an area mapping (CDM area -> geo service area). It is recommended to set the mapping
     * in a persistent way, so it is available after restarting the application.
     * @param area
     * @param geoServiceArea
     * @throws XMLStreamException
     */
    public void setMapping(NamedArea area, GeoServiceArea geoServiceArea);

    public String getDistributionServiceRequestParameterString(List<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference, boolean statusOrderPreference, Set<MarkerType> hideMarkedAreas,
            Map<UUID, Color> presenceAbsenceTermColors, List<Language> langs,
            boolean includeUnpublished);

    /**
     *
     * @param distributions
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            see {@link DescriptionUtility#filterDistributions(Collection, boolean, boolean}
     * @param presenceAbsenceTermColors
     * @param langs
     * @return
     */
    public String getDistributionServiceRequestParameterString(
            Set<Distribution> distributions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<UUID, Color> presenceAbsenceTermColors,
            List<Language> langs);

    /**
     * Reads csv data containing the attributes from a shape file and adds the
     * shapefile data to each area in the given set of {@link NamedAreas}. The
     * way this data it attached to the areas is specific to the
     * {@link IGeoServiceAreaMapping} implementation. It is recommended to
     * create csv file directly from the original shape file by making use of
     * the {@code org2ogr} command which is contained in the <a
     * href="http://www.gdal.org/ogr2ogr.html">gdal</a> tools:
     *
     * <pre>
     * ogr2ogr -f csv out.csv input_shape_file.shp
     * </pre>
     *
     * @param csvReader
     * @param idSearchFields
     *            An ordered list column names in the the csv file to be
     *            imported. These columns will be used to search for the
     *            {@link NamedArea#getIdInVocabulary() IdInVocabulary} of each
     *            area
     * @param wmsLayerName
     * @return the resulting table of the import, also together with diagnostic
     *         messages per NamedArea (id not found, ambiguous mapping)
     * @param areaVocabularyUuid
     *            , can be <code>NULL</code>. The NamedAreas contained in this
     *            vocabulary will be combined with areas defined in the
     *            <code>namedAreaUuids</code>
     * @param namedAreaUuids
     *            a set of UUIDS for {@link NamedArea}. Can be <code>NULL</code>.
     *            Will be combined with the vocabulary if the
     *            <code>areaVocabularyUuid</code> is also given.
     *
     * @return
     * @throws IOException
     */
    @Transactional(readOnly=false)
    public abstract Map<NamedArea, String> mapShapeFileToNamedAreas(Reader csvReader,
            List<String> idSearchFields, String wmsLayerName, UUID areaVocabularyUuid,
            Set<UUID> namedAreaUuids)
            throws IOException;
}