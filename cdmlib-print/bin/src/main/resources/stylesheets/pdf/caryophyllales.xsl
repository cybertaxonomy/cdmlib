<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [<!ENTITY mdash "&#x2014;" > <!ENTITY ndash "&#x2013;" > <!ENTITY ndash "&#x2715;" >]> 

<!--
  
  CDM XSL Transformation
  Target Format: Flore d'Afrique Centrale
  
-->
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="http://exslt.org/common" 
  xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  
  <xsl:param name="abstract"/>
  <xsl:param name="title"/>
  

  <!-- ############### Constants and definitions ################### -->

  <!-- **********************************
    ** global definitions 
    *********************************** -->
  <xsl:param name="global-family-font">Times</xsl:param>
  <xsl:param name="global-size-font">10pt</xsl:param>
  <xsl:param name="global-title-font">10pt</xsl:param>
  <xsl:param name="global-line-height">12pt</xsl:param>

  <xsl:param name="global-width-page">159mm</xsl:param>
  <xsl:param name="global-height-page">244mm</xsl:param>

  <xsl:param name="global-indentation">0mm</xsl:param>

  <!-- **********************************   
    ** definitions for taxon pages
    *********************************** -->
  <xsl:param name="taxon-first-page-top-margin">30mm</xsl:param>
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

  <xsl:param name="distribution-size-font">8pt</xsl:param>

  <xsl:param name="taxon-name-indentation">4mm</xsl:param>
  <xsl:param name="taxon-name-size-font">8pt</xsl:param>
  <xsl:param name="taxon-name-line-height">9.6pt</xsl:param>

  <xsl:param name="taxon-header-style-font">italic</xsl:param>
  <xsl:param name="taxon-header-page-number-size-font">10pt</xsl:param>
  <xsl:param name="taxon-header-page-number-align-text">end</xsl:param>
  <xsl:param name="taxon-header-page-number-margin">11mm</xsl:param>

  <xsl:param name="taxon-page-number-initial">1</xsl:param>

  <!-- hardcoded ranks -->
  <xsl:param name="uuidFamily">af5f2481-3192-403f-ae65-7c957a0f02b6</xsl:param>
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
        <fo:simple-page-master master-name="taxon_page_first" page-height="{$global-height-page}"
          page-width="{$global-width-page}" margin-top="{$taxon-first-page-top-margin}"
          margin-bottom="{$taxon-page-bottom-margin}" margin-left="{$taxon-page-inner-margin}">
          <fo:region-body margin-right="{$taxon-region-body-outer-margin}"/>
          <fo:region-before extent="{$taxon-region-before-extent}" region-name="odd-before"/>
          <fo:region-after extent="{$taxon-region-after-extent}" region-name="odd-after"/>
        </fo:simple-page-master>

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
            <fo:conditional-page-master-reference master-reference="taxon_page_first"
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
          
          <!--fo:block><xsl:value-of select="$abstract"/></fo:block-->
          
          <fo:block font-family="{$global-family-font}" font-size="{$global-size-font}"
            line-height="{$global-line-height}" linefeed-treatment="preserve">
            
            <fo:block text-align="center" font-weight="bold" font-size="global-title-font" linefeed-treatment="preserve">            
              <xsl:value-of select="$title"/>
              <xsl:text>&#xA;</xsl:text>
              <xsl:text>&#xA;</xsl:text>
            </fo:block>
            <!--fo:block linefeed-treatment="preserve">
            <fo:inline font-weight="bold"><xsl:text>Abstract. </xsl:text></fo:inline>
            <xsl:value-of select="$abstract"/>
              <xsl:text>&#xA;</xsl:text>
            </fo:block-->
            
            <xsl:for-each select="//TaxonNode">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
            
            <fo:block text-align="center" text-transform="uppercase" font-weight="bold" linefeed-treatment="preserve">
              <xsl:text>&#xA;</xsl:text>
              <xsl:text>REFERENCES</xsl:text>             
            </fo:block>
            <xsl:text>&#xA;</xsl:text>
            <xsl:call-template name="References"/>
            
            <fo:block text-align="center" font-weight="bold" linefeed-treatment="preserve">
              <xsl:text>&#xA;</xsl:text>
              <xsl:text>Noms scientifiques</xsl:text>             
            </fo:block>
            <xsl:text>&#xA;</xsl:text>
            <xsl:call-template name="scientific-name-index"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <!-- TODO shorten this so that not repeating code for creating author in each xsl:when statement -->
  <xsl:template name="scientific-name-index">   
    <!-- create a variable for the sorted scientific names for the index, then once sorted can select preceding to get the first occurance of 
    the genusOrUninomial in the list-->
    <xsl:variable name="sortedcopy">
      <xsl:for-each select="//name">
        <xsl:sort select="genusOrUninomial" data-type="text" order="ascending"></xsl:sort>
        <xsl:sort select="specificEpithet" data-type="text" order="ascending"></xsl:sort>
        <xsl:copy-of select="."/>       
      </xsl:for-each>
    </xsl:variable>  

    <fo:block margin-bottom="5mm" text-align="justify">    
      <xsl:for-each select="$sortedcopy/*"><!-- or select="//name/genusOrUninomial-->
         
        <xsl:variable name="genus" select="."/>
        <xsl:choose>
          <!-- family -->
          <xsl:when test="rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
            <fo:inline font-style="italic">
            <xsl:apply-templates select="genusOrUninomial"/>
            </fo:inline>
            <xsl:for-each select="taggedName/e">
              <xsl:if test="type='authors'">
                <xsl:text> </xsl:text>
                <xsl:value-of select="text"/>
              </xsl:if>              
            </xsl:for-each>
          </xsl:when>
          <!-- genus -->
          <xsl:when test="rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
            <fo:block>
              <fo:inline font-style="italic">
              <xsl:apply-templates select="genusOrUninomial"/>
              </fo:inline>
              <xsl:for-each select="taggedName/e">
                <xsl:if test="type='authors'">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="text"/>
                </xsl:if>              
              </xsl:for-each>
            </fo:block>
          </xsl:when>
          <!-- subgenus -->
          <xsl:when test="rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
            <fo:block>
              <xsl:apply-templates select="rank/representation_L10n"/>
              <xsl:text> </xsl:text>
              <fo:inline font-style="italic">
              <xsl:apply-templates select="genusOrUninomial"/>
              </fo:inline>
              <xsl:for-each select="taggedName/e">
                <xsl:if test="type='authors'">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="text"/>
                </xsl:if>              
              </xsl:for-each>
            </fo:block>            
          </xsl:when>
          <!-- species -->
          <xsl:when test="rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
            
            <!--first occurance of the genusOrUninomial in the list is not indented -->
              <xsl:choose>
                <xsl:when test="not(preceding::genusOrUninomial[1] = genusOrUninomial)">
                <fo:block>
                  <fo:inline font-style="italic">
                <xsl:apply-templates select="genusOrUninomial"/>
                <xsl:text> </xsl:text>
                  <xsl:apply-templates select="specificEpithet"/>
                  </fo:inline>
                  <xsl:text> </xsl:text>
                  <!--xsl:apply-templates select="rank/titleCache"/-->
                  <xsl:for-each select="taggedName/e">
                    <xsl:if test="type='authors'">
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="text"/>
                    </xsl:if>              
                  </xsl:for-each>
                    
                </fo:block>
                </xsl:when>
                <xsl:otherwise>
                  <fo:block text-indent="{$taxon-name-indentation}">
                    <fo:inline font-style="italic">
                      <xsl:apply-templates select="specificEpithet"/>
                    </fo:inline>
                      <xsl:text> </xsl:text>
                      <!--xsl:apply-templates select="rank/titleCache"/--><!-- for debugging -->
                    <xsl:for-each select="taggedName/e">
                      <xsl:if test="type='authors'">
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="text"/>
                      </xsl:if>              
                    </xsl:for-each>
                    </fo:block>
                </xsl:otherwise>
              </xsl:choose>
              
            
          </xsl:when>
          <!-- variant d5feb6a5-af5c-45ef-9878-bb4f36aaf490-->
          <xsl:when test="rank/uuid='d5feb6a5-af5c-45ef-9878-bb4f36aaf490'">
            <fo:block text-indent="{$taxon-name-indentation}">
              <xsl:text>&ndash; </xsl:text> <!--concat ndash-->
              <xsl:apply-templates select="rank/representation_L10n_abbreviatedLabel"/>
              <xsl:text> </xsl:text>
              <fo:inline font-style="italic">
              <xsl:apply-templates select="specificEpithet"/>
              </fo:inline>
              
              <xsl:for-each select="taggedName/e">
                <xsl:if test="type='authors'">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="text"/>
                </xsl:if>              
              </xsl:for-each>
            </fo:block>
          </xsl:when>
          <!-- sub-species 462a7819-8b00-4190-8313-88b5be81fad5-->
          <xsl:when test="rank/uuid='462a7819-8b00-4190-8313-88b5be81fad5'">
            <fo:block text-indent="{$taxon-name-indentation}">
              <!--xsl:apply-templates select="specificEpithet"/--><!--remove - just for testing -->
              <xsl:text>- </xsl:text>
              <xsl:apply-templates select="rank/representation_L10n_abbreviatedLabel"/>
              <xsl:text> </xsl:text>
              <fo:inline font-style="italic">
              <xsl:apply-templates select="infraSpecificEpithet"/>
              </fo:inline>
              <!--xsl:variable name="full-name">
              <xsl:apply-templates select="../taggedName"/> we only want to add the authors from taggedName if taggedName/e/type='author' select text
              </xsl:variable-->
              <!--fo:inline font-style="bold">
                <xsl:apply-templates select="$full-name"></xsl:apply-templates>
              </fo:inline-->
              <!--xsl:apply-templates select="substring-after(' ', $full-name)"></xsl:apply-templates-->
              <!--xsl:call-template name="dispay-author-name">
                <xsl:with-param name="e" select="../taggedName/e"/>
              </xsl:call-template-->
              <xsl:for-each select="taggedName/e">
                <xsl:if test="type='authors'">
                  <xsl:text> </xsl:text>
                  <xsl:value-of select="text"/>
                </xsl:if>              
              </xsl:for-each>
            </fo:block>
          </xsl:when>
          <!--xsl:otherwise-->
            <!-- for debugging LORNA DEBUGGING XSLT CHOOSE: <xsl:value-of
              select="../rank/uuid"/>: <xsl:value-of select="../titleCache"/>
          </xsl:otherwise!-->
        </xsl:choose>    
      </xsl:for-each>
    </fo:block>
  </xsl:template>
  

  <xsl:template name="dispay-author-name"> <!-- call this template with node e - don't display uninomial for species -->
    <xsl:param name="e"/>
      <xsl:if test="$e/type='authors'">
        <xsl:text>++++++</xsl:text>
        <xsl:value-of select="$e/text"/>
      </xsl:if>
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
    <!--xsl:call-template name="title"/--> <!-- sections don't have stitles as just creating a name list -->
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
          <fo:block font-weight="bold">
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- genus -->
        <!-- we don't show the genus as a title for the caryophyllales -->
        <xsl:when test="name/rank/uuid='1b11c34c-48a8-4efa-98d5-84f7f66ef43a'">
          <fo:block>
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- subgenus -->
        <xsl:when test="name/rank/uuid='78786e16-2a70-48af-a608-494023b91904'">
          <fo:block>
            <xsl:apply-templates select="name/rank/representation_L10n"/>
            <xsl:text> </xsl:text>
            <xsl:apply-templates select="name/genusOrUninomial"/>
          </fo:block>
        </xsl:when>
        <!-- species -->
        <xsl:when test="name/rank/uuid='b301f787-f319-4ccc-a10f-b4ed3b99a86d'">
          <fo:block>
            <xsl:apply-templates select="name/genusOrUninomial"/>
            <xsl:text> </xsl:text>
            <xsl:apply-templates select="name/specificEpithet"/>
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
          <!-- for debugging --> <!--Unformatted title for rank uuid: <xsl:value-of
            select="name/rank/uuid"/>: <xsl:value-of select="name/titleCache"/>-->
        </xsl:otherwise>
      </xsl:choose>
    </fo:block>
  </xsl:template>

  <!-- NAME -->

  <xsl:template match="name">
    
    <xsl:variable name="fontstyle">
    <xsl:choose>
    <xsl:when test="rank/uuid='af5f2481-3192-403f-ae65-7c957a0f02b6'">
      <xsl:value-of select="'bold'" />
    </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'regular'" />
      </xsl:otherwise>
    </xsl:choose>
    </xsl:variable>
    <fo:block font-weight="{$fontstyle}">
    <xsl:apply-templates select="taggedName"/>
    <xsl:apply-templates select="nomenclaturalReference"/>
    </fo:block>
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
      <xsl:value-of select="authorship/titleCache"/>
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
              <!-- The headings are in English - do we want to remove them altogether or have French headings -->
              <!--fo:inline text-decoration="underline" keep-with-next.within-line="always">

                <xsl:value-of select="representation_L10n"/>
              </fo:inline>
              <fo:inline>: </fo:inline-->
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
              <!--xsl:if test="position() = 1"-->
                <!--xsl:value-of select="../../../../../../Taxon/name/titleCache"/-->
              
             <!-- if it has the title map don't show it in the image section -->

              <xsl:if test="not(starts-with(media/e/title_L10n,'Map'))">
                <xsl:apply-templates select="media/e/representations/e/parts/e/uri"/>
              </xsl:if>
              
              
              
              
              <!--xsl:call-template name="uri">
                <xsl:with-param name="uri-node" select="media/e/representations/e/parts/e/uri"/>
              </xsl:call-template-->
              <!--/xsl:if-->
              <!--xsl:apply-templates select="e[1]/name[1]/homotypicalGroup[1]/typifiedNames[1]/e/taxonBases[1]/e/descriptions[1]/e/elements[1]/e[1]/media[1]/e/representations[1]/e/parts[1]/e/uri"></xsl:apply-templates-->

            </xsl:for-each>

            <xsl:text> </xsl:text>
          </fo:inline>
        </xsl:if>
        <!--/xsl:if-->
      </xsl:for-each>

    </fo:block>

  </xsl:template>

  <!-- IMAGES -->
  
  <xsl:template match="uri">
    <!--xsl:template name="uri"-->
      
    <xsl:param name="uri-node"/>
    <xsl:variable name="graphic" select="."/>
    <xsl:variable name="title" select="../../../../../title_L10n"/>
    <!--fo:block text-align="center"-->
    
      <!--xsl:variable name="graphic" select="e/representations/e/parts/e/uri"/-->

      <!-- Is there a description element of type Figure for this TaxonNode?-->
   
      <xsl:choose>
        
        <!--xsl:when test="contains($graphic,'jpg')"-->
        <xsl:when test="starts-with($title,'Map')">
          <fo:block keep-with-next="always" text-align="center">
            
            <!--fo:inline text-align="center"-->
            <fo:external-graphic content-height="scale-to-fit" height="50mm"
              scaling="uniform" src="{$graphic}" padding-before="30" padding-after="2"
              display-align="center"/>
            <!--/fo:inline-->
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
         
          <fo:block keep-with-next="always" text-align="center">
            
            <!--fo:inline text-align="center"-->
            <fo:external-graphic content-height="scale-to-fit" height="{$graphic-height}"
              scaling="uniform" src="{$graphic}" padding-before="30" padding-after="2"
              display-align="center"/>
            <!--/fo:inline-->
          </fo:block>
          <fo:block>
            <fo:leader leader-pattern="rule" leader-alignment="{$taxon-page-inner-margin}"
              rule-thickness="0.8pt" leader-length="114mm"/>
            
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>

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
    <!--xsl:call-template name="add-markup">
      <xsl:with-param name="str" select="."/>

      <xsl:with-param name="tag-name" select="b"/>
    </xsl:call-template-->
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

  <!-- TODO Can this template be made shorter by less nesting in the xsl:choose statements -->
  <xsl:template name="add-markup">
    <xsl:param name="str"/>
    <xsl:param name="tag-name"/>
    
    <xsl:variable name="opening-tag">
      <xsl:value-of select="concat('&lt;', $tag-name, '&gt;')"> </xsl:value-of>
    </xsl:variable>
    <xsl:variable name="closing-tag">
      <xsl:value-of select="concat('&lt;/', $tag-name, '&gt;')"> </xsl:value-of>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="contains($str, $opening-tag)">
        <xsl:variable name="before-tag" select="substring-before($str, $opening-tag)"/>
        <xsl:variable name="inside-tag"
          select="substring-before(substring-after($str,$opening-tag),$closing-tag)"/>
        <xsl:variable name="after-tag" select="substring-after($str, $closing-tag)"/>
        <xsl:choose>
          <xsl:when test="contains($before-tag, '#x2014;')">
            <xsl:call-template name="replace-string">
              <xsl:with-param name="text" select="$before-tag"/>
              <xsl:with-param name="replace" select="'&amp;#x2014;'" />
              <xsl:with-param name="with" select="'&mdash;'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="contains($before-tag, '#x2013;')">
                <xsl:call-template name="replace-string">
                  <xsl:with-param name="text" select="$before-tag"/>
                  <xsl:with-param name="replace" select="'&amp;#x2013;'" />
                  <xsl:with-param name="with" select="'&ndash;'"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="contains($str, '#x2716;')">
                    <xsl:call-template name="replace-string">
                      <xsl:with-param name="text" select="$before-tag"/>
                      <xsl:with-param name="replace" select="'&amp;#x2715;'" />
                      <xsl:with-param name="with" select="'&ndash;'"/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$before-tag"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
        <!-- add BOLD or italics when inside the appropriate tags -->
        <xsl:choose>
          <xsl:when test="$tag-name = 'b'">
            <fo:inline font-weight="bold">
              <xsl:value-of select="$inside-tag"/>
            </fo:inline>
          </xsl:when>
          <xsl:otherwise>
            <fo:inline font-style="italic">
              <xsl:value-of select="$inside-tag"/>
            </fo:inline>
          </xsl:otherwise>
        </xsl:choose>
        <!-- call template recursively with the remaining text after the tag -->
        <xsl:call-template name="add-markup">
          <xsl:with-param name="str" select="$after-tag"/>
          <xsl:with-param name="tag-name" select="$tag-name"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($str, '#x2014;')">
            <xsl:call-template name="replace-string">
              <xsl:with-param name="text" select="$before-tag"/>
              <xsl:with-param name="replace" select="'&amp;#x2014;'" />
              <xsl:with-param name="with" select="'&mdash;'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="contains($str, '#x2013;')">
                <xsl:call-template name="replace-string">
                  <xsl:with-param name="text" select="$before-tag"/>
                  <xsl:with-param name="replace" select="'&amp;#x2013;'" />
                  <xsl:with-param name="with" select="'&ndash;'"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:choose>
                  <xsl:when test="contains($str, '#x2716;')">
                    <xsl:call-template name="replace-string">
                      <xsl:with-param name="text" select="$before-tag"/>
                      <xsl:with-param name="replace" select="'&amp;#x2715;'" />
                      <xsl:with-param name="with" select="'&ndash;'"/>
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
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text"
            select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
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
          <fo:inline font-weight="bold" keep-with-next="always">
            <xsl:text> Matériel examiné </xsl:text>
            <xsl:text>&#xA;</xsl:text>
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
      
      <!-- get the map associated with the distribution, this is temporarily stored as a media object jpg created by 
        Quentin's GIS software -->
      <xsl:if test="uuid='9fc9d10c-ba50-49ee-b174-ce83fc3f80c6'">
        
        <!-- get the map attached to the feature 'Figures' -->
        <xsl:apply-templates
          select="../feature/feature/representation_L10n[.='Figures']/../descriptionelements/descriptionelement[1]/media/e/representations/e/parts/e/uri"/>
        <!--media/e/representations/e/parts/e/uri media/e/title_L10n[.='Map']/../representations/e/parts/e/uri"/-->
      </xsl:if>
      
    </fo:block>
  </xsl:template>

  <xsl:template match="key" name="key">
    <!--fo:block margin-bottom="5mm" line-height="{$taxon-name-line-height}" font-size="{$taxon-name-size-font}"-->
    <fo:block linefeed-treatment="preserve">

      <fo:block font-weight="bold" text-align="center">
        <xsl:value-of select="HashMap/titleCache"/>
      </fo:block>
      <xsl:if test="HashMap/records/e">
        <xsl:text>&#xA;</xsl:text>
        <fo:table>
          <fo:table-column column-width="5mm"/>
          <fo:table-column column-width="5mm"/>
          <fo:table-column column-width="69mm"/>
          <fo:table-column column-width="36mm"/>
          <fo:table-body>
            <!--xsl:if test="ArrayList/e"-->
            <xsl:for-each select="HashMap/records/e">

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
                        <xsl:variable name="genus"
                          select="//Taxon/uuid[.=$taxonUuid]/../name/genusOrUninomial"/>
                        <xsl:choose>
                          <xsl:when test="taxonUuid = $uuidSubgenus">
                            <xsl:variable name="repr"
                              select="//Taxon/uuid[.='71cd0e8d-47eb-4c66-829a-e21c705ee660']/../name/rank/representation_L10n"/>
                            <!--xsl:value-of select="concat($substring($genus,1,1), '. ', $repr)"/-->
                            <xsl:value-of select="concat($genus, '. ', $repr)"/>
                          </xsl:when>
                          <xsl:when test="taxonUuid = $uuidGenus">
                            <fo:block font-weight="bold" text-align="center"
                              text-transform="uppercase">
                              <xsl:apply-templates select="$genus"/>
                            </fo:block>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:variable name="specificEpithet"
                              select="//Taxon/uuid[.=$taxonUuid]/../name/specificEpithet"/>
                            <!-- abbreviate the genus for species names -->
                            <!--xsl:value-of select="concat(substring($genus,1,1), '. ', $specificEpithet)"/-->
                            <xsl:value-of select="concat($genus, '. ', $specificEpithet)"/>
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
    <!--fo:block margin-bottom="5mm" line-height="{$taxon-name-line-height}"
      font-size="{$taxon-name-size-font}"-->
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
        <!--xsl:text>.**********</xsl:text-->
        <xsl:apply-templates select="homotypicSynonymsByHomotypicGroup"/>
        <xsl:apply-templates select="../name/typeDesignations"/>
      </fo:block>
      <xsl:apply-templates select="heterotypicSynonymyGroups"/>
    <!--/fo:block-->
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
        
        <xsl:variable name="lastname_text" select="citation/authorship/lastname"/>
        <xsl:variable name="prev_lastname_text" select="preceding-sibling::e[1]/citation/authorship/lastname"/>
        
        <xsl:if test="nameUsedInSource/uuid=$name-uuid">
          <xsl:text>; </xsl:text>
          <fo:inline>
            <!--xsl:value-of select="citation/authorship/titleCache"/-->
            <!--TODO wrap this in a variable and compare the previous variable to this one to see if we're dealing with the same name-->
            <xsl:for-each select="citation/authorship/teamMembers/e">
              <xsl:value-of select="lastname"/>
              <xsl:choose>
                <xsl:when test="position() != last()">
                  <xsl:text> &amp; </xsl:text>
                </xsl:when>
              </xsl:choose>
            </xsl:for-each>

