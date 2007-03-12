#!/usr/bin/env groovy 

import groovy.xml.DOMBuilder;
import groovy.xml.dom.DOMCategory;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import javax.xml.transform.OutputKeys;

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;

import org.xml.sax.InputSource;



def createConfigFile(configFile) {
    return createConfigFile(configFile) { true }
}
def createConfigFile(configFile, matcher) {
    def e = new Expando(
                        configFile:configFile, 
                        load:{ configFile.load() }, 
                        process:{ svcs -> svcs.findAll(matcher).each { configFile.process(it) } },
                        save:{ configFile.save() },
                        );
    e['override'] = { map -> map.each { key, val -> e[key] = val; }; return e }
    return e
    
}

def pdb = ProvisioningDatabase.load(new File("config-ext.txt"), new File("config-int.txt"));


def capsdConfig = new CapsdConfiguration(new File("capsd-configuration.xml"));
def pollerConfig = new PollerConfiguration(new File("poller-configuration.xml"));
def respGraphProps = new ResponseGraphProperties(new File("response-graph.properties"));
def httpCollectionConfig = new HttpCollectionConfig(new File("http-datacollection-config.xml")); 
def collectdConfig = new CollectdConfiguration(new File("collectd-configuration.xml"), "http");
def snmpGraphProps = new SnmpGraphProperties(new File("snmp-graph.properties"));
def threshdConfig = new ThreshdConfiguration(new File("threshd-configuration.xml"), "http");
def thresholds = new Thresholds(new File("thresholds.xml"));

def internalOnly = { svc -> svc.internal }

def configFiles = [
    createConfigFile(capsdConfig), 
    createConfigFile(pollerConfig), 
    createConfigFile(respGraphProps),
    createConfigFile(httpCollectionConfig, internalOnly),
    createConfigFile(collectdConfig, internalOnly),
    createConfigFile(snmpGraphProps, internalOnly),
    createConfigFile(threshdConfig, internalOnly),
    createConfigFile(thresholds, internalOnly)
];

configFiles.each { it.load() }

pdb.services.each { svc -> configFiles.each { configFile -> configFile.process([svc]) } }


configFiles.each { it.save() }


class ThreshdConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    def packageName
    
    ThreshdConfiguration(File file, String packageName) {
        super(file);
        this.packageName = packageName;
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'threshd-configuration'()
    }
    
    private boolean packageExists() {
       	boolean result;
       	use(DOMCategory) {
            result = (null != document.documentElement.'package'.find { it['@name'] == packageName });    
       	}
       	return result;
    }
    
    private void createPackage() {
        def xml = new DomBuilder(document, document.documentElement)
        xml.'package'(name:packageName) {
            filter("IPADDR IPLIKE *.*.*.*")	 
            specific("0.0.0.0")
            'include-range'(begin:"1.1.1.1", end:"254.254.254.254")
            'include-url'("file:/space/svn/trunk/target/opennms-1.3.3-SNAPSHOT/etc/include")
            'outage-calendar'("zzz from poll-outages.xml zzz")
        }
    }

    private boolean thresholderExists(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.thresholder?.find { it['@service'] == svc.serviceName })
        }
        return result;
    }

    public void createThresholder(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.thresholder(service:svc.serviceName, 'class-name':'org.opennms.netmgt.threshd.SnmpThresholder')
    }
    
    public void process(ProvisionedService svc) {
        if (!svc.threshold.enabled) {
            return;
        }
        if (!packageExists()) {
            println "${svc}: Creating package ${packageName} in ${file.name}."
            createPackage();
        }
        if (alreadyConfigured(svc)) {
            println "${svc}: Threshold service ${svc.serviceName} already exists in ${file.name}. skipping."
        } else {
            println "${svc}: Creating threshold service ${svc.serviceName} in ${file.name}."
            createNewConfiguration(svc);
        }
        if (!thresholderExists(svc)) {
            println "${svc}: Creating thresholder for ${svc.serviceName} in ${file.name}."
            createThresholder(svc);
        }
    }
    
    private findPackage() {
        def result;
        use(DOMCategory) {
            result = document.documentElement.'package'.find{ it['@name'] == packageName }
        }
        return result;
    }

    
    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'package'.find { it['@name'] == packageName }?.service?.find { it['@name'] == svc.serviceName });
        }
        return result;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, findPackage());
        
        xml.service(name:svc.serviceName, interval:"300000", 'user-defined':false, status:"on") {
            parameter(key:"thresholding-group", value:svc.serviceName)
        }
        
    }
}


