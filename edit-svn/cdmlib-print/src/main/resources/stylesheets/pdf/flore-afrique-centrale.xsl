<?xml version="1.0" encoding="UTF-8"?>
<!--
  
  CDM XSL Transformation
  Target Format: Flore d'Afrique Centrale
  
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">

  <!-- ############### Constants and definitions ################### -->

  <!-- **********************************
    ** global definitions 
    *********************************** -->
  <xsl:param name="global-family-font">Times</xsl:param>
  <xsl:param name="global-size-font">10pt</xsl:param>
  <xsl:param name="global-line-height">12pt</xsl:param>

  <xsl:param name="global-width-page">159mm</xsl:param>
  <xsl:param name="global-height-page">244mm</xsl:param>

  <xsl:param name="global-indentation">0mm</xsl:param>

  <!-- **********************************   
    ** definitions for taxon pages
    *********************************** -->
  <xsl:param name="taxon-page-top-margin">32mm</xsl:param>
  <xsl:param name="taxon-page-inner-margin">21mm</xsl:param>
  <xsl:param name="taxon-page-bottom-margin">32mm</xsl:param>

  <xsl:param name="taxon-region-body-outer-margin">24mm</xsl:param>

  <xsl:param name="taxon-region-before-extent">0mm</xsl:param>
  <xsl:param name="taxon-region-after-extent">0mm</xsl:param>

  <xsl:param name="content-bg-color">white</xsl:param>
  <xsl:param name="taxon-header-family-font">Times</xsl:param>
  <xsl:param name="taxon-header-before-bg-color">white</xsl:param>
  <xsl:param name="taxon-header-after-bg-color">white</xsl:param>
  <xsl:param name="taxon-header-size-font">7pt</xsl:param>
  
  
  <xsl:param name="taxon-name-indentation">4mm</xsl:param>
  <xsl:param name="taxon-name-size-font">8pt</xsl:param>
  <xsl:param name="taxon-name-line-height">9.6pt</xsl:param>
  
  <xsl:param name="taxon-header-style-font">italic</xsl:param>
  <xsl:param name="taxon-header-page-number-size-font">10pt</xsl:param>
  <xsl:param name="taxon-header-page-number-align-text">end</xsl:param>

  <xsl:param name="taxon-page-number-initial">1</xsl:param>

  <!-- hardcoded ranks -->
  <xsl:param name="uuidFamily">210a8214-4e69-401a-8e47-c7940d990bdd</xsl:param>
  <xsl:param name="uuidGenus">1b11c34c-48a8-4efa-98d5-84f7f66ef43a</xsl:param>
  <xsl:param name="uuidSubgenus">78786e16-2a70-48af-a608-494023b91904</xsl:param>

  <!-- **********************************   
    ** html support
    *********************************** -->

  <!-- format html i tags in text as italic -->
  <xsl:template match="i">
    <fo:inline font-style="italic">
      <xsl:value-of select="."/>
    </fo:inline>
  </xsl:template>

  <!-- format html b tags in text as bold -->
  <xsl:template match="b">
    <fo:inline font-weight="bold">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

  <!-- ########################################################################## -->


  <!-- start xsl output -->
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="no"/>
  <xsl:param name="versionParam" select="'1.0'"/>

  <!-- root element: root -->
  <xsl:template match="root">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

      <!-- defines page layout -->
      <fo:layout-master-set>

        <!-- layout for odd taxon pages -->
        <fo:simple-page-master 
          master-name="taxon_page_odd" 
          page-height="{$global-height-page}"
          page-width="{$global-width-page}" 
          margin-top="{$taxon-page-top-margin}"
          margin-bottom="{$taxon-page-bottom-margin}" 
          margin-left="{$taxon-page-inner-margin}"
          margin-right="">
          <fo:region-body 
            margin-right="{$taxon-region-body-outer-margin}"/>
          <fo:region-before extent="{$taxon-region-before-extent}" region-name="odd-before"/>
          <fo:region-after extent="{$taxon-region-after-extent}" region-name="odd-after"/>
        </fo:simple-page-master>

        <!-- layout for even taxon pages -->
        <fo:simple-page-master 
          master-name="taxon_page_even" 
          page-height="{$global-height-page}"
          page-width="{$global-width-page}" 
          margin-top="{$taxon-page-top-margin}"
          margin-bottom="{$taxon-page-bottom-margin}" 
          margin-left="0"
          margin-right="{$taxon-page-inner-margin}">
          <fo:region-body 
            margin-left="{$taxon-region-body-outer-margin}"/>
          <fo:region-before extent="{$taxon-region-before-extent}" region-name="even-before"/>
          <fo:region-after extent="{$taxon-region-after-extent}" region-name="even-after"/>
        </fo:simple-page-master>



        <!-- defines repeatable page-sequence for layout of taxa -->
        <fo:page-sequence-master master-name="taxon_page">
          <fo:repeatable-page-master-alternatives>
            <fo:conditional-page-master-reference master-reference="taxon_page_odd"
              page-position="first"/>
            <fo:conditional-page-master-reference master-reference="taxon_page_even"
              page-position="rest" odd-or-even="even"/>
            <fo:conditional-page-master-reference master-reference="taxon_page_odd"
              page-position="rest" odd-or-even="odd"/>
            <!--            <fo:conditional-page-master-reference master-reference="mclBoook_taxon_page_even"
              page-position="last"/>-->
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>
      <!-- end: defines page layout -->

      <!-- page sequence for taxon view -->
      <fo:page-sequence master-reference="taxon_page"
        initial-page-number="{$taxon-page-number-initial}" force-page-count="no-force">

        <fo:static-content flow-name="odd-before">
          <xsl:call-template name="right-extent"/>
        </fo:static-content>

        <fo:static-content flow-name="even-before">
          <xsl:call-template name="left-extent"/>
        </fo:static-content>

        <!-- format taxa -->
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-family="{$global-family-font}" font-size="{$global-size-font}"
            line-height="{$global-line-height}">
            <xsl:for-each select="//TaxonNode">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </fo:block>
        </fo:flow>

      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <!-- HEADER -->

  <xsl:template name="right-extent">

    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}">
      <!-- No pagination at the moment
      <fo:block font-size="{$taxon-header-page-number-size-font}"
        text-align="{$taxon-header-page-number-align-text}"> &#x2014; <fo:page-number/> &#x2014; </fo:block>
        <fo:block color="white">-</fo:block> -->
      <fo:block>
        <fo:retrieve-marker retrieve-class-name="pageTitleMarker"
          retrieve-position="first-starting-within-page" retrieve-boundary="document"/>
        <xsl:text> </xsl:text>
        <fo:retrieve-marker retrieve-class-name="pageTitleSectionMarker"
          retrieve-position="first-starting-within-page" retrieve-boundary="document"/>
      </fo:block>
    </fo:block>

  </xsl:template>

  <xsl:template name="left-extent">
    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}">
      <!-- No pagination at the moment
      <fo:block font-size="{$taxon-header-page-number-size-font}"
        text-align="{$taxon-header-page-number-align-text}"> &#x2014; <fo:page-number/> &#x2014;
        </fo:block> -->
    </fo:block>
  </xsl:template>

  <!--xsl:template name="odd-after-header">
    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}">
      <fo:block>
        <fo:inline>
          <fo:retrieve-marker retrieve-class-name="pageTitleMarker"
            retrieve-position="last-starting-within-page" retrieve-boundary="document"/>
          <fo:retrieve-marker retrieve-class-name="pageTitleSectionMarker"
            retrieve-position="last-starting-within-page" retrieve-boundary="document"/>
        </fo:inline>
      </fo:block>
    </fo:block>
  </xsl:template-->


  <!--xsl:template name="even-before-header">
    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}"-->
      <!--
      No pagination at the moment  
      <fo:block color="white" font-size="{$taxon-header-page-number-size-font}">-</fo:block>
      <fo:block color="white">-</fo:block>-->
    <!--  
    <fo:block>
        <fo:inline>
          <fo:retrieve-marker retrieve-class-name="pageTitleMarker"
            retrieve-position="first-starting-within-page" retrieve-boundary="document"/>
          <fo:retrieve-marker retrieve-class-name="pageTitleSectionMarker"
            retrieve-position="first-starting-within-page" retrieve-boundary="document"/>
        </fo:inline>
      </fo:block>
    </fo:block>

  </xsl:template-->

  <xsl:template name="even-after-header">
    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}">
      <fo:block>
        <fo:inline>
          <fo:retrieve-marker retrieve-class-name="pageTitleMarker"
            retrieve-position="last-starting-within-page" retrieve-boundary="document"/>
          <fo:retrieve-marker retrieve-class-name="pageTitleSectionMarker"
            retrieve-position="last-starting-within-page" retrieve-boundary="document"/>
        </fo:inline>
      </fo:block>
      <!-- No pagination at the moment
      <fo:block color="white">-</fo:block>
      <fo:block font-size="{$taxon-header-page-number-size-font}"
        text-align="{$taxon-header-page-number-align-text}"> &#x2014; <fo:page-number/> &#x2014;
        </fo:block> -->
    </fo:block>
  </xsl:template>

  <!-- GENERAL -->

  <xsl:template match="TaxonNode" name="TaxonNode">
    <xsl:apply-templates select="Taxon"/>
  </xsl:template>

  <xsl:template match="Taxon" name="Taxon">
    <xsl:call-template name="title"/>
    <xsl:apply-templates select="synonymy"/>
    <xsl:apply-templates select="descriptions"/>
  </xsl:template>


  <xsl:template name="title">
    <fo:block margin-bottom="5mm">
      <!-- we need to find a way to store the uuid in a variable xsl:parameter did not work or i was using it wrong -->
      <xsl:choose>
        <!-- family -->
        <xsl:when test="name/rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
          <fo:block text-align="center" text-transform="uppercase">
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- genus -->
        <xsl:when test="name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
          <fo:block font-weight="bold" text-align="center" text-transform="uppercase">
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- subgenus -->
        <xsl:when test="name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
          <fo:block font-weight="bold" text-align="center">
            <xsl:apply-templates select="name/rank/representation_L10n"/>
            <xsl:text> </xsl:text>
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- species -->
        <xsl:when test="name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
          <fo:block font-weight="bold" text-align="center">
            <xsl:apply-templates select="name/genusOrUninomial"/> 
            <xsl:text> </xsl:text>
            <xsl:apply-templates select="name/specificEpithet"/>
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
          <!-- for debugging --> Unformatted title for rank uuid: <xsl:value-of
            select="name/rank/uuid"/>: <xsl:value-of select="name/titleCache"/>
        </xsl:otherwise>
      </xsl:choose>
    </fo:block>
  </xsl:template>

  <!-- NAME -->

  <xsl:template match="name">
    <xsl:apply-templates select="taggedName"/>
    <xsl:apply-templates select="nomenclaturalReference"/>
  </xsl:template>

  <xsl:template match="taggedName">
    <xsl:for-each select="e">
      <xsl:choose>
        <xsl:when test="type='name'">
          <fo:inline font-style="italic">
            <xsl:value-of select="text"/>
          </fo:inline>
          <xsl:text> </xsl:text>
        </xsl:when>
        <xsl:when test="type='authors'">
          <xsl:value-of select="text"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="text"/>
          <xsl:text> </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

