
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" indent="yes"/>
    <!-- Authors: Sybille & Lorna -->
    <!-- Date: March/April 2013 -->

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- create a timestamp for the whole going -->
    <xsl:variable name="timestamp">
        <xsl:value-of
            select="concat(year-from-date(current-date()),'-',month-from-date(current-date()),'-',day-from-date(current-date()),'T',hours-from-time(current-time()),':',minutes-from-time(current-time()),':00Z')"
        />
    </xsl:variable>

    <!-- create the username who changed/created the pages -->
    <xsl:variable name="username">Sybille Test </xsl:variable>

    <!-- this is the start template 
    it creates the mediawiki tag surounding and calls a template to create a page for 
    every taxon node TODO: and a category -->
    <xsl:template match="root">
        <mediawiki xmlns="http://www.mediawiki.org/xml/export-0.7/"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.7.xsd" version="0.7"
            xml:lang="en">
            <siteinfo>
                <sitename>TestWiki</sitename>
                <base>http://biowikifarm.net/test/Main_Page</base>
                <generator>MediaWiki 1.20.3</generator>
                <case>first-letter</case>
                <namespaces>
                    <namespace key="-2" case="first-letter">Media</namespace>
                    <namespace key="-1" case="first-letter">Special</namespace>
                    <namespace key="0" case="first-letter"/>
                    <namespace key="1" case="first-letter">Talk</namespace>
                    <namespace key="2" case="first-letter">User</namespace>
                    <namespace key="3" case="first-letter">User talk</namespace>
                    <namespace key="4" case="first-letter">TestWiki</namespace>
                    <namespace key="5" case="first-letter">TestWiki talk</namespace>
                    <namespace key="6" case="first-letter">File</namespace>
                    <namespace key="7" case="first-letter">File talk</namespace>
                    <namespace key="8" case="first-letter">MediaWiki</namespace>
                    <namespace key="9" case="first-letter">MediaWiki talk</namespace>
                    <namespace key="10" case="first-letter">Template</namespace>
                    <namespace key="11" case="first-letter">Template talk</namespace>
                    <namespace key="12" case="first-letter">Help</namespace>
                    <namespace key="13" case="first-letter">Help talk</namespace>
                    <namespace key="14" case="first-letter">Category</namespace>
                    <namespace key="15" case="first-letter">Category talk</namespace>
                    <namespace key="102" case="first-letter">Property</namespace>
                    <namespace key="103" case="first-letter">Property talk</namespace>
                    <namespace key="106" case="first-letter">Form</namespace>
                    <namespace key="107" case="first-letter">Form talk</namespace>
                    <namespace key="108" case="first-letter">Concept</namespace>
                    <namespace key="109" case="first-letter">Concept talk</namespace>
                    <namespace key="170" case="first-letter">Filter</namespace>
                    <namespace key="171" case="first-letter">Filter talk</namespace>
                    <namespace key="198" case="first-letter">Internal</namespace>
                    <namespace key="199" case="first-letter">Internal talk</namespace>
                    <namespace key="200" case="first-letter">Portal</namespace>
                    <namespace key="201" case="first-letter">Portal talk</namespace>
                    <namespace key="202" case="first-letter">Bibliography</namespace>
                    <namespace key="203" case="first-letter">Bibliography talk</namespace>
                    <namespace key="204" case="first-letter">Draft</namespace>
                    <namespace key="205" case="first-letter">Draft talk</namespace>
                    <namespace key="206" case="first-letter">Submission</namespace>
                    <namespace key="207" case="first-letter">Submission talk</namespace>
                    <namespace key="208" case="first-letter">Reviewed</namespace>
                    <namespace key="209" case="first-letter">Reviewed talk</namespace>
                    <namespace key="420" case="first-letter">Layer</namespace>
                    <namespace key="421" case="first-letter">Layer talk</namespace>
                </namespaces>
            </siteinfo>

            <xsl:apply-templates select="//TaxonNode"/>
            <!-- TODO we cannot just call every node, we have to parse the tree
             to gather tree structure for the categories tree-->
        </mediawiki>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- this creates a page and a category for 
    every taxon node.  -->
    <!-- TODO create a template for page creating and for categorie creating -->
    <xsl:template match="TaxonNode" name="TaxonNode">

        <!-- as we will need the title more than once, we create a variable-->
        <xsl:variable name="title">
            <xsl:call-template name="title">
                <xsl:with-param name="taxon" select="Taxon"/>
            </xsl:call-template>
        </xsl:variable>

        <!-- we also initialize the parent taxon variable if there is a higher taxon assigned: -->

        <xsl:variable name="parent-title">
            <xsl:if test="exists(../..) and name(../..)='TaxonNode'">

                <xsl:call-template name="title">
                    <xsl:with-param name="taxon" select="../../Taxon"/>
                </xsl:call-template>

            </xsl:if>
            <!-- else if no higher taxon could be found -->
            <xsl:if test="not(exists(../..)) or not(name(../..)='TaxonNode')">

                <xsl:text>{{No Parent}}</xsl:text>

            </xsl:if>
        </xsl:variable>

        <!-- create category -->
        <page>
            <title>
                <xsl:text>Category:</xsl:text>
                <xsl:value-of select="$title"/>
            </title>
            <revision>
                <!-- TODO: create seconds without positions after decimal point! -->
                <timestamp>
                    <xsl:value-of select="$timestamp"/>
                </timestamp>
                <contributor>
                    <username>Sybille Test</username>
                </contributor>
                <text xml:space="preserve">
                    
                    <!-- redirekt to corresponding page -->
                    <xsl:value-of select="concat('#REDIRECT [[',$title,']]')"/>

                    <!-- add parent categorie if exists -->
                    <xsl:if test="exists(../..) and name(../..)='TaxonNode'">
                        <xsl:value-of select="concat('[[Category:',$parent-title,']]')"/>
                    </xsl:if>
              
                </text>
            </revision>
        </page>

        <!-- create taxon page -->
        <page>
            <title>
                <xsl:value-of select="$title"/>
            </title>
            <revision>
                <!-- TODO: create seconds without positions after decimal point! -->
                <timestamp>
                    <xsl:value-of select="$timestamp"/>
                </timestamp>
                <contributor>
                    <username>
                        <xsl:value-of select="$username"/>
                    </username>
                </contributor>
                <text xml:space="preserve">
                    <!-- add table of contents -->
                    <xsl:call-template name="TOC"/>
                   <!-- add taxo tree -->
                     <xsl:value-of select="concat('{{Taxo Tree|',$parent-title, '}}')"/> 
                   
                   <!-- add contents of taxon page -->                   

                    <xsl:apply-templates select="Taxon"/>
                    <xsl:call-template name="display-references"/>
                    
                    <!-- put page to corresponding tax category -->
                    <xsl:value-of select="concat('[[Category:',$title, ']]')"/>

                </text>
            </revision>
        </page>

        <!--<xsl:apply-templates select="//childNodes/TaxonNode/Taxon"/>-->
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- this template provides the correct name of a taxon 
    parameter: taxon the taxon we want to get the name from-->
    <xsl:template name="title">
        <xsl:param name="taxon"/>
        <xsl:choose>
            <!-- family -->
            <xsl:when test="$taxon/name/rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
                <xsl:call-template name="get-family-or-genus-title">
                    <xsl:with-param name="genusOrUninomial" select="$taxon/name/genusOrUninomial"/>
                </xsl:call-template>
            </xsl:when>
            <!-- genus -->
            <xsl:when test="$taxon/name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
                <xsl:call-template name="get-family-or-genus-title">
                    <xsl:with-param name="genusOrUninomial" select="$taxon/name/genusOrUninomial"/>
                </xsl:call-template>
            </xsl:when>
            <!--TODO-->
            <!-- subgenus -->
            <xsl:when test="$taxon/name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
                <xsl:apply-templates select="$taxon/name/rank/representation_L10n"/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="$taxon/name/genusOrUninomial"/>
            </xsl:when>
            <!-- species -->
            <xsl:when test="$taxon/name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
                <xsl:call-template name="get-species-title">
                    <xsl:with-param name="taxon" select="$taxon"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <!-- for debugging --> Unformatted title for rank uuid: <xsl:value-of
                    select="$taxon/name/rank/uuid"/>: <xsl:value-of select="$taxon/name/titleCache"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- these templates create the name of the taxa wheater their kind is -->

    <xsl:template name="get-family-or-genus-title">
        <xsl:param name="genusOrUninomial"/>
        <xsl:value-of select="$genusOrUninomial"/>
    </xsl:template>

    <xsl:template name="get-species-title">
        <xsl:param name="taxon"/>
        <xsl:value-of
            select="concat($taxon/name/genusOrUninomial, ' ', $taxon/name/specificEpithet)"/>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->




    <!-- we run this for the content of the page -->
    <xsl:template match="Taxon" name="Taxon">
        <xsl:apply-templates select="synonymy"/>
        <xsl:apply-templates select="descriptions"/>


    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- these templates provide the citations -->

    <xsl:template name="display-references">
        <xsl:call-template name="chapter">
            <xsl:with-param name="title">References</xsl:with-param>
        </xsl:call-template>
        <xsl:text>{{Show_References}}</xsl:text>
    </xsl:template>


    <xsl:template name="citations">
        <xsl:param name="name-uuid"/>
        <xsl:param name="descriptionelements"/>

        <!--<ref name="{$name-uuid}">-->
        <!-- iterate through all the description elements for the citation feature -->
        <xsl:for-each select="$descriptionelements/descriptionelement">

            <xsl:sort select="sources/e[1]/citation/datePublished/start"/>

            <xsl:variable name="citation-uuid" select="sources/e[1]/citation/uuid"/>

            <xsl:for-each select="sources/e">
                <xsl:if test="nameUsedInSource/uuid=$name-uuid">

                    <!-- use the citation-uuid as a unique name for the reference -->
                    <xsl:value-of
                        select="concat('{{ViBRANT_Reference|Name=',$citation-uuid,'|Content=')"/>
                    <xsl:text>{{aut|</xsl:text>


                    <xsl:choose>
                        <xsl:when test="citation/authorTeam/teamMembers/e[1]/lastname != ''">
                            <xsl:for-each select="citation/authorTeam/teamMembers/e">
                                <xsl:value-of select="lastname"/>
                                <xsl:choose>
                                    <xsl:when test="position() != last()">
                                        <xsl:text> &amp; </xsl:text>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="citation/authorTeam/lastname"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>}} </xsl:text>
                    <xsl:if test="citation/datePublished/start != ''">
                        <xsl:value-of select="citation/datePublished/start"/>
                        <xsl:text>: </xsl:text>
                    </xsl:if>
                    <xsl:value-of select="citation/title"/>
                    <xsl:text>.</xsl:text>
                    <xsl:value-of select="citation/pages"/>
                    <xsl:text>.</xsl:text>

                    <xsl:if test="citation/type = 'Book' or citation/type = 'BookSection'">
                        <xsl:if test="citation/placePublished != '' or citation/publisher != ''">
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="citation/placePublished"/>
                        <xsl:text>, </xsl:text>
                        <xsl:value-of select="citation/publisher"/>
                        <xsl:text>.</xsl:text>
                        </xsl:if>
                    </xsl:if>
                    <xsl:text>}}</xsl:text>
                </xsl:if>
            </xsl:for-each>
            <!--</ref>-->
        </xsl:for-each>

    </xsl:template>
    <!--ref name="SC078">{{aut|Mohrig, W.; Menzel, F.}} 1992: Neue Arten europäischer Trauermücken (Diptera, Sciaridae). ''An International Journal of Dipterological Research'', '''3'''(1-2), 1–16.</ref-->

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- these templates provide the synonomy -->

    <xsl:template match="synonymy" name="synonymy">
        <!--<xsl:text>&#xA;'''Synonymy'''&#xA;&#xA;</xsl:text>-->
        <xsl:call-template name="chapter">
            <xsl:with-param name="title">Synonomy</xsl:with-param>
        </xsl:call-template>
        <xsl:apply-templates select="../name"/>
        <xsl:call-template name="citations">
            <xsl:with-param name="descriptionelements"
                select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
            <xsl:with-param name="name-uuid" select="../name/uuid"/>
        </xsl:call-template>

        <xsl:apply-templates select="homotypicSynonymsByHomotypicGroup"/>
        <xsl:apply-templates select="heterotypicSynonymyGroups"/>

    </xsl:template>

    <!--.............................................-->

    <xsl:template match="homotypicSynonymsByHomotypicGroup">
        <xsl:for-each select="e">
            <xsl:text>{{Homotypic Synonym|1|</xsl:text>

            <xsl:apply-templates select="name"/>
            <xsl:text>}}</xsl:text>
        </xsl:for-each>
    </xsl:template>

    <!--.............................................-->

    <xsl:template match="heterotypicSynonymyGroups">
        <xsl:for-each select="e">

            <!--do foreach for the rest-->
            <!--<xsl:text>==***==</xsl:text>-->
            <xsl:variable name="first-element" select="e[1]"/>
            <xsl:text>{{Heterotypic Synonym|1|</xsl:text>
            <xsl:apply-templates select="$first-element/name"/>
            <xsl:text>}}</xsl:text>
            <!--<xsl:text>==***==</xsl:text>-->

            <!-- take the first one to printout as the head of the homotypic group -->

            <xsl:for-each select="e[position() &gt; 1]">
                <xsl:text>{{Homotypic Synonym|2|</xsl:text>
                <xsl:apply-templates select="name"/>
                <xsl:text>}}</xsl:text>
                <!--xsl:call-template name="citations">
                <LORNA Pass the description elements for the citation 99b2842f-9aa7-42fa-bd5f-7285311e0101>
                <xsl:with-param name="descriptionelements" select="../../../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
                <xsl:with-param name="name-uuid" select="name/uuid"/>
            </xsl:call-template-->
            </xsl:for-each>
        </xsl:for-each>

        <!--xsl:apply-templates select="e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates-->
    </xsl:template>

    <!--.............................................-->

    <xsl:template match="name">
        <xsl:apply-templates select="taggedName"/>
        <!--xsl:apply-templates select="nomenclaturalReference"/-->
    </xsl:template>

    <xsl:template match="taggedName">
        <xsl:for-each select="e">
            <!-- TODO mybe some types don't contain font tags, we don't have to call the tmeplate  -->
            <!--        we could just use <xsl:value-of select="text"/>-->
            <xsl:choose>
                <xsl:when test="type='name'">
                    <xsl:apply-templates select="text"/>
                    <xsl:text> </xsl:text>
                </xsl:when>
                <xsl:when test="type='authors'">
                    <xsl:apply-templates select="text"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="text"/>
                    <xsl:text> </xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!-- description features -->

    <!-- TODO: for first and second level: 
        check if there is a page that describes the feature 
        and use Parameter "Link=pagename" with templates Tax_Feature or 
        Second_Level_Feature-->

    <xsl:template match="descriptions" name="descriptions">
        <xsl:for-each select="features/feature">
            <xsl:choose>
                <xsl:when test="count(feature)!=0">
                    <xsl:call-template name="secondLevelDescriptionElements"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- everything but Citation -->
                    <xsl:if test="uuid!='99b2842f-9aa7-42fa-bd5f-7285311e0101'">
                        <xsl:call-template name="descriptionElements"/>
                    </xsl:if>
                    <!--xsl:apply-templates select="media/e/representations/e/parts/e/uri"/-->
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!--.............................................-->

    <xsl:template name="secondLevelDescriptionElements">

        <xsl:value-of select="concat('{{Two_Leveled_Features_Title|',representation_L10n,'}}')"/>

        <xsl:for-each select="feature">
            <xsl:value-of
                select="concat('{{Second_Level_Feature|Name=',representation_L10n,'|Elements=')"/>
            <!-- TODO create Element -->
            <xsl:for-each select="descriptionelements/descriptionelement">
                <xsl:value-of
                    select="concat('{{Second_Level_Feature_DescrElement|',multilanguageText_L10n/text, '}}')"
                />
            </xsl:for-each>
            <xsl:text>}}</xsl:text>
        </xsl:for-each>

    </xsl:template>
    <!--.............................................-->


    <xsl:template name="descriptionElements">
        <xsl:choose>
            <xsl:when test="supportsCommonTaxonName='true'">
                <!-- must be Vernacular Name feature -->
                <xsl:call-template name="commonTaxonName"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- for example Habitat, Material Examined -->
                <xsl:call-template name="textData"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--.............................................-->

    <xsl:template name="commonTaxonName">

        <xsl:value-of select="concat('{{Tax_Feature|Name=',representation_L10n,'|Elements=')"/>
        <xsl:for-each select="descriptionelements/descriptionelement">
            <xsl:text/>
            <xsl:value-of
                select="concat('{{Common_Name_Feature_Element|Name=',name,'|Language=',language/representation_L10n,'}}')"
            />
        </xsl:for-each>
        <xsl:text>}}</xsl:text>
    </xsl:template>

    <!--.............................................-->

    <xsl:template name="textData">

        <xsl:value-of
            select="concat('{{Tax_Feature|Name=',representation_L10n, '|Elements={{Feature_Text|' )"/>
        <xsl:choose>
            <xsl:when test="uuid!='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'">
                <!-- feature is not "Distribution" -->
                <xsl:apply-templates
                    select="descriptionelements/descriptionelement[1]/multilanguageText_L10n/text"/>
            </xsl:when>
        </xsl:choose>

        <xsl:text>}}}}</xsl:text>


        <!-- LORNA TRY IMAGE HERE -->
        <!--xsl:apply-templates select="descriptionelements/descriptionelement[1]/media/e/representations/e/parts/e/uri"/-->


    </xsl:template>

    <!--.............................................-->


    <xsl:template match="text">

        <xsl:choose>
            <xsl:when test="contains(.,&quot;&lt;b&gt;&quot;)">
                <xsl:call-template name="add-markup">
                    <xsl:with-param name="str" select="."/>
                    <!--xsl:with-param name="tag-name" select="b"/-->
                    <xsl:with-param name="tag-name">b</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="add-markup">
                    <xsl:with-param name="str" select="."/>
                    <xsl:with-param name="tag-name">i</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

        <!--/fo:block -->
    </xsl:template>


    <!--+++++++++++++++++++++++++++++L A Y O U T ++++++++++++++++++++++++++++++++++++++ -->


    <!-- change here to change the look of the mediawiki output -->
    <!-- please use mediawiki templates -->
    <!-- think also of template changes in the mediawiki -->
    <!-- TODO: wrap TOC layout in a mediawiki template -->

    <xsl:template name="TOC"> {{ViBRANT_TOC}} </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <xsl:template name="chapter">
        <xsl:param name="title"/>
        <xsl:value-of select="concat('{{Chapter|',$title,'}}')"/>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <xsl:template name="subchapter">
        <xsl:param name="title"/>
        <xsl:value-of select="concat('{{Subchapter|',$title,'}}')"/>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!--this template layouts and displays the name of a 
        taxon depending on it's "kind"
        MAYDO: create more useful layouts instead of title sizes-->
    <xsl:template name="display-taxon-name">
        <xsl:param name="taxon"/>
        <!-- get the name of the taxon -->
        <xsl:variable name="name">
            <xsl:call-template name="title">
                <xsl:with-param name="taxon" select="$taxon"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- format the name of the taxon according to it's kind -->
        <xsl:choose>
            <!-- family -->
            <xsl:when test="$taxon/name/rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
                <xsl:value-of select="concat('{{Family name|', $name, '}}')"/>
            </xsl:when>
            <!-- genus -->
            <xsl:when test="$taxon/name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
                <xsl:value-of select="concat('{{Genus name|', $name, '}}')"/>
            </xsl:when>
            <!--TODO-->
            <!-- subgenus -->
            <xsl:when test="$taxon/name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
                <xsl:value-of select="concat('{{Subgenus name|', $name, '}}')"/>
            </xsl:when>
            <!-- species -->
            <xsl:when test="$taxon/name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
                <xsl:value-of select="concat('{{Species name|', $name, '}}')"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- for debugging --> Unformatted title for rank uuid: <xsl:value-of
                    select="$taxon/name/rank/uuid"/>: <xsl:value-of select="$taxon/name/titleCache"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

    <!--we replace html i> with mediawiki templates-->
    <xsl:template match="text" name="replace_html_tags">
        <xsl:call-template name="replace-tags">
            <xsl:with-param name="text-string" select="."/>
        </xsl:call-template>

    </xsl:template>

    <!--.............................................-->

    <xsl:template name="replace-tags">
        <xsl:param name="text-string"/>
        <xsl:choose>
            <xsl:when test="contains($text-string,';&lt;b&gt;')">
                <xsl:call-template name="add-markup">
                    <xsl:with-param name="str" select="$text-string"/>
                    <!--xsl:with-param name="tag-name" select="b"/-->
                    <xsl:with-param name="wiki-template">Bold</xsl:with-param>
                    <xsl:with-param name="tag-name">b</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="add-markup">
                    <xsl:with-param name="str" select="$text-string"/>
                    <xsl:with-param name="wiki-template">Italic</xsl:with-param>
                    <xsl:with-param name="tag-name">i</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!--.............................................-->

    <xsl:template name="add-markup">
        <xsl:param name="str"/>
        <xsl:param name="wiki-template"/>
        <xsl:param name="tag-name"/>

        <xsl:variable name="opening-tag">
            <xsl:value-of select="concat('&lt;', $tag-name, '&gt;')"> </xsl:value-of>
        </xsl:variable>
        <xsl:variable name="closing-tag">
            <xsl:value-of select="concat('&lt;/', $tag-name, '&gt;')"> </xsl:value-of>
        </xsl:variable>


        <xsl:choose>
            <xsl:when test="contains($str, $opening-tag) and contains($str, $closing-tag)">
                <xsl:variable name="before-tag" select="substring-before($str, $opening-tag)"/>
                <xsl:variable name="inside-tag"
                    select="substring-before(substring-after($str,$opening-tag),$closing-tag)"/>
                <xsl:variable name="after-tag" select="substring-after($str, $closing-tag)"/>
                <xsl:value-of select="concat($before-tag,'{{',$wiki-template,'|',$inside-tag,'}}')"/>
                <xsl:call-template name="replace-tags">
                    <xsl:with-param name="text-string" select="$after-tag"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

</xsl:stylesheet>