class Thresholds extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    Thresholds(File file) {
        super(file);
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'thresholding-config'()
    }
    
    public void process(ProvisionedService svc) {
        if (!svc.threshold.enabled) {
            return;
        }
        if (alreadyConfigured(svc)) {
            println "${svc}: Threshold group already exists for service ${svc.serviceName} in ${file.name}. skipping."
        } else {
            println "${svc}: Creating threshold grouop for service ${svc.serviceName} in ${file.name}."
            createNewConfiguration(svc);
        }
    }
    
    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.group.find { it['@name'] == svc.serviceName });
        }
        return result;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.comment(comment(svc));
        xml.group(name:svc.serviceName, rrdRepository:"/opt/opennms/share/rrd/snmp/") {
            svc.threshold.configs.each { t ->
                    threshold(type:t.type, 
                              'ds-name':svc.serviceName, 
                              'ds-type':'node', 
                              value:t.value, 
                              rearm:t.rearm, 
                              trigger:t.trigger)
            }
        }
        

    }
}

class SnmpGraphProperties extends PropertiesConfigurationFile {
    
    SnmpGraphProperties(File file) {
        super(file);
    }
    
    public void process (ProvisionedService svc) {
        if (!alreadyConfigured(svc)) {
            println "${svc}: Creating report ${svc.reportName} in ${file.name}"
            addReport(svc);
        } else {
            println "${svc}: Report ${svc.reportName} already exists. skipping."
        }
    }
    
    public boolean alreadyConfigured(ProvisionedService svc) {
        return getReportList().contains('fast.'+svc.reportName);
    }
    
    private List getReportList() {
        def reports = get("reports");
        if (reports == null) {
            return [];
        }
        return reports.split(/\s*,\s*/).toList();
    }
    
    private putReportList(reports) {
        put("reports", reports.join(', '));
    }
    
    private addToReportList(reportName) {
        def reports = getReportList();
        reports.add(reportName);
        putReportList(reports);
    }
    
    public boolean addReport(ProvisionedService svc) {
        def reportName = 'fast.'+svc.reportName;
        addToReportList(reportName);
        
        put("report.${reportName}.name", "${svc.serviceName} Document Count")
     	put("report.${reportName}.columns", "${svc.serviceName}")
     	put("report.${reportName}.type", "nodeSnmp")
     	def prop = put("report.${reportName}.command")
     	prop << "--title=\"${svc.serviceName} Document Count\" " 
        prop << " DEF:cnt={rrd1}:${svc.serviceName}:AVERAGE "
        prop << " LINE2:cnt#0000ff:\"Document Count\" "
        prop << " GPRINT:cnt:AVERAGE:\" Avg  \\\\: %8.2lf %s\" " 
        prop << " GPRINT:cnt:MIN:\"Min  \\\\: %8.2lf %s\" "
        prop << " GPRINT:cnt:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" "
     	
     	addBlankLine();
        
    }
}



class CollectdConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    def packageName
    CollectdConfiguration(File file, String packageName) {
        super(file);
        this.packageName = packageName
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'collectd-configuration'(threads:50)
    }
    
    private boolean packageExists() {
       	boolean result;
       	use(DOMCategory) {
            result = (null != document.documentElement.'package'.find { it['@name'] == packageName });    
       	}
       	return result;
    }
    
    private void createPackage() {
        def xml = new DomBuilder(document, document.documentElement)
        xml.'package'(name:packageName) {
            filter("IPADDR IPLIKE *.*.*.*")	 
            specific("0.0.0.0")
            'include-range'(begin:"1.1.1.1", end:"254.254.254.254")
            'include-url'("file:/space/svn/trunk/target/opennms-1.3.3-SNAPSHOT/etc/include")
            'outage-calendar'("zzz from poll-outages.xml zzz")
        }
    }
    
    public boolean createNewCollector(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.collector(service:svc.serviceName, 'class-name':'org.opennms.netmgt.collectd.HttpCollector');
    }
    
    public boolean collectorConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.collector.find{ it['@service'] == svc.serviceName });
        }
        return result;
    }
    public void process(ProvisionedService svc) {
        if (!collectorConfigured(svc)) {
            println "${svc}: Adding Collector for service ${svc.serviceName} to ${file.name}."
            createNewCollector(svc);
        } else {
	    println "${svc}: Monitor for service ${svc.serviceName} already exists in ${file.name}. skipping."            
        }
        if (!packageExists()) {
            println "${svc}: Creating package ${packageName} in ${file.name}."
            createPackage();
        }
	if (alreadyConfigured(svc)) {
	    println "${svc}: Collection service ${svc.serviceName} already exists ${file.name}. skipping."
	} else {
	    println "${svc}: Creating collection service ${svc.serviceName} in ${file.name}."
	    createNewConfiguration(svc);
	}
    }
    
    private findPackage() {
        def result;
        use(DOMCategory) {
            result = document.documentElement.'package'.find{ it['@name'] == packageName }
        }
        return result;
    }
    
    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'package'.find { it['@name'] == packageName }?.service?.find { it['@name'] == svc.serviceName });
        }
        return result;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, findPackage());
        
        xml.service(name:svc.serviceName, interval:"300000", 'user-defined':false, status:"on") {
            parameter(key:"http-collection", value:svc.serviceName)
        }
        
    }
}


class HttpCollectionConfig extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    HttpCollectionConfig(File file) {
        super(file);
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'http-datacollection-config'()
    }
    
    public void process(ProvisionedService svc) {
        if (alreadyConfigured(svc)) {
            println "${svc}: Collection already exists for service ${svc.name} in http-datacollection-config.xml. skipping."
        } else {
            println "${svc}: Creating collection for service ${svc.name} in http-datacollection-config.xml."
            createNewConfiguration(svc);
        }
    }
    
    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'http-collection'.find { it['@name'] == svc.serviceName });
        }
        return result;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.comment(comment(svc));
        xml.'http-collection'(name:svc.serviceName) {
            rrd(step:300) {
                rra("RRA:AVERAGE:0.5:1:8928")
                rra("RRA:AVERAGE:0.5:12:8784")
                rra("RRA:MIN:0.5:12:8784")
                rra("RRA:MAX:0.5:12:8784")
            }
            uris {
                uri {
                    url(scheme:"http", 'http-version':"1.1", host:svc.url.address, port:svc.url.port, 
                        path:svc.url.path, query:svc.url.query,  matches:/(?s).*#CNT\s+([0-9]+).*/, 'response-range':"100-399")
            	    attributes {
                        attrib (alias:svc.serviceName, 'match-group':"1", type:"gauge32")
            	    }
                }
            }
        }
    }
    
}



class ResponseGraphProperties extends PropertiesConfigurationFile {
    
    ResponseGraphProperties(File file) {
        super(file);
    }
    
    public void process (ProvisionedService svc) {
        if (!alreadyConfigured(svc)) {
            println "${svc}: Creating report ${svc.reportName} in ${file.name}"
            addReport(svc);
        } else {
            println "${svc}: Report ${svc.reportName} already exists. skipping."
        }
    }
    
    public boolean alreadyConfigured(ProvisionedService svc) {
        return getReportList().contains(svc.reportName);
    }
    
    private List getReportList() {
        def reports = get("reports");
        if (reports == null) {
            return [];
        }
        return reports.split(/\s*,\s*/).toList();
    }
    
