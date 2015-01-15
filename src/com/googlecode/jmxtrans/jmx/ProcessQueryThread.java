package com.googlecode.jmxtrans.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;

import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Server;

/**
 * Executes either a getAttribute or getAttributes query.
 */
public class ProcessQueryThread implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(ProcessQueryThread.class);

	private final MBeanServerConnection mbeanServer;
	private final Server server;
	private final Query query;

	public ProcessQueryThread(MBeanServerConnection mbeanServer, Server server, Query query) {
		this.mbeanServer = mbeanServer;
		this.server = server;
		this.query = query;
	}

	public void run() {
		try {
			new JmxQueryProcessor().processQuery(this.mbeanServer, this.server, this.query);
		} catch (Exception e) {
			log.error("Error executing query: " + query, e);
			throw new RuntimeException(e);
		}
	}
}
