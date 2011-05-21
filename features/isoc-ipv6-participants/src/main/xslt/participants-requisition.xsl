<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://xmlns.opennms.org/xsd/config/model-import" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:java="http://xml.apache.org/xalan/java" version="1.0"> 

  <xsl:output method="xml"/> 

  <xsl:template match="/">
	<model-import foreign-source="ISOC">
	  <xsl:apply-templates/>
	</model-import>
  </xsl:template>

  <!--
  <xsl:template match="/participants">
	<xsl:if test="participant"> 
        <xsl:apply-templates/>
	</xsl:if>
  </xsl:template> 
  -->

  <xsl:template match="/participants/participant">
    <!-- 
      I tried all permutations of creating a primitive boolean with Xalan... 
      this seems to be the only one that works.
    -->
    <xsl:variable name="javaTrue" select="java:java.lang.Boolean.parseBoolean('true')"/>
    <xsl:variable name="javaFalse" select="java:java.lang.Boolean.parseBoolean('false')"/>
	<node building="Participants">
	  <xsl:attribute name="node-label"><xsl:value-of select="hostname"/></xsl:attribute>
	  <xsl:attribute name="foreign-id"><xsl:value-of select="hostname"/></xsl:attribute>
	  <xsl:if test="v4_only != ''">
		<xsl:variable name="dnsName" select="v4_only"/>
		<xsl:variable name="address" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaFalse, $javaFalse)"/>
		<xsl:if test="string($address)">
		  <interface status="1" snmp-primary="N">
			<xsl:attribute name="ip-addr">
			  <xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($address)"/>
			</xsl:attribute>
			<xsl:attribute name="descr">
			  <xsl:value-of select="$dnsName"/>
			</xsl:attribute>
			<monitored-service service-name="HTTP"/>
			<!-- <monitored-service service-name="HTTP-V4"/> -->
		  </interface>
		</xsl:if>
		<xsl:if test="dual_hostname != ''">
		  <xsl:variable name="dnsName" select="dual_hostname"/>
		  <xsl:variable name="dualAddress" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaFalse, $javaFalse)"/>
		  <xsl:if test="string($dualAddress) and (not(string($address)) or (java:getHostAddress($dualAddress) != java:getHostAddress($address)))">
			<interface status="1" snmp-primary="N">
			  <xsl:attribute name="ip-addr">
				<xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($dualAddress)"/>
			  </xsl:attribute>
			  <xsl:attribute name="descr">
				<xsl:value-of select="$dnsName"/>
			  </xsl:attribute>
			  <monitored-service service-name="HTTP"/>
			  <!-- <monitored-service service-name="HTTP-V4"/> -->
			</interface>
		  </xsl:if>
		</xsl:if>
	  </xsl:if>
	  <xsl:if test="v6_only != ''">
		<xsl:variable name="dnsName" select="v6_only"/>
		<xsl:variable name="address" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaTrue, $javaFalse)"/>
		<xsl:if test="string($address)">
		  <interface status="1" snmp-primary="N">
			<xsl:attribute name="ip-addr">
			  <xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($address)"/>
			</xsl:attribute>
			<xsl:attribute name="descr">
			  <xsl:value-of select="$dnsName"/>
			</xsl:attribute>
			<monitored-service service-name="HTTP"/>
			<!-- <monitored-service service-name="HTTP-V6"/> -->
		  </interface>
		</xsl:if>
		<xsl:if test="dual_hostname != ''">
		  <xsl:variable name="dnsName" select="dual_hostname"/>
		  <xsl:variable name="dualAddress" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaTrue, $javaFalse)"/>
		  <xsl:if test="string($dualAddress) and (not(string($address)) or (java:getHostAddress($dualAddress) != java:getHostAddress($address)))">
			<interface status="1" snmp-primary="N">
			  <xsl:attribute name="ip-addr">
				<xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($dualAddress)"/>
			  </xsl:attribute>
			  <xsl:attribute name="descr">
				<xsl:value-of select="$dnsName"/>
			  </xsl:attribute>
			  <monitored-service service-name="HTTP"/>
			  <!-- <monitored-service service-name="HTTP-V6"/> -->
			</interface>
		  </xsl:if>
		</xsl:if>
	  </xsl:if>
	  <!-- If there are no v4_only or v6_only address, then emit interfaces for the dual stack hostnames -->
	  <xsl:if test="dual_hostname != '' and v4_only = '' and v6_only = ''">
		<xsl:variable name="dnsName" select="dual_hostname"/>
		<xsl:variable name="address" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaFalse, $javaFalse)"/>
		<xsl:if test="string($address)">
		  <interface status="1" snmp-primary="N">
			<xsl:attribute name="ip-addr">
			  <xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($address)"/>
			</xsl:attribute>
			<xsl:attribute name="descr">
			  <xsl:value-of select="$dnsName"/>
			</xsl:attribute>
			<monitored-service service-name="HTTP"/>
			<!-- <monitored-service service-name="HTTP-V4"/> -->
		  </interface>
		</xsl:if>
		<xsl:variable name="address" select="java:org.opennms.core.utils.InetAddressUtils.getInetAddress($dnsName, $javaTrue, $javaFalse)"/>
		<xsl:if test="string($address)">
		  <interface status="1" snmp-primary="N">
			<xsl:attribute name="ip-addr">
			  <xsl:value-of select="java:org.opennms.core.utils.InetAddressUtils.str($address)"/>
			</xsl:attribute>
			<xsl:attribute name="descr">
			  <xsl:value-of select="$dnsName"/>
			</xsl:attribute>
			<monitored-service service-name="HTTP"/>
			<!-- <monitored-service service-name="HTTP-V6"/> -->
		  </interface>
		</xsl:if>
	  </xsl:if>
	</node>
	<!--
	<xsl:value-of select="join_date"/>
	<xsl:value-of select="name"/>
	<xsl:value-of select="hostname"/>
	<xsl:value-of select="v6_only"/>
	<xsl:value-of select="v4_only"/>
	<xsl:value-of select="dual_hostname"/>
	<xsl:value-of select="v4_fetchable-url/@small"/>
	<xsl:value-of select="v4_fetchable-url/@notsmall"/>
	<xsl:value-of select="dual_fetchable-url/@small"/>
	<xsl:value-of select="dual_fetchable-url/@notsmall"/>
	<xsl:value-of select="alerts_to"/>
	</tr>
	-->
  </xsl:template>

  <!-- Black hole any participants that do not have v4, v6, or dual-stack hostnames defined -->
  <xsl:template match="/participants/participant[v4_only = '' and v6_only = '' and dual_hostname = '']"/>

  <!-- Identity transform -->
  <xsl:template match="node()|@*">
	<xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>