    private putReportList(reports) {
        put("reports", reports.join(', '));
    }
    
    private addToReportList(reportName) {
        def reports = getReportList();
        reports.add(reportName);
        putReportList(reports);
    }
    
    public boolean addReport(ProvisionedService svc) {
        
        addToReportList(svc.reportName);
        
        put("report.${svc.reportName}.name", "${svc.serviceName} Latency")
     	put("report.${svc.reportName}.columns", "${svc.serviceName}")
     	put("report.${svc.reportName}.type", "responseTime, distributedStatus")
     	def prop = put("report.${svc.reportName}.command")
     	prop << "--title=\"${svc.serviceName} Response Time\" " 
     	prop << " --vertical-label=\"Seconds\" "
     	prop << " DEF:rtMills={rrd1}:${svc.serviceName}:AVERAGE "
     	prop << " CDEF:rt=rtMills,1000,/ "
        prop << " LINE1:rt#0000ff:\"Response Time\" "
     	prop << " GPRINT:rt:AVERAGE:\" Avg  \\\\: %8.2lf %s\" "
     	prop << " GPRINT:rt:MIN:\"Min  \\\\: %8.2lf %s\" "
     	prop << " GPRINT:rt:MAX:\"Max  \\\\: %8.2lf %s\\\\n\""
     	
     	addBlankLine();
        
     	
    }
    
    
    
    
}

class CapsdConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    CapsdConfiguration(File file) {
        super(file);
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'capsd-configuration'();
    }
    
    public void process(ProvisionedService svc) {
        if (alreadyConfigured(svc)) {
            println "${svc}: Capsd plugin ${svc.serviceName} already configured for address ${svc.url.address}. skipping."
            return;
        }
        def plugin = getExistingConfiguration(svc);
        if (plugin == null) {
            println "${svc}: Creating Capsd plugin ${svc.serviceName} for address ${svc.url.address}."
            createNewConfiguration(svc);
        } else {
            println "${svc}: Capsd plugin ${svc.serviceName} already exists. Adding address ${svc.url.address}"
            addToExistingConfiguration(plugin, svc);
        }
    }
    
    public alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'protocol-plugin'.find { it['@protocol'] == svc.serviceName }?.'protocol-configuration'?.specific?.find { it.text() == svc.url.address });
        }
        return result;
    }
    
    public Object getExistingConfiguration(ProvisionedService svc) {
        // handle a bug in groovy that doesnt properly return from 'use' cks
        def p;
        use(DOMCategory) {
            p = document.documentElement['protocol-plugin'].find { it['@protocol'] == svc.serviceName }
        }
        return p;
    }
    
    public void addToExistingConfiguration(Element plugin, ProvisionedService svc) {
        def protoConfig;
        use(DOMCategory) {
            protoConfig = plugin['protocol-configuration'][0];
        }
        addLeadingComment(plugin, comment(svc));
        def xml = new DomBuilder(document, protoConfig);
        xml.specific(svc.url.address);
    }
    
    
    
    public void createNewConfiguration(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.comment(comment(svc));
        xml.'protocol-plugin'(protocol:svc.serviceName, 'class-name':'org.opennms.netmgt.capsd.plugins.LoopPlugin', scan:'off', 'user-defined':false) {
            'protocol-configuration'(scan:'enable', 'user-defined':false) {
                specific(svc.url.address)
            }
            'property'(key:'timeout', value:3000)
            'property'(key:'retry', value:1)
        }
    }
    
}

class PollerConfiguration extends XMLConfigurationFile {
    
    def comment = { svc -> " ${svc.name} ${svc.url} ${svc.threshold} ${svc.email} " }
    
    PollerConfiguration(File file) {
        super(file);
    }
    
