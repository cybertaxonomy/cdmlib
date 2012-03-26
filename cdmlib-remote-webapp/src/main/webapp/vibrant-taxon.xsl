<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- pass this in as a parameter? -->
    <xsl:variable name="url_portal"> http://160.45.63.151:8080/cichorieae/ </xsl:variable>
    <xsl:variable name="url_webservice">http://localhost:8080/taxon/findTaxaAndNames</xsl:variable>
    
    <!--xsl:call-template name="doMoreStuff">
            <xsl:with-param name="param1" select="document($url)/foo"/>
        </xsl:call-template-->


    <xsl:template match="/DefaultPagerImpl">
        <HTML>
            <BODY>
                <H2>Taxons:</H2>
                <xsl:element name="form">
                    <xsl:attribute name="action"
                        >http://160.45.63.151:8080/cichorieae/name</xsl:attribute>
                    <xsl:attribute name="method">GET</xsl:attribute>
                    <TABLE border="0">
                        <TR>
                            <TD>Title cache</TD>
                            <TD>UUID</TD>
                        </TR>
                        <xsl:for-each select="records/e">

                            <!--xsl:variable name="url">
                                http://160.45.63.151:8080/cichorieae/name/<xsl:value-of select="uuid"/>
                            </xsl:variable-->
                            <xsl:variable name="url">
                                <xsl:value-of select="$url_portal"/>name/<xsl:value-of select="uuid"
                                />
                            </xsl:variable>

                            <TR>
                                <TD>
                                    <xsl:apply-templates select="titleCache"/>
                                </TD>
                                <TD>
                                    <xsl:apply-templates select="uuid"/>
                                </TD>
                                <!-- TD><xsl:value-of select="$url"></xsl:value-of></TD -->
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

                </xsl:element>
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
        <xsl:variable name="query_string"><xsl:value-of select="$url_webservice"/>?query=Lapsana&amp;pageSize=25&amp;doTaxa=1&amp;doSynonyms=1&amp;doMisappliedNames=1&amp;doTaxaByCommonNames=0</xsl:variable>
        
        <!--pass in query strong instead of lapsana 
        only show next and previous if the pages exist-->
        
        <a href="{$query_string}&amp;pageNumber=0">First</a>
        &#160; 
        <xsl:for-each select="e">
            <xsl:variable name="page_number"><xsl:value-of select="."/></xsl:variable>
            
            <a href="{$query_string}&amp;pageNumber={$page_number}"><xsl:value-of select="."/></a>&#160; 
        </xsl:for-each>
        &#160; 
        <a href="{$query_string}&amp;pageNumber=1">Last</a>
        
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
    
    <xsl:template name="dots">      
        <xsl:param name="count" select="1"/>    
        <xsl:if test="$count > 0">
            <xsl:text>.</xsl:text>
            <xsl:call-template name="dots">
                <xsl:with-param name="count" select="$count - 1"/>
            </xsl:call-template>
        </xsl:if>       
    </xsl:template>

    <!--xsl:template match=".">
        <xsl:value-of select="text()"/>
    </xsl:template-->

</xsl:stylesheet>
