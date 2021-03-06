package org.sagebionetworks.table.cluster;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.StackConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Table's Database information now comes from the stack configuration.
 * 
 * @author jmhill
 *
 */
@Service
public class InstanceDiscoveryImpl implements  InstanceDiscovery {
	
	private static final Logger log = LogManager.getLogger(InstanceDiscoveryImpl.class);
	
	private StackConfiguration config;
	
	@Autowired
	public InstanceDiscoveryImpl(StackConfiguration config) {
		this.config = config;
	}
	
	/**
	 * This method is designed to be called repeatedly over time to discover new instances and lost instances.
	 */
	@Override
	public List<InstanceInfo> discoverAllInstances() {
		List<InstanceInfo> list = new LinkedList<InstanceInfo>();
		for(int i=0; i<config.getTablesDatabaseCount(); i++){
			String endpoint = config.getTablesDatabaseEndpointForIndex(i);
			String schema = config.getTablesDatabaseSchemaForIndex(i);
			boolean useSSL = config.useSSLConnectionForTablesDatabase();
			InstanceInfo info = new InstanceInfo(endpoint, schema, useSSL);
			list.add(info);
			log.debug("Found a database: "+info.toString());
		}
		return list;
	}
}