<xsl:choose>
            <xsl:when test="$lastname_text != $prev_lastname_text">
            <xsl:value-of select="citation/authorship/lastname"/><!--TODO We print lastname here as well as the author list is this a mistake?-->
            
            <xsl:text> (</xsl:text>
            <xsl:value-of select="citation/datePublished/start"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="citationMicroReference"/>
            <xsl:text>)</xsl:text>
            </xsl:when>
  <xsl:otherwise>
    <xsl:text> </xsl:text><!-- TODO For the first ref with a particlar name we should open brackets -->
    <xsl:value-of select="citation/datePublished/start"/>
    <xsl:if test="citationMicroReference != ''">
      <xsl:text>: </xsl:text>
      <xsl:value-of select="citationMicroReference"/>
    </xsl:if>
  </xsl:otherwise>
</xsl:choose>
          </fo:inline>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="citationsworks">
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
            <!--xsl:value-of select="citation/authorship/titleCache"/-->
            <xsl:for-each select="citation/authorship/teamMembers/e">
              <xsl:value-of select="lastname"/>
              <xsl:choose>
                <xsl:when test="position() != last()">
                  <xsl:text> &amp; </xsl:text>
                </xsl:when>
              </xsl:choose>
            </xsl:for-each>
            
            <xsl:value-of select="citation/authorship/lastname"/>
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

  
  <!-- this template is for the list of all citations at the end of the PDF -->
  <!-- references are under //citation and under //nomenclaturalReference and //inReference-->
  <!-- could try //class[.='Reference']/parent::node() to get all References -->
  <xsl:key name="citations-by-uuid" match="//citation | //nomenclaturalReference" use="uuid" />
  <!--xsl:key name="nomenclaturalrefs-by-uuid" match="//nomenclaturalReference" use="uuid" /-->

  <xsl:template name="References"> 
    <!-- new line for the end of the section -->
    <xsl:text>&#xA;</xsl:text>
        
    <!--nomenclaturalReference or citation-->
    <!-- problem with this is that if the same reference occurs under citaiton and under nomenclaturalReference it appears twice -->
    <!--xsl:for-each select="//nomenclaturalReference[count(. | key('nomenclaturalrefs-by-uuid', uuid)[1]) = 1] | //citation[count(. | key('citations-by-uuid', uuid)[1]) = 1]"-->
    
    <xsl:for-each select="//nomenclaturalReference[count(. | key('citations-by-uuid', uuid)[1]) = 1] | //citation[count(. | key('citations-by-uuid', uuid)[1]) = 1]">
    <!--xsl:for-each select="//nomenclaturalReference[count(. | key('nomenclaturalrefs-by-uuid', uuid)[1]) = 1]"-->
      <!--xsl:for-each select="//nomenclaturalReference"-->
        <xsl:sort select="authorship/lastname | authorship/teamMembers/e[1]/lastname" />
      <xsl:sort select="datePublished/start"></xsl:sort>

      <fo:block linefeed-treatment="preserve" text-align="justify" text-indent="-{$taxon-name-indentation}" start-indent="{$taxon-name-indentation}">
        
        <fo:inline>        
          <!-- filter out repeated citation uuids. Could write a controller method in the CDM to get all unique references for a TaxonNode -->
          
          <!--I am only listing references which have at least one author name. If there are other references in the database - why don't these have an author name-->
              <xsl:if test="authorship/teamMembers/e[1]/lastname != '' or authorship/lastname != ''">               
                <!--xsl:text>&#xA;</xsl:text-->
                <xsl:choose>
                  <xsl:when test="authorship/teamMembers/e[1]/lastname != ''">
                    <xsl:for-each select="authorship/teamMembers/e">
                      <fo:inline>
                        <xsl:value-of select="lastname"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="firstname"/>
                        <xsl:choose>
                          <xsl:when test="position() != last()">
                            <xsl:text> &amp; </xsl:text>
                          </xsl:when>
                        </xsl:choose>
                      </fo:inline>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise test="authorship/lastname != ''">
                    <fo:inline>
                      <xsl:value-of select="authorship/lastname"/>
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="authorship/firstname"/>
                    </fo:inline>                                 
                  </xsl:otherwise>
                </xsl:choose>                            
                
                <xsl:if test="datePublished/start != ''">
                  <xsl:text> (</xsl:text>
                  <xsl:value-of select="datePublished/start"/>
                  <xsl:text>) </xsl:text>
                </xsl:if>                
                
                <xsl:apply-templates select="title"/>
                <xsl:apply-templates select="volume"/>
                <xsl:apply-templates select="pages"/>
                <xsl:apply-templates select="placePublished"/>
                <xsl:apply-templates select="publisher"/>
                
                <!-- if inReference has child nodes-->
                <xsl:if test="count(inReference/*) &gt; 0">
                  
                  <xsl:text>In </xsl:text>
                  <xsl:apply-templates select="inReference/title"/>
                  <xsl:apply-templates select="inReference/volume"/>
                  <xsl:apply-templates select="inReference/pages"/>
                  <xsl:apply-templates select="inReference/placePublished"/>
                  <xsl:apply-templates select="inReference/publisher"/>
                </xsl:if>
                <!--add template match to self:: that works for the above whether it's a citation or nomenclaturalReference -->              
                <!-- new line for the end of the section -->
                <xsl:text>&#xA;</xsl:text>             
              </xsl:if>                  
        </fo:inline>      
      </fo:block>     
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="*">

    <xsl:value-of select="."/>
      
    <xsl:if test="name(.) = 'title' or name(.) = 'publisher' or name(.) = 'pages'">
      <!-- . if pages or publisher or title - comma if placePublished -->
      <xsl:text>. </xsl:text> 
    </xsl:if>
    
    <xsl:if test="name(.) = 'volume'">
      <xsl:text>: </xsl:text>
    </xsl:if>
    
    <xsl:if test="(../type = 'Book' or ../type = 'BookSection') and name(.) = 'placePublished' and . != ''">
      <xsl:text>, </xsl:text> 
    </xsl:if>
   

  </xsl:template>
  

  <xsl:template name="Referencesold">
    
      <!-- need to sort by lastname of the first author i.e. //citation/authorship/teamMembers/e[1]/lastname -->
      <xsl:for-each select="//citation">

        <!-- TODO sorting only works for the first citation, implement correctly -->
        <xsl:sort select="authorship/lastname"/>
        <xsl:sort select="authorship/teamMembers/e[1]/lastname"/>
        <fo:block>
        <fo:inline>
          
          <!-- filter out repeated citation uuids. Could write a controller method in the CDM to get all unique references for a TaxonNode -->
          <xsl:variable name="prev_citation_uuid" select="preceding-sibling::citation/uuid"/>
          <xsl:variable name="citation_uuid" select="uuid"/>
          <xsl:text>preceeding:</xsl:text>
          <xsl:value-of select="preceding-sibling::citation[1]/uuid"></xsl:value-of>
          <xsl:text>current:</xsl:text>
          <xsl:value-of select="uuid"></xsl:value-of>

          <!--xsl:value-of select="preceding-sibling::citation/uuid"></xsl:value-of-->

          <!--xsl:if test="$citation_uuid != $prev_citation_uuid"-->
          <xsl:if test="preceding-sibling::citation[1]/uuid  != uuid">   
            
           <!-- .[not(preceding-sibling::Link[@personId   = current()/@personId -->
          <xsl:choose>
                         
            <xsl:when test="authorship/teamMembers/e[1]/lastname != '' or authorship/lastname != ''">                                     
            
              <xsl:choose>
                <xsl:when test="authorship/teamMembers/e[1]/lastname != ''">
                <xsl:for-each select="authorship/teamMembers/e">
                  <fo:inline font-weight="bold">
                    <xsl:value-of select="lastname"/>
                    <xsl:choose>
                      <xsl:when test="position() != last()">
                        <xsl:text> &amp; </xsl:text>
                      </xsl:when>
                    </xsl:choose>
                  </fo:inline>
                </xsl:for-each>
                </xsl:when>
                <xsl:otherwise test="authorship/lastname != ''">
                  <fo:inline font-weight="bold">
                    <xsl:value-of select="authorship/lastname"/>
                  </fo:inline>                                 
                </xsl:otherwise>
              </xsl:choose>                            
              
              <xsl:if test="datePublished/start != ''">
                <xsl:text> (</xsl:text>
                <xsl:value-of select="datePublished/start"/>
                <xsl:text>) </xsl:text>
              </xsl:if>
              <xsl:value-of select="title"/>
              <xsl:text>.</xsl:text>
              <xsl:value-of select="pages"/>
              <xsl:text>.</xsl:text>
              
              <!-- new line for the end of the section -->
              <xsl:text>&#xA;</xsl:text>
              
            </xsl:when>
            <!--xsl:otherwise>
              <xsl:text>HELLO&#xA;</xsl:text>
            </xsl:otherwise-->
          </xsl:choose>
          </xsl:if>
          
        </fo:inline>

    </fo:block>
    <xsl:text>&#xA;</xsl:text>
      </xsl:for-each>
      



  </xsl:template>

  <xsl:template match="typeDesignations">
    <xsl:for-each select="child::*">
      <fo:inline>
        
        <xsl:choose>
          <xsl:when test="class='SpecimenTypeDesignation'">
            <xsl:text> - Typus: </xsl:text>
            <xsl:value-of select="typeSpecimen/titleCache"/>
          </xsl:when>
          <xsl:when test="class='NameTypeDesignation'">
            <xsl:text> - Typus: </xsl:text>
            <xsl:value-of select="typeName/titleCache"/>
          </xsl:when>
        </xsl:choose>
      </fo:inline>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>
