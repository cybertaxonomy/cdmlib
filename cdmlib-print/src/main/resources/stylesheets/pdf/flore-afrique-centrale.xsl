<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [<!ENTITY mdash "&#x2014;" > <!ENTITY ndash "&#x2013;" >]> 

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
  <xsl:param name="graphic-height">150mm</xsl:param>

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
  <xsl:param name="taxon-header-page-number-margin">11mm</xsl:param>

  <xsl:param name="taxon-page-number-initial">1</xsl:param>

  <!-- hardcoded ranks -->
  <xsl:param name="uuidFamily">210a8214-4e69-401a-8e47-c7940d990bdd</xsl:param>
  <xsl:param name="uuidGenus">1b11c34c-48a8-4efa-98d5-84f7f66ef43a</xsl:param>
  <xsl:param name="uuidSubgenus">78786e16-2a70-48af-a608-494023b91904</xsl:param>


  <!-- **********************************   
    ** html support
    *********************************** -->

  <!-- format html i tags in text as italic -->
  <!--xsl:template match="i">
    <fo:inline font-style="italic">
      <xsl:value-of select="."/>
    </fo:inline>
  </xsl:template-->

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
        <fo:simple-page-master master-name="taxon_page_odd" page-height="{$global-height-page}"
          page-width="{$global-width-page}" margin-top="{$taxon-page-top-margin}"
          margin-bottom="{$taxon-page-bottom-margin}" margin-left="{$taxon-page-inner-margin}">
          <fo:region-body margin-right="{$taxon-region-body-outer-margin}"/>
          <fo:region-before extent="{$taxon-region-before-extent}" region-name="odd-before"/>
          <fo:region-after extent="{$taxon-region-after-extent}" region-name="odd-after"/>
        </fo:simple-page-master>

        <!-- layout for even taxon pages -->
        <fo:simple-page-master master-name="taxon_page_even" page-height="{$global-height-page}"
          page-width="{$global-width-page}" margin-top="{$taxon-page-top-margin}"
          margin-bottom="{$taxon-page-bottom-margin}" margin-left="0"
          margin-right="{$taxon-page-inner-margin}">
          <fo:region-body margin-left="{$taxon-region-body-outer-margin}"/>
          <fo:region-before extent="{$taxon-region-before-extent}" region-name="even-before"/>
          <fo:region-after extent="{$taxon-region-after-extent}" region-name="even-after"/>

          <!--fo:leader leader-pattern="rule" leader-length="10mm"/-->

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
    <!--fo:block text-align="center">Page <fo:page-number/>
    </fo:block-->
    <fo:block font-family="{$taxon-header-family-font}" font-style="{$taxon-header-style-font}"
      font-size="{$taxon-header-size-font}">
      <!-- No pagination at the moment -->
      <fo:block font-size="{$taxon-header-page-number-size-font}" font-style="normal"
        padding-top="95%" text-align="end">| <fo:page-number padding-right="10%"
          border-right-width="11mm"/></fo:block>
      <!--text-align="{$taxon-header-page-number-align-text}"> | <fo:page-number/></fo:block> -->
      <!--margin-right="{$taxon-region-body-outer-margin}" -->
      <!-- &#x2014; -->
      <fo:block color="white">-</fo:block>
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
      <fo:block font-size="{$taxon-header-page-number-size-font}" font-style="normal"
        padding-top="95%" text-align="start"><fo:page-number padding-left="10%"
          border-right-width="11mm"/> |</fo:block>
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
    <xsl:apply-templates select="key"/>
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
      <xsl:value-of select="datePublished/start"/>: </fo:inline>
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
          <!--xsl:apply-templates select="media/e/representations/e/parts/e/uri"/-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="descriptionElements">
    <xsl:choose>
      <xsl:when test="supportsCommonTaxonName='true'">
        <!-- for example Vernacular Name -->
        <xsl:call-template name="commonTaxonName"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- for example Habitat, Material Examined -->
        <xsl:call-template name="textData"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="secondLevelDescriptionElements">
    <fo:block text-align="justify" margin-bottom="5mm">
      <!--fo:external-graphic src='http://lully.snv.jussieu.fr/xper2AppletThumbnailsMaker/sdd/images/ant_anatomy2.jpg'/-->
      <xsl:for-each select="feature">

        <xsl:variable name="representation" select="representation_L10n"/>
        <xsl:if test="not(starts-with($representation, 'Material'))">
          <!--xsl:if test="not(starts-with($representation, 'Figures'))"-->
          <fo:inline keep-with-next.within-line="always">

            <xsl:if test="not(starts-with($representation, 'Figures'))">
              <fo:inline text-decoration="underline" keep-with-next.within-line="always">

                <xsl:value-of select="representation_L10n"/>
              </fo:inline>
              <fo:inline>: </fo:inline>
            </xsl:if>

            <xsl:for-each select="descriptionelements/descriptionelement">

              <xsl:variable name="desc_element_text" select="multilanguageText_L10n/text"/>

              <!-- filter out repeated description element text. Lorna - could do this in the CDM so it doesn't occur in the XML but not sure why it's happening-->
              <xsl:variable name="prev_desc_element_text"
                select="preceding-sibling::descriptionelement[1]/multilanguageText_L10n/text"/>

              <fo:inline font-size="9pt" space-after="5mm">

                <xsl:if test="not(starts-with($desc_element_text, 'Figure'))">

                  <xsl:choose>
                    <xsl:when test="position() = 1">
                      <xsl:apply-templates select="multilanguageText_L10n/text"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:if test="$desc_element_text != $prev_desc_element_text">
                        <!-- checked in the tax editor and looks like only descriptionelement[1] refers to the species so don't need below-->
                        <!--xsl:apply-templates select="multilanguageText_L10n/text"/-->
                      </xsl:if>
                    </xsl:otherwise>
                  </xsl:choose>

                </xsl:if>

              </fo:inline>

              <!--xsl:apply-templates select="multilanguageText_L10n/text"/-->
              <!--xsl:apply-templates select="media"/-->

              <!--TODO Lorna we need to work out which description element contains the uri node for each figure 
            and place it next to the appropriate taxon in the output-->

              <!--xsl:if test="not(starts-with($representation, 'Figures'))"-->
              <xsl:if test="position() = 1">
                <!--xsl:value-of select="../../../../../../Taxon/name/titleCache"/-->
                <xsl:apply-templates select="media/e/representations/e/parts/e/uri"/>
              </xsl:if>
              <!--xsl:apply-templates select="e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates-->

            </xsl:for-each>

            <xsl:text> </xsl:text>
          </fo:inline>
        </xsl:if>
        <!--/xsl:if-->
      </xsl:for-each>


      <!--xsl:apply-templates select="../../../../../../../../../synonymy[1]/homotypicSynonymsByHomotypicGroup[1]/e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates-->

    </fo:block>

  </xsl:template>

  <!-- IMAGES -->

  <!--xsl:template match="media"-->
  <xsl:template match="uriprob">
    <fo:block>
      <xsl:variable name="graphic" select="."/>
      <fo:external-graphic content-height="450%" scaling="uniform" src="{$graphic}"
        padding-before="100" padding-after="30"/>
      <!--xsl:apply-templates select="../multilanguageText_L10n/text"/-->
      <fo:inline font-size="{$taxon-name-size-font}">
        <xsl:apply-templates select="../../../../../../../multilanguageText_L10n/text"/>
      </fo:inline>
    </fo:block>
  </xsl:template>

  <xsl:template match="uri">
    <!--fo:block text-align="center"-->
    <fo:block keep-with-next="always" text-align="center">

      <!--fo:inline text-align="center"-->
      <!--xsl:variable name="graphic" select="e/representations/e/parts/e/uri"/-->

      <!-- Is there a description element of type Figure for this TaxonNode?-->

      <xsl:variable name="graphic" select="."/>

      <fo:external-graphic content-height="scale-to-fit" height="{$graphic-height}"
        scaling="uniform" src="{$graphic}" padding-before="30" padding-after="2"
        display-align="center"/>
      <!--/fo:inline-->
    </fo:block>
    <fo:block>
      <fo:leader leader-pattern="rule" leader-alignment="{$taxon-page-inner-margin}"
        rule-thickness="0.8pt" leader-length="114mm"/>
    </fo:block>
    <fo:block>

      <!--fo:leader leader-pattern="rule" leader-length="120mm"/-->
      <fo:inline font-size="{$taxon-name-size-font}">
        <!--go back up to the description element and get the text for the Figure legend -->
        <xsl:apply-templates select="../../../../../../../multilanguageText_L10n/text"/>
      </fo:inline>
    </fo:block>
  </xsl:template>


  <xsl:template match="text">
    <!--fo:block font-size="9pt" space-after="5mm" -->
    <!--xsl:apply-templates select="node()"/-->
    <xsl:call-template name="add-italics">
      <xsl:with-param name="str" select="."/>
    </xsl:call-template>
    <!--/fo:block -->
  </xsl:template>

  <!-- TODO Make this templates shorter by creating generic template for replacing html codes 2013 and 2014-->
  <xsl:template name="add-italics">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="contains($str,&quot;&lt;i&gt;&quot;)">
        <xsl:variable name="before-first-i" select="substring-before($str,&quot;&lt;i&gt;&quot;)"/>
        <xsl:variable name="inside-first-i"
          select="substring-before(substring-after
          ($str,'&lt;i&gt;'),'&lt;/i&gt;')"/>
        <xsl:variable name="after-first-i" select="substring-after($str,&quot;&lt;/i&gt;&quot;)"/>
        <xsl:choose>
          <xsl:when test="contains($before-first-i, '#x2014;')">
            <xsl:call-template name="replace-mdash-html">
              <xsl:with-param name="value" select="$before-first-i"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="contains($before-first-i, '#x2013;')">
                <xsl:call-template name="replace-ndash-html">
                  <xsl:with-param name="value" select="$before-first-i"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$before-first-i"/>
              </xsl:otherwise>
            </xsl:choose>
            <!--xsl:value-of select="$before-first-i"/></xsl:otherwise-->
          </xsl:otherwise>
        </xsl:choose>
        <fo:inline font-style="italic">
          <xsl:value-of select="$inside-first-i"/>
        </fo:inline>
        <xsl:call-template name="add-italics">
          <xsl:with-param name="str" select="$after-first-i"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($str, '#x2014;')">
            <xsl:call-template name="replace-mdash-html">
              <xsl:with-param name="value" select="$str"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="contains($str, '#x2013;')">
                <xsl:call-template name="replace-ndash-html">
                  <xsl:with-param name="value" select="$str"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$str"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="replace-mdash-html-new">
    <xsl:param name="value"/>
    <xsl:choose>
      <xsl:when test="contains($value, '#x2013;')">
        <xsl:variable name="replaced-ndash"
          select="concat(substring-before($value, '&amp;#x2013;'), '&ndash;', substring-after($value, '&amp;#x2013;'))"/>
        <xsl:value-of
          select="concat(substring-before($replaced-ndash, '&amp;#x2014;'), '&mdash;', substring-after($replaced-ndash, '&amp;#x2014;'))"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
          select="concat(substring-before($value, '&amp;#x2014;'), '&mdash;', substring-after($value, '&amp;#x2014;'))"
        />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="replace-ndash-html">
    <xsl:param name="value"/>
    <xsl:value-of
      select="concat(substring-before($value, '&amp;#x2013;'), '&ndash;', substring-after($value, '&amp;#x2013;'))"
    />
  </xsl:template>

  <xsl:template name="replace-mdash-html">
    <xsl:param name="value"/>
    <xsl:value-of
      select="concat(substring-before($value, '&amp;#x2014;'), '&mdash;', substring-after($value, '&amp;#x2014;'))"
    />
  </xsl:template>

  <xsl:template name="remove_ampersands">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="contains($str,&quot;amp;&quot;)"> </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="remove">
    <xsl:param name="value"/>
    <xsl:value-of select="concat(substring-before($value, 'amp;'), substring-after($value, 'amp;'))"
    />
  </xsl:template>

  <!--xsl:template match="i">
    <fo:inline font-style="italic">
      <xsl:apply-templates select="node()"/>
    </fo:inline>
  </xsl:template-->

  <xsl:template match="i">
    <fo:inline font-style="italic">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="i//text()">
    <fo:inline font-style="italic">
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>


  <xsl:template name="commonTaxonName">
    <fo:inline font-weight="bold">
      <xsl:value-of select="representation_L10n"/>
    </fo:inline>
    <xsl:text> &mdash; </xsl:text>
    <xsl:for-each select="descriptionelements/descriptionelement">
      <fo:inline>
        <xsl:choose>
          <xsl:when test="position() != last()">
            <xsl:value-of select="name"/> (<xsl:value-of select="language/representation_L10n"/>), </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="name"/> (<xsl:value-of select="language/representation_L10n"
            />).</xsl:otherwise>
        </xsl:choose>
      </fo:inline>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="commonTaxonNameold2">
    <fo:inline font-weight="bold">
      <xsl:value-of select="representation_L10n"/>
    </fo:inline>
    <xsl:text> &mdash; </xsl:text>
    <xsl:for-each select="descriptionelements/descriptionelement">
      <fo:inline>
        <xsl:value-of select="name"/> (<xsl:value-of select="language/representation_L10n"/>) <xsl:choose>
          <xsl:when test="position() != last()">,</xsl:when>
          <xsl:otherwise>.</xsl:otherwise>
        </xsl:choose>
      </fo:inline>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="commonTaxonNameold">
    <fo:inline font-weight="bold">
      <xsl:value-of select="representation_L10n"/>
    </fo:inline>
    <xsl:text> &mdash; </xsl:text>
    <xsl:for-each select="descriptionelements/descriptionelement">
      <fo:inline>
        <xsl:value-of select="name"/> (<xsl:value-of select="language/representation_L10n"/>),
      </fo:inline>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="textData">
    <fo:block text-align="justify" margin-bottom="5mm">
      <!-- show all feature headlines except "ditribution" -->
      <!--xsl:if test="uuid!='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'">
        <fo:inline font-weight="bold">
          <xsl:value-of select="representation_L10n"/>
        </fo:inline>
        <xsl:text> – </xsl:text>
      </xsl:if-->
      <xsl:choose>
        <!-- 9fc9d10c-ba50-49ee-b174-ce83fc3f80c6 is Distribution -->
        <xsl:when test="uuid!='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'">
          <fo:inline font-weight="bold">
            <xsl:value-of select="representation_L10n"/>
            <!-- e.g. Habitat -->
          </fo:inline>
          <xsl:text> &mdash; </xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <fo:inline font-weight="bold">
            <xsl:text> Matériel examiné </xsl:text>
          </fo:inline>
        </xsl:otherwise>

      </xsl:choose>
      <!--xsl:for-each select="descriptionelements/descriptionelement">
        <fo:inline>
          <xsl:text>LINE 609</xsl:text>
          <xsl:apply-templates select="multilanguageText_L10n/text"/>
        </fo:inline>
      </xsl:for-each-->

      <!-- LORNA TRY IMAGE HERE -->
      <!--xsl:apply-templates select="descriptionelements/descriptionelement[1]/media/e/representations/e/parts/e/uri"/-->

      <xsl:apply-templates
        select="descriptionelements/descriptionelement[1]/multilanguageText_L10n/text"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="key" name="key">
    <!--fo:block margin-bottom="5mm" line-height="{$taxon-name-line-height}" font-size="{$taxon-name-size-font}"-->
    <fo:block linefeed-treatment="preserve">

      <xsl:if test="ArrayList/e">
        <fo:table>
          <fo:table-column column-width="5mm"/>
          <fo:table-column column-width="5mm"/>
          <fo:table-column column-width="69mm"/>
          <fo:table-column column-width="36mm"/>
          <fo:table-body>
            <!--xsl:if test="ArrayList/e"-->
            <xsl:for-each select="ArrayList/e">

              <!-- TaxonLinkDto or PolytomousKeyNodeLinkDto-->

              <fo:table-row>
                <fo:table-cell>
                  <fo:block>
                    <xsl:if test="edgeNumber = 1">
                      <xsl:value-of select="nodeNumber"/>
                      <xsl:text>.</xsl:text>
                    </xsl:if>
                  </fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block>&ndash;</fo:block>
                </fo:table-cell>
                <fo:table-cell>
                  <!--fo:block>
                    <xsl:apply-templates select="childStatement"/-->
                  <!--xsl:value-of select="concat(childStatement, '..............')"/-->
                  <!--xsl:text>.......................................................................................</xsl:text>
                  </fo:block-->
                  <fo:block text-align="justify" text-align-last="justify">
                    <!-- leader-length="auto"-->
                    <xsl:value-of select="childStatement"/>
                    <xsl:text> </xsl:text>
                    <fo:leader leader-pattern="dots"/>
                  </fo:block>

                </fo:table-cell>
                <fo:table-cell display-align="after">
                  <fo:block font-style="italic" text-align="justify" text-align-last="justify">
                    <!-- leader-length="auto" text-align="right"-->
                    <fo:leader leader-pattern="dots"/>
                    <xsl:choose>
                      <xsl:when test="links/e[1]/class = 'PolytomousKeyNodeLinkDto'">
                        <xsl:value-of select="links/e[1]/nodeNumber"/>
                      </xsl:when>
                      <xsl:when test="links/e[1]/class = 'TaxonLinkDto'">
                        <xsl:variable name="taxonUuid" select="links/e[1]/uuid"/>
                        <xsl:variable name="genus" select="//Taxon/uuid[.=$taxonUuid]/../name/genusOrUninomial"/>
                        <xsl:choose>
                          <xsl:when test="taxonUuid = $uuidSubgenus">
                            <xsl:variable name="repr" select="//Taxon/uuid[.='71cd0e8d-47eb-4c66-829a-e21c705ee660']/../name/rank/representation_L10n"/>
                            <xsl:value-of select="concat($substring($genus,1,1), '. ', $repr)"/>
                          </xsl:when>
                          <xsl:when test="taxonUuid = $uuidGenus">
                              <fo:block font-weight="bold" text-align="center" text-transform="uppercase">
                                <xsl:apply-templates select="$genus"/>
                              </fo:block>
                            </xsl:when>
                          <xsl:otherwise>
                            <xsl:variable name="specificEpithet" select="//Taxon/uuid[.=$taxonUuid]/../name/specificEpithet"/>
                            <!-- abbreviate the genus for species names -->
                            <xsl:value-of select="concat(substring($genus,1,1), '. ', $specificEpithet)"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:when>
                      <xsl:otherwise/>
                    </xsl:choose>
                  </fo:block>
                </fo:table-cell>
              </fo:table-row>
            </xsl:for-each>

          </fo:table-body>
        </fo:table>
        <fo:leader leader-pattern="rule" leader-length="100%"/>
        <xsl:text>&#xA;</xsl:text>
      </xsl:if>
      <!-- new line for the end of the section -->
      <xsl:text>&#xA;</xsl:text>
    </fo:block>
  </xsl:template>


  <!-- SYNONYMY BLOCK -->

  <xsl:template match="synonymy" name="synonymy">
    <fo:block margin-bottom="5mm" line-height="{$taxon-name-line-height}"
      font-size="{$taxon-name-size-font}">
      <fo:block text-align="justify" text-indent="-{$taxon-name-indentation}"
        start-indent="{$taxon-name-indentation}">
        <xsl:apply-templates select="../name"/>
        <!--xsl:call-template name="citations"/-->
        <!-- 99b2842f-9aa7-42fa-bd5f-7285311e0101 is Citation -->
        <xsl:call-template name="citations">
          <xsl:with-param name="descriptionelements"
            select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
          <xsl:with-param name="name-uuid" select="../name/uuid"/>
        </xsl:call-template>
        <xsl:text>.**********</xsl:text>
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
        <xsl:with-param name="descriptionelements"
          select="../../../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
        <xsl:with-param name="name-uuid" select="name/uuid"/>
      </xsl:call-template>
    </xsl:for-each>

    <!--xsl:apply-templates select="e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates-->
  </xsl:template>

  <xsl:template match="heterotypicSynonymyGroups">
    <xsl:for-each select="e">
      <fo:block text-align="start" text-indent="-{$taxon-name-indentation}"
        start-indent="{$taxon-name-indentation}">
        <xsl:for-each select="e">
          <xsl:apply-templates select="name"/>
          <xsl:call-template name="citations">
            <xsl:with-param name="descriptionelements"
              select="../../../../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements"/>
            <xsl:with-param name="name-uuid" select="name/uuid"/>
          </xsl:call-template>
          <xsl:apply-templates select="name/typeDesignations"/>
        </xsl:for-each>
        <!--xsl:apply-templates select="e[1]/name/typeDesignations" /-->
      </fo:block>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="citations">
    <xsl:param name="name-uuid"/>
    <xsl:param name="descriptionelements"/>
    <!--xsl:for-each
      select="../descriptions/features/feature[uuid='99b2842f-9aa7-42fa-bd5f-7285311e0101']/descriptionelements/descriptionelement"-->
    <xsl:for-each select="$descriptionelements/descriptionelement">
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
                <xsl:when test="position() != last()">
                  <xsl:text> &amp; </xsl:text>
                </xsl:when>
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
