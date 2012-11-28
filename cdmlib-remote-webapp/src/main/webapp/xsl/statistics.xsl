<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:http="http://expath.org/ns/http-client" version="2.0">
   
   
    <xsl:template match="/Statistics">
        
        <HTML>
            <HEAD>
                <script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"/>
                <link type="text/css" rel="stylesheet" media="all" href="/vibrant_index/search/vibrant.css"/>
            </HEAD>
            <BODY>                
                <script type="text/javascript" language="javascript"></script>
                <a href="/vibrant_index/search/vibrant_names.html" title="Home">
                    <img src="/vibrant_index/search/acquia_prosper_logo.png" alt="Home"/>
                </a>
                
                <TABLE border="0">
                    <tr>
                        <td>
                    <xsl:for-each select="countMap/*">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                        </td>
                    </tr>
                </TABLE>
            </BODY>
        </HTML>
       
    </xsl:template>
    
    <xsl:template match=".">
        <xsl:value-of select="."></xsl:value-of>        
    </xsl:template>
    

    
</xsl:stylesheet>