    public void intializeNewDocument(Document document) {
        new DomBuilder(document).'poller-configuration'() {
            ['internal','external'].each {
                'package'(name:it, remote:true) {
                    filter("IPADDR IPLIKE *.*.*.*")
                    'include-range'(begin:'1.1.1.1', end:'254.254.254.254')
                    rrd(step:300) {
                        rra('RRA:AVERAGE:0.5:1:2016')
                        rra('RRA:AVERAGE:0.5:12:4464')
                        rra('RRA:MIN:0.5:12:4464')
                        rra('RRA:MAX:0.5:12:4464')
                    }
                }
            }
        };
    }
    
    public void process(ProvisionedService svc) {
        if (!monitorConfigured(svc)) {
            println "${svc}: Adding Monitor for service ${svc.serviceName} to ${file.name}."
            createNewMonitor(svc);
        } else {
            println "${svc}: Monitor for service ${svc.serviceName} already exists in ${file.name}. skipping."            
        }
        if (!alreadyConfigured(svc)) {
            println "${svc}: Creating service ${svc.serviceName} in polling package '${svc.pollingPackageName}'."
            createNewConfiguration(svc);
        } else {
            println "${svc}: Service ${svc.serviceName} already configured for package '${svc.pollingPackageName}'. skipping."
        }
    }
    
    public boolean monitorConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.monitor.find{ it['@service'] == svc.serviceName });
        }
        return result;
    }
    
    public boolean createNewMonitor(ProvisionedService svc) {
        def xml = new DomBuilder(document, document.documentElement);
        xml.monitor(service:svc.serviceName, 'class-name':'org.opennms.netmgt.poller.monitors.PageSequenceMonitor');
    }
    
    public boolean alreadyConfigured(ProvisionedService svc) {
        boolean result;
        use(DOMCategory) {
            result = (null != document.documentElement.'package'.find{ it['@name'] == svc.pollingPackageName }?.service?.find{ it['@name'] == svc.serviceName })
        }
        return result;
    }
    
    public Node findPackageIfExists(ProvisionedService svc) {
        Node pkg;
        use (DOMCategory) {
            pkg = document.documentElement.'package'.find{ it['@name'] == svc.pollingPackageName };
        }
        return pkg;
    }
    
    public Node findPackage(ProvisionedService svc) {
        Node pkg = findPackageIfExists(svc);
        if (pkg == null) {
            def xml = new DomBuilder(document, document.documentElement);
            pkg = xml.'package'(name:svc.pollingPackageName, remote:true) {
                   filter("IPADDR IPLIKE *.*.*.*")
                    'include-range'(begin:'1.1.1.1', end:'254.254.254.254')
                    rrd(step:300) {
                        rra('RRA:AVERAGE:0.5:1:2016')
                        rra('RRA:AVERAGE:0.5:12:4464')
                        rra('RRA:MIN:0.5:12:4464')
                        rra('RRA:MAX:0.5:12:4464')
                    }
                    downtime(interval:30000, begin:0, end:300000)
                    downtime(interval:300000, begin:300000, end:43200000)
                    downtime(interval:600000, begin:43200000)
            }
        }
        return pkg;
    }
    
    public void createNewConfiguration(ProvisionedService svc) {
        def pkg = findPackage(svc);
        def agentInfo = svc.internal ? "Internal" : "External";
        def xml = new DomBuilder(document, pkg);
        xml.comment(comment(svc));
        xml.service(name:svc.serviceName, interval:300000, 'user-defined':true, status:'on') {
            parameter(key:'retry', value:1);
            parameter(key:'timeout', value:3000);
            parameter(key:'rrd-repository', value:'/opt/opennms/share/rrd/response');
            parameter(key:'ds-name', value:svc.serviceName);
            parameter(key:'page-sequence') {
                'page-sequence' {
                    page('user-agent':"FASTMonitor/v1.3.2 (${agentInfo})", host:svc.url.host, path:svc.url.file, port:svc.url.port)
                }
            }
        }
    }
    
}

class ProvisioningDatabase {
    def comment = ~/^\s*#.*$/;
    def blank = ~/^\s*$/;
    def data = ~/^\s*(\S*)\s+(\S*)\s+(\S*)\s+(\S*)\s*$/;
    
    def services = [];
    
