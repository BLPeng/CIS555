<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/documentcollection/document/rss">
        <html><body>
	    <xsl:for-each select="channel">
		<h2><xsl:value-of select="title"/></h2>
		<table border="1">
		<tr><th>Title</th><th>Description</th></tr>
		<xsl:for-each select="item">
			<tr>
			<td><xsl:value-of select="title"/></td>
			<td><xsl:value-of select="description"/></td>
			</tr>
		</xsl:for-each>
		</table>
	    </xsl:for-each>
	</body></html>
    </xsl:template>
</xsl:stylesheet>