<!-- first date in brackets -->
  <xsl:template match="nomenclaturalReference">
    <xsl:text> (</xsl:text>
    <fo:inline>
      <xsl:value-of select="authorTeam/titleCache"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="datePublished/start"/>:
    </fo:inline>
    <xsl:text> </xsl:text>
    <fo:inline>
      <xsl:value-of select="../nomenclaturalMicroReference"/>
    </fo:inline>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- DESCRIPTIONS -->

  <xsl:template match="descriptions" name="descriptions">
    <xsl:for-each select="features/feature">
      <xsl:choose>
        <xsl:when test="count(feature)!=0">
          <xsl:call-template name="secondLevelDescriptionElements"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- everything except Citation -->
          <xsl:if test="uuid!='99b2842f-9aa7-42fa-bd5f-7285311e0101'">
            <xsl:call-template name="descriptionElements"/>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="descriptionElements">
      <xsl:choose>
        <xsl:when test="supportsCommonTaxonName='true'">
          <xsl:call-template name="commonTaxonName"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="textData"/>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <xsl:template name="secondLevelDescriptionElements">
    <fo:block text-align="justify" margin-bottom="5mm">
      <xsl:for-each select="feature">
        <fo:inline>
          <fo:inline text-decoration="underline">
            <xsl:value-of select="representation_L10n"/>:
          </fo:inline>
          <xsl:for-each select="descriptionelements/descriptionelement">
            <fo:inline>
              <xsl:value-of
                select="multilanguageText_L10n/text"/>
            </fo:inline>
          </xsl:for-each>
          <xsl:text> </xsl:text>
        </fo:inline>
      </xsl:for-each>
    </fo:block>
  </xsl:template>

  <xsl:template name="commonTaxonName">
    <fo:inline font-weight="bold">
      <xsl:value-of select="representation_L10n"/>
    </fo:inline>
    <xsl:text> – </xsl:text>
    <xsl:for-each select="descriptionelements/descriptionelement">
      <fo:inline>
        <xsl:value-of select="name"/> (<xsl:value-of
          select="language/representation_L10n"/>), </fo:inline>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="textData">
    <fo:block text-align="justify" margin-bottom="5mm">
      <!-- show all feature headlines except "ditribution" -->
      <xsl:if test="uuid!='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'">
        <fo:inline font-weight="bold">
          <xsl:value-of select="representation_L10n"/>
        </fo:inline>
        <xsl:text> – </xsl:text>
      </xsl:if>
      <xsl:for-each select="descriptionelements/descriptionelement">
        <fo:inline>
          <xsl:apply-templates select="multilanguageText_L10n/text"/>
        </fo:inline>
      </xsl:for-each>
    </fo:block>
  </xsl:template>


  <!-- SYNONYMY BLOCK -->

  <xsl:template match="synonymy" name="synonymy">
    <fo:block margin-bottom="5mm" line-height="{$taxon-name-line-height}" font-size="{$taxon-name-size-font}">
      <fo:block text-align="justify"
        text-indent="-{$taxon-name-indentation}" start-indent="{$taxon-name-indentation}">
        <xsl:apply-templates select="../name"/>
        <!--xsl:call-template name="citations"/-->
        <xsl:call-template name="citations">
          <xsl:with-param name="descriptionelements" select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
          <xsl:with-param name="name-uuid" select="../name/uuid"/>
        </xsl:call-template>
        <xsl:text>.</xsl:text>
        <xsl:apply-templates select="homotypicSynonymsByHomotypicGroup"/>
        <xsl:apply-templates select="../name/typeDesignations"/>
      </fo:block>
      <xsl:apply-templates select="heterotypicSynonymyGroups"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="homotypicSynonymsByHomotypicGroup">
    <xsl:for-each select="e">
      <xsl:text> - </xsl:text>
      <xsl:apply-templates select="name"/>
      <xsl:call-template name="citations">
      <!--LORNA Pass the description elements for the citation 99b2842f-9aa7-42fa-bd5f-7285311e0101 -->
        <xsl:with-param name="descriptionelements" select="../../../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
        <xsl:with-param name="name-uuid" select="name/uuid"/>
      </xsl:call-template>
      
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="heterotypicSynonymyGroups">
    <xsl:for-each select="e">
      <fo:block text-align="start"
        text-indent="-{$taxon-name-indentation}" start-indent="{$taxon-name-indentation}">
        <xsl:for-each select="e">
          <xsl:apply-templates select="name"/>
          <xsl:call-template name="citations">
            <xsl:with-param name="descriptionelements" select="../../../../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
            <xsl:with-param name="name-uuid" select="name/uuid"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:apply-templates select="e[1]/name/typeDesignations" />
      </fo:block>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="citations">
    <xsl:param name="name-uuid"/>
    <xsl:param name="descriptionelements" />
    <!--xsl:for-each
      select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements/descriptionelement"-->
    <xsl:for-each
      select="$descriptionelements/descriptionelement">
      <!-- TODO sorting only works for the first citation, implement correctly -->
      <xsl:sort select="sources/e[1]/citation/datePublished/start"/>
      <xsl:for-each select="sources/e">
        <xsl:if test="nameUsedInSource/uuid=$name-uuid">
          <xsl:text>; </xsl:text>
          <fo:inline>
            <!--xsl:value-of select="citation/authorTeam/titleCache"/-->
            <xsl:for-each select="citation/authorTeam/teamMembers/e">
              <xsl:value-of select="lastname"/>
              <xsl:choose>
                <xsl:when test="position() != last()"><xsl:text> &amp; </xsl:text></xsl:when>
              </xsl:choose>
            </xsl:for-each>
            
            <xsl:value-of select="citation/authorTeam/lastname"/>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="citation/datePublished/start"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="citationMicroReference"/>
            <xsl:text>)</xsl:text>
          </fo:inline>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="typeDesignations">
    <xsl:for-each select="child::*">
      <fo:inline>
        <xsl:text> - Type: </xsl:text>
        <xsl:choose>
          <xsl:when test="class='SpecimenTypeDesignation'">
            <xsl:value-of select="typeSpecimen/titleCache"/>
          </xsl:when>
        </xsl:choose>
      </fo:inline>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
