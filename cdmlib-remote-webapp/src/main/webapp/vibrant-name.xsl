<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:http="http://expath.org/ns/http-client" version="1.0">

    <!-- pass this in as a parameter? -->
    <xsl:variable name="url_portal"> http://160.45.63.151:8080/cichorieae/</xsl:variable>
    <xsl:variable name="url_taxon">http://localhost:8080/taxon/findTaxaAndNames</xsl:variable>
    <xsl:variable name="url_webservice">http://localhost:8080/name/findTitleCache</xsl:variable>
    <!--xsl:param name="matchMode"/-->
    <xsl:param name="url"/>

    <xsl:variable name="vReps" select="document('')/xsl:param[@name='matchMode']"/>

    <!--xsl:param name="response" select="element(http:response)"/ -->

    <!--xsl:call-template name="doMoreStuff">
            <xsl:with-param name="param1" select="document($url)/foo"/>
    </xsl:call-template-->


    <xsl:template match="/DefaultPagerImpl">
        <HTML>
            <HEAD>
                <link type="text/css" rel="stylesheet" media="all" href="/vibrant.css"/>
            </HEAD>
            <BODY>
                <a href="/vibrant_names.html" title="Home">
                    <img src="http://localhost:8080/acquia_prosper_logo.png" alt="Home"/>
                </a>
                <br/>
                <table cellpadding="1" width="100%">
                    <tr>
                        <td width="5%"/>
                        <td valign="top">
                            <h2>ViBRANT Common Data Model names search</h2>
                            <strong>
                                <xsl:value-of select="$vReps"/>
                            </strong>
                            <br/>
                            <H3>Search results:</H3>
                            <p><strong><xsl:value-of select="count"/></strong> unique names found.
                                Click on name for details of source.</p>
                            <br/>

                            <TABLE border="0">
                                <xsl:for-each select="records/e">

                            <!--xsl:variable name="url">
                                <xsl:value-of select="$url_portal"/>name/<xsl:value-of select="uuid"
                                />
                            </xsl:variable-->

                                    <TR>
                                        <TD> </TD>
                                        <TD>
                                            <a>
                                                <xsl:variable name="titleCacheString"><xsl:value-of select="."></xsl:value-of></xsl:variable>
                                                <xsl:variable name="genus"><xsl:value-of select="substring-before($titleCacheString,' ')"></xsl:value-of></xsl:variable>
                                                <xsl:variable name="specificEpithet"><xsl:value-of select="substring-before(normalize-space(substring-after($titleCacheString, $genus)),' ')"></xsl:value-of></xsl:variable>
                                                
                                                
                                                <xsl:variable name="query_string_taxon"><xsl:value-of select="$url_taxon"
                                                />?query=<xsl:value-of select="$genus"/>%20<xsl:value-of select="$specificEpithet"/>&amp;matchMode=BEGINNING&amp;doSynonyms=1</xsl:variable>
                                                    
                                               <!--     ?query=Lapsana%20communis%20var.%20aurantia&amp;matchMode=BEGINNING&amp;doSynonyms=1</xsl:variable> -->
                                                
                                                
                                                <xsl:attribute name="href">
                                                    <!--xsl:value-of select="$url_local"/>name/<xsl:value-of select="../uuid"/-->
                                                    <xsl:value-of select="$query_string_taxon"/>
                                                </xsl:attribute>
                                                <xsl:apply-templates select="."/>
                                            </a>
                                        </TD>
                                    </TR>
                                </xsl:for-each>
                                <TR>
                                    <TD/>
                                    <TD>
                                        <!--xsl:apply-templates select="pagesAvailable"/-->
                                        <!--xsl:variable name="NumberpagesAvailable"><xsl:value-of select="pagesAvailable"/>
                                </xsl:variable>
                                <xsl:call-template name="pagesAvailable">
                                    <xsl:with-param name="index" select="$NumberpagesAvailable"/>
                                    
                                </xsl:call-template-->
                                        <xsl:apply-templates select="indices"/>
                                    </TD>
                                </TR>
                            </TABLE>

                        </td>
                    </tr>
                </table>
            </BODY>
        </HTML>
    </xsl:template>

    <xsl:template match="titleCache">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="$url_portal"/>name/<xsl:value-of
                    select="../uuid"/></xsl:attribute>
            <xsl:value-of select="."/>
        </a>
    </xsl:template>

    <!-- show page links with parameters -->
    <xsl:template match="pagesAvailableold">
        <xsl:variable name="pagesAvailable"><xsl:value-of select="."/></xsl:variable>
        <xsl:variable name="currenctIndex"><xsl:value-of select="../currentIndex"/></xsl:variable>
        <xsl:variable name="prevIndex"><xsl:value-of select="../prevIndex"/></xsl:variable>
        <xsl:apply-templates select="pagesAvailable"/>
        <a href="defaultPagertest.xml">Previous</a>&#160; <a
            href="http://160.45.63.151:8080/cichorieae/portal/taxon/find.xml?">Next</a>
    </xsl:template>

    <xsl:template match="indices">
        <!--a href="{$Server}?ID={ID}&amp;APP={etc}">click here</a-->
        <!--nextIndex type="number">1</nextIndex-->
        <!--xsl:variable name="query_string"><xsl:value-of select="$url_webservice"/>?query=Lapsana&amp;pageSize=25&amp;doTaxa=1&amp;doSynonyms=1&amp;doMisappliedNames=1&amp;doTaxaByCommonNames=0</xsl:variable-->
        <xsl:variable name="query_string"><xsl:value-of select="$url_webservice"
            />?query=Lapsana&amp;matchMode=ANYWHERE</xsl:variable>
        <!--pass in query strong instead of lapsana 
        only show next and previous if the pages exist-->
        <a href="{$query_string}&amp;pageNumber=0">First</a> &#160; <xsl:for-each select="e">
            <xsl:variable name="page_number"><xsl:value-of select="."/></xsl:variable>
            <a href="{$query_string}&amp;pageNumber={$page_number}"><xsl:value-of select="."
            /></a>&#160; </xsl:for-each> &#160; <a href="{$query_string}&amp;pageNumber=1">Last</a>
    </xsl:template>



    <xsl:template name="pagesAvailable">
        <xsl:param name="index" select="1"/>
        <xsl:if test="$index > 0">
            <xsl:value-of select="../currentIndex"/>
            <xsl:value-of select="$index"/>
            <xsl:text>.</xsl:text>
            <xsl:call-template name="pagesAvailable">
                <xsl:with-param name="index" select="$index - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>


    <!--xsl:template match=".">
        <xsl:value-of select="text()"/>
    </xsl:template-->

</xsl:stylesheet>