    public static ProvisioningDatabase load(externalFile, internalFile) {
        ProvisioningDatabase pdb = new ProvisioningDatabase();
        pdb.parse(false, externalFile);
        pdb.parse(true, internalFile);
        return pdb;
    }
    
    public void parse(internal, r) {
        r.eachLine { line ->
                if (line =~ blank) {
                return;
            }
            if (line =~ comment) {
                return;
            }
            def matcher = line =~ data;
            if (matcher) {
                def name = matcher.group(1);
                def url = new ServiceURL(url:matcher.group(2));
                def threshold = new ThresholdSpecification(matcher.group(3));
                def email = matcher.group(4);
                def svc = new ProvisionedService(internal:internal, name:name, url:url, threshold:threshold, email:email);
                services.add(svc);
            }
        }
    }
    
    public void print() {
        for(svc in services) {
            println svc;
        }
    }
    
    
}

/*
  Threshold criteria:
	A value with a "-\.[9]+" is a plus or minus percent change with .05 being 5 percent so this 
        represents a negative decimal number call it x
        The corresponding thresholds.xml configuation where 0.95 = 1.0 + x (x is negative) and 1.05 = 1.0 - x
        <!-- Note: the "rearm" and "trigger" values are not currently used. -->
       <threshold type="relativeChange" ds-name="Vodafone"  ds-type="node" value="0.95" rearm="1.0" trigger="1"/>
       <threshold type="relativeChange" ds-name="Vodafone"  ds-type="node" value="1.05" rearm="1.0" trigger="1"/>

	A "-1" is thresholding disabled
        no configuration here

	A "0" is plus or minus 10 percent change
        <!-- Note: the "rearm" and "trigger" values are not currently used. -->
       <threshold type="relativeChange" ds-name="Vodafone"  ds-type="node" value="0.9" rearm="1.0" trigger="1"/>
       <threshold type="relativeChange" ds-name="Vodafone"  ds-type="node" value="1.1" rearm="1.0" trigger="1"/>
        
	A "+[9]+" is a hard threshold and the threshold is triggered when it falls below the specified value
        <threshold type="low" ds-name="${svc.serviceName}"  ds-type="node" value="X" rearm="X" trigger="3"/>
        
   
 */

class ThresholdSpecification {
    static PCT_SPEC = ~/-\.\d+/;
    def boolean legacy = false;
    def configs;

    ThresholdSpecification(String spec) {
        if (spec == '-1') {
            configs = [];
        } else if (spec == "0") {
            legacy = true;
            configs = [
                [type:'relativeChange', value:'0.9', rearm:'1.0', trigger:1],
                [type:'relativeChange', value:'1.1', rearm:'1.0', trigger:1]
            ];
        } else if (spec =~ PCT_SPEC) {
            def d = spec.toBigDecimal();
            configs = [
                [type:'relativeChange', value:1.0+d, rearm:'1.0', trigger:1],
                [type:'relativeChange', value:1.0-d, rearm:'1.0', trigger:1]
            ];
        } else {
            def v = spec.toBigInteger();
            configs = [
                [type:'low', value:v, rearm:v, trigger:3]
            ];
        }
    }

    public boolean isEnabled() {
        return configs.size() > 0
    }

    public String toString() {
        if (configs.size() == 0) {
            return "-1";
        } else if (configs.size() == 1) {
            return configs[0].value.toString();
        } else if (legacy) {
            return "0"
        } else {
            return (configs[0].value - 1.0).toString();
        }        
    }
}

class ServiceURL {
    String url;
    private addr = null;
    
    
    public String toString() {
        return url;
    }
    
    public int getPort() {
	URL u = new URL(url);
	return (u.port < 0 ? u.defaultPort : u.port);
    }
    
    public String getPath() {
        return new URL(url).getPath();
    }
    
    public String getHost() {
        return new URL(url).getHost();
    }
    
    public String getQuery() {
        return new URL(url).getQuery();
    }
    
    public String getFile() {
        return new URL(url).getFile();
    }
    
