/********************************************************************************
 * Copyright (c) 2015, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/

package org.eclipse.mdm.testutils;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract class for providing an ASAM ODS Server as a TestContainer for use
 * with the tests.
 * 
 * <p>
 * To use it, you have to provide an implementation of this class, that uses a
 * docker image of an actual ASAM ODS server implementation and provides the
 * connection parameters to connect to this image through
 * {@link OdsServerContainer.getConnectionParameters}. As the
 * ODSHttpContextFactory needs at minimum the parameters "url", "username" and
 * "password", the values for this parameters have to be provided by the
 * implementation. Since the implementation is looked up by the Java Service
 * Provider, you have to provide your implementation by creating a file
 * <code>META-INF/services/org.eclipse.mdm.testutils.OdsServerContainer</code>
 * on your classpath containing the fully qualified name of your implementation
 * class as content.
 * </p>
 * <p>
 * If no Java Service Provider implementation is found, the Tests using the
 * {@link OdsServerContainer} are skipped during test execution.
 * </p>
 * <p>
 * The ODS Server container has to use an openMDM application model.
 * </p>
 * <p>
 * In a Test you can use the OdsServerContainer to open an ApplicationContext on
 * the ASAM ODS Server:
 * </p>
 * 
 * <pre>
 * &#64;Testcontainers(disabledWithoutDocker = true)
 * public class MyTest {
 *   &#64;Container
 *   public static OdsServerContainer odsServer = OdsServerContainer.create();
 * 
 *   &#64;BeforeAll
 *   public static void setUpBeforeClass() throws ConnectionException {
 * 	   ApplicationContext context = new ODSHttpContextFactory().connect("MDM", odsServer.getConnectionParameters());
 *     ...
 *   }
 * }
 * 
 * </pre>
 */
public abstract class OdsServerContainer extends GenericContainer<OdsServerContainer> {

	private static Logger LOG = LoggerFactory.getLogger(OdsServerContainer.class);

	public OdsServerContainer(RemoteDockerImage image) {
		super(image);
	}

	public OdsServerContainer(DockerImageName dockerImageName) {
		super(dockerImageName);
	}

	public OdsServerContainer(String dockerImageName) {
		super(dockerImageName);
	}

	public abstract Map<String, String> getConnectionParameters();

	public static OdsServerContainer create() {
		ServiceLoader<OdsServerContainer> loader = ServiceLoader.load(OdsServerContainer.class);

		Iterator<OdsServerContainer> it = loader.iterator();

		while (it.hasNext()) {
			OdsServerContainer container = it.next();
			return container;
		}

		LOG.warn("No OdsServerContainer implementation found!");
		return new OdsServerContainer("busybox") {

			@Override
			public Map<String, String> getConnectionParameters() {
				throw new TestAbortedException(
						"Could not find any service implementation for " + OdsServerContainer.class.getName());
			}
		};
	}
}
