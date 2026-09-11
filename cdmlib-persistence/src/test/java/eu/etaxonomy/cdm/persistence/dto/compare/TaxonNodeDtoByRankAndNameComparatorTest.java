/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author muellera
 * @since 02.09.2026
 */
public class TaxonNodeDtoByRankAndNameComparatorTest {

    private TaxonNodeDtoByRankAndNameComparator comparator;
    private ISortableTaxonNodeDto nodeDto1;
    private ISortableTaxonNodeDto nodeDto2;
    private ISortableTaxonNodeDto nodeDto3;
    private ISortableTaxonNodeDto nodeDto4;
    private ISortableTaxonNodeDto nodeDto5;
    private List<ISortableTaxonNodeDto> nodeDtoList;
    private List<ISortableTaxonNodeDto> sortedList;
    private TaxonName name1;
    private TaxonName name2;
    private TaxonName name3;
    private TaxonName name4;
    private TaxonName name5;
    private INonViralNameParser<TaxonName> nameParser;
    private UUID classificationUuid;


    @Before
    public void setUp() throws Exception {
        classificationUuid = UUID.randomUUID();
        nameParser = (INonViralNameParser)NonViralNameParserImpl.DefaultInstance();
        name1 = nameParser.parseFullName("Genus vidus All");
        name2 = nameParser.parseFullName("Genus baldus All");
        name3 = nameParser.parseFullName("Genus dretus All");
        name4 = nameParser.parseFullName("Genus hortus All");
        name5 = nameParser.parseFullName("Genus locus All");

        boolean published = true;
        TaxonNodeStatus included = TaxonNodeStatus.INCLDUDED;
        comparator = new TaxonNodeDtoByRankAndNameComparator();
        nodeDto1 = new TaxonNodeDto(UUID.randomUUID(),
                1, UUID.randomUUID(), "#t1#1#", "Name A",
                "Taxon A", 43, UUID.randomUUID(), 1,
                classificationUuid, published, included,
                null, 0, null,
                name1.getTaggedName());
        nodeDto2 = new TaxonNodeDto(UUID.randomUUID(),
                2, UUID.randomUUID(), "#t1#2#", "Name B",
                "Taxon B", 43, UUID.randomUUID(), 1,
                classificationUuid, published, included,
                null, 0, null,
                name2.getTaggedName());
        nodeDto3 = new TaxonNodeDto(UUID.randomUUID(),
                3, UUID.randomUUID(), "#t1#3#", "Name C",
                "Taxon C", 42, UUID.randomUUID(), 1,
                classificationUuid, published, included,
                null, 0, null,
                null);
        nodeDto4 = new TaxonNodeDto(UUID.randomUUID(),
                4, UUID.randomUUID(), "#t1#4#", "Name D",
                "Taxon D", 43, UUID.randomUUID(), 1,
                classificationUuid, published, included,
                null, 0, null,
                null);
        nodeDto5 = new TaxonNodeDto(UUID.randomUUID(),
                5, UUID.randomUUID(), "#t1#5#", "Name E",
                "Taxon E", 43, UUID.randomUUID(), 1,
                classificationUuid, published, included,
                null, 0, null,
                null);

        nodeDtoList = List.of(nodeDto5, nodeDto3, nodeDto2, nodeDto1, nodeDto4);
        sortedList = List.of(nodeDto3, nodeDto2, nodeDto1, nodeDto4, nodeDto5);

    }

//    TaxonNodeDto(UUID uuid,
//    Integer id, UUID taxonUuid, String treeIndex, String nameTitleCache,
//    String taxonTitleCache, Integer rankOrderIndex, UUID parentUuid, Integer sortIndex,
//    UUID classificationUuid, Boolean taxonIsPublished, TaxonNodeStatus status,
//    List<LanguageString> placementNote, Integer childrenCount, UUID secUuid,
//    List<TaggedText> taggedName){

    @Test
    public void testCompare() {
        int c = comparator.compare(null, null);
        Assert.assertEquals(0, c);
        c = comparator.compare(nodeDto1, null);
        Assert.assertEquals(-1, c);
        c = comparator.compare(null, nodeDto2);
        Assert.assertEquals(1, c);
        c = comparator.compare(nodeDto1, nodeDto1);
        Assert.assertEquals(0, c);

        c = comparator.compare(nodeDto1, nodeDto2);
        Assert.assertTrue(c>0);
        c = comparator.compare(nodeDto2, nodeDto1);
        Assert.assertTrue(c<0);

        List<ISortableTaxonNodeDto> sortableList = new ArrayList<>(nodeDtoList);
        Collections.sort(sortableList, comparator);
        Assert.assertEquals(sortedList, sortableList);
    }
}