    public String getAddress() {
        if (addr != null) {
            return addr;
        }
        
        String host = getHost();
        try {
            addr = InetAddress.getByName(host).getHostAddress();
        } catch(Exception e) {
            System.err.println e
            addr = host;
        }
        
        return addr;
    }
}

class ProvisionedService {
    boolean internal;
    String name;
    ServiceURL url;
    ThresholdSpecification threshold;
    String email;
    
    public String getServiceName() {
        return name.replace('-','_');
    }
    
    public String getReportName() {
        return getServiceName().toLowerCase();
    }
    
    public String getPollingPackageName() {
        return internal ? "internal" : "external";
    }
    
    public String toString() {
        def descr = internal ? "Internal" : "External"        
        return "${name} (${descr})"
    }
}



class Property {
    def key;
    def val = [];
    
    Property(key) {
        this.key = key
    }
    
    Property(key, v) {
        this(key);
        this << v;
    }
    
    Property leftShift(v) {
        val.add(v.toString());
        return this;
    }
    
    void save(w) {
        boolean first = true;
        
        val.each { item -> if (first) { w.print("${key}=${item}"); first = false; } else { w.println('\\'); w.print(item) } }
        w.println();
    }
    
    String toString() {
        return val.join();
    }
    
}

class PropertiesConfigurationFile {
    def comment = ~/^\s*#.*$/;
    def blank = ~/^\s*$/;
    def data = ~/^\s*([^=\s]+)\s*=\s*(.+)$/;
    
    def file;
    def index = new LinkedHashMap();
    
    public PropertiesConfigurationFile(File file) {
        this.file = file;
    }
    
    public void save() {
        save(file);
    }
    
    public void save(File f) {
        f.withPrintWriter { writer -> index.each { key, value -> value.save(writer) } }	
    }
    
    public void load() {
        if (!file.exists()) {
            return;
        }
        int lineno = 0;
        def continuation = null;
        file.eachLine { line ->
                lineno++;
            if (continuation != null) {
                if (line.endsWith('\\')) {
                    continuation << line.substring(0, line.length()-1);
                } else {
                    continuation << line;
                    continuation = null;
                }
            }
        else if (line =~ blank || line =~ comment) {
                index["_line-"+lineno] =  createLineHolder(line);
            }
        else {
                def match = line =~ data;
                if (match) {
                    def key = match.group(1);
                    def val = match.group(2);
                    Property prop = new Property(key);
                    if (val.endsWith('\\')) {
                        prop << val.substring(0, val.length()-1);
                        continuation = prop;
                    } else {
                        prop << val;
                    }
                    index[key] = prop;
                }
            }
            
        }
    }
    
    private createLineHolder(String line) {
        new Expando(line:line, save:{ w -> w.println(line) }, toString:{ return line });
    }
    
    public void addComment(String cmt) {
        index["_line-"+index.size()] = createLineHolder("# ${cmt}"); 
    }
    
    public void addBlankLine() {
        index["_line-"+index.size()] = createLineHolder("");
    }
    
    public String get(String key) {
        return getAt(key);
    }
    
    public String getAt(String key) {
        return index[key]?.toString();
    }
    
    public Property put(String key) {
        index[key] = new Property(key);
        return index[key];
    }
    
    public Property put(String key, String val) {
        index[key] = new Property(key, val)
        return index[key];
        
    }
    
    public void putAt(String key, String val) {
        put(key, val);
    }
    
}

/**
 * This class reprsents a base class for XML configuration files that need to 
 * be loaded edited and saved.  
 */
abstract class XMLConfigurationFile {
    DocumentBuilder bldr;
    File file;
    Document document;
    
    XMLConfigurationFile(File file) {
        bldr = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        this.file = file;
    }
    
    public void load() {
        if (file.exists()) {
            document = bldr.parse(file);
        } else {
            document = bldr.newDocument();
            intializeNewDocument(document);
        }
    }
    
    public abstract void intializeNewDocument(Document document);
    
    public void addLeadingComment(Node node, String comment) {
        DomBuilder.addLeadingComment(node, comment);
    }
    
    public void save() {
        save(file)
    }
    
    public void save(File f) {
        def transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(f));
    }        
}


/**
 * This class is a DomBuilder similar to DOMBuilder provided with groovy but handles comments and
 * properly indents created DOM elements
 */
class DomBuilder extends BuilderSupport {
    private static final String START_TAG_INDENT = "start-tag-indent";
    private static final String END_TAG_INDENT = "end-tag-indent";
    
    Document document;
    
    public DomBuilder(Document doc) {
        this(doc, doc);
    }
    
    public DomBuilder(Document doc, Node node) {
        document = doc;
        setCurrent(node);
    }
    
    public static String addLeadingComment(Node node, String comment) {
        Document d = node.ownerDocument;
        Node parent = node.parentNode;
        Node insertionPoint = node;
        Node prev = node.previousSibling;
        if (DomBuilder.isIndent(prev)) {
            insertionPoint = prev;
        }
        parent.insertBefore(createIndent(d, nodeDepth(d, node)), insertionPoint);
        parent.insertBefore(d.createComment(comment), insertionPoint);
    }
    
    public static String indentString(int depth) {
        StringBuilder buf = new StringBuilder(depth*4 + 1);
        (depth*4).times { buf.append(' '); }
        return buf.toString();
    }
    
    protected void setParent(Object parent, Object child) {
        Node lastChild = parent.lastChild;
        if (DomBuilder.isIndent(lastChild)) {
            parent.insertBefore(child, lastChild);
        } else {
            parent.appendChild(child);
        }
    }
    
    private static boolean isIndent(Node lastChild) {
        if (lastChild == null) {
            return false;
        }
        
        if (lastChild.nodeType != Node.TEXT_NODE) {
            return false;
        }
        
        if (lastChild.textContent =~ /^\s*$/) {
            return true;
        }
        
        return false;
    }
    protected Object createNode(Object name) {
        if ("comment".equals(name)) {
            return document.createComment("");
        } else {
            return document.createElement(name);
        }
    }
    protected Object createNode(Object name, Object value) {
        if ("comment".equals(name)) {
            return document.createComment(value.toString());
        } else {
            def element = document.createElement(name);
            def text = document.createTextNode(value.toString());
            element.appendChild((Node)text);
            return element
        }
    }
    protected Object createNode(Object name, Map attributes) {
        def element = document.createElement(name);
        attributes.each { k,v -> element.setAttribute(k.toString(), v.toString()) }
        return element;
    }
    protected Object createNode(Object name, Map attributes, Object value) {
        def element = createNode(name, attributes);
        element.appendChild((Node)document.createTextNode(value.toString()));
        return element;
    }
    protected void nodeCompleted(Object parent, Object node) {
        int depth = nodeDepth(node);
        if (depth > 0) {
            parent.insertBefore(createIndentForStartTag(depth), node);
        }
        
        if (!isOneLiner(node)) {
            node.appendChild(document.createTextNode('\n'+indentString(depth)));
        }
    }
    
    public Node createIndentForStartTag(int depth) {
        createIndent(document, depth);
    }
    
    public static Node createIndent(Document d, int depth) {
        Node text = d.createTextNode('\n'+indentString(depth));
        return text;
    }
    
    
    public Node createIndentForEndTag(int depth) {
        createIndent(document, depth);
    }
    
    public int nodeDepth(Node node) {
        nodeDepth(document, node);
    }
    
    public static int nodeDepth(Document d, Node node) {
        Node n = node;
        int depth = 0;
        while(!n.isSameNode(d.documentElement)) {
            n = n.getParentNode();
            depth++;
        }
        return depth;
        
    }
    
    public boolean isOneLiner(Node node) {
        if (!node.hasChildNodes()) {
            return true;
        }
        
        NodeList children = node.getChildNodes();
        if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
            return true;
        }
        
        return false;
        
    }
}


