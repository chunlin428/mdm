/********************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandResult.ExitStatus;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testcontainers.DockerClientFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

public class GlassfishExtension implements TestRule, BeforeAllCallback, AfterAllCallback, BeforeEachCallback,
		AfterEachCallback, ExecutionCondition, ParameterResolver {

	private static final int httpPort = 8081;
	private final String contextRoot = "org.eclipse.mdm.nucleus";

	private OdsServerContainer odsServer;
	private Path projectRoot;
	private GlassFish glassfish;
	private String appName;
	private Client client;
	private static GlassFishRuntime glassfishRuntime;
	private NewCookie sessionCookie;

	static {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		try {
			glassfishRuntime = GlassFishRuntime.bootstrap();
		} catch (GlassFishException e) {
			throw new RuntimeException("Cannot bootstrap embedded Glassfish!", e);
		}
	}

	public GlassfishExtension(OdsServerContainer odsServer) {
		this(odsServer, Paths.get("../../"));
	}

	public GlassfishExtension(OdsServerContainer odsServer, Path projectRoot) {
		this.odsServer = odsServer;
		this.projectRoot = projectRoot;
		this.client = createClient();
		bootstrapSystemVariables();
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		if (odsServer.getDockerImageName().contains("busybox")) {
			return ConditionEvaluationResult.disabled("ODS Server container not available.");
		} else {
			if (DockerClientFactory.instance().isDockerAvailable()) {
				return ConditionEvaluationResult.enabled("ODS Server container created.");
			} else {
				return ConditionEvaluationResult.disabled("Docker is not available");
			}
		}
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		startGlassfish();
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		stopGlassfish();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		login();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		logout();
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				startGlassfish();
				base.evaluate();
				stopGlassfish();
			}
		};
	}

	private void startGlassfish() throws Exception {
		if (odsServer.isCreated()) {
			Path configRoot = projectRoot.resolve("tools/testutils/src/main/resources/config-root/").toAbsolutePath()
					.normalize();

			System.setProperty("java.security.auth.login.config",
					configRoot.resolve("login.conf").toAbsolutePath().toString());
			System.setProperty("odsserver.url", odsServer.getConnectionParameters().get("url"));

			GlassFishProperties glassfishProperties = new GlassFishProperties();
			glassfishProperties.setPort("http-listener", httpPort);

			glassfish = glassfishRuntime.newGlassFish(glassfishProperties);
			glassfish.start();

			createMDMRealm(configRoot);
			createJdbcResource();

			URI archiveUri = createDeployment().toURI();
			appName = glassfish.getDeployer().deploy(archiveUri, "--contextroot=" + contextRoot);
		}
	}

	private void stopGlassfish() throws Exception {
		if (glassfish != null && appName != null) {
			glassfish.getDeployer().undeploy(appName);
			glassfish.stop();
		}
	}

	private void bootstrapSystemVariables() {
		try {
			System.setProperty("org.eclipse.mdm.configPath",
					projectRoot.resolve("tools/testutils/src/main/resources").toRealPath().toString());
		} catch (IOException e) {
			throw new RuntimeException("Cannot resolve org.eclipse.mdm.configPath.", e);
		}
	}

	private void createMDMRealm(Path configRoot) throws GlassFishException {
		Path keyfile = configRoot.resolve("mdm-keyfile");

		CommandResult r = glassfish.getCommandRunner().run("create-auth-realm",
				"--classname=com.sun.enterprise.security.auth.realm.file.FileRealm",
				"--property=file=" + keyfile.toAbsolutePath().toString().replace("\\", "/").replace(":", "\\:")
						+ ":jaas-context=MDMRealm:assign-groups=MDM",
				"MDMRealm");
		if (r.getExitStatus() != ExitStatus.SUCCESS) {
			throw new RuntimeException(r.getOutput(), r.getFailureCause());
		}
		CommandResult r2 = glassfish.getCommandRunner().run("set",
				"server-config.security-service.default-realm=MDMRealm");
		if (r2.getExitStatus() != ExitStatus.SUCCESS) {
			throw new RuntimeException(r2.getOutput(), r2.getFailureCause());
		}
	}

	private void createJdbcResource() throws GlassFishException {
		CommandResult r2 = glassfish.getCommandRunner().run("create-jdbc-resource", "--connectionpoolid", "DerbyPool",
				"jdbc/openMDM");
		if (r2.getExitStatus() != ExitStatus.SUCCESS) {
			throw new RuntimeException(r2.getOutput(), r2.getFailureCause());
		}
	}

	private ScatteredArchive createDeployment() throws IOException {
		Path rootDir = Files.createTempDirectory("gf_rootdir");
		copyDirectory(projectRoot.resolve("nucleus/application/src/main/webapp"), rootDir);
		Files.copy(projectRoot.resolve("nucleus/webclient/src/main/webapp/src/index.html"),
				rootDir.resolve("index.html"));

		ScatteredArchive archive = new ScatteredArchive(contextRoot, ScatteredArchive.Type.WAR, rootDir.toFile());

		// Use web.xml with declared roles because gradle build needs it
		archive.addMetadata(projectRoot.resolve("nucleus/application/src/test/resources/web.xml").toFile());
		archive.addMetadata(projectRoot.resolve("nucleus/application/src/main/webconfig/glassfish-web.xml").toFile());

		// Eclipse IDE per default uses bin/main for class files and resources
		File f = projectRoot.resolve("nucleus/application/bin/main").toFile();
		if (f.exists()) {
			archive.addClassPath(f);
		}
		archive.addClassPath(projectRoot.resolve("nucleus/application/build/classes/java/main").toFile());
		archive.addClassPath(projectRoot.resolve("nucleus/application/build/resources/main").toFile());

//		Stream.of(System.getProperty("java.class.path", "").split(File.pathSeparator)).forEach(System.out::println);
		archive.addCurrentClassPath(new Predicate<String>() {
			@Override
			public boolean test(String t) {
				return !new File(t).exists() || t.contains("glassfish-embedded-all-") || t.contains("freetextindexer");
			}
		});
		return archive;
	}

	public static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
		Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
				if (!Files.exists(targetPath)) {
					Files.createDirectories(targetPath);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path targetPath = targetDir.resolve(sourceDir.relativize(file));
				Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public String getUri() {
		return "http://localhost:" + httpPort + "/" + contextRoot + "/";
	}

	private Client createClient() {
		ClientConfig config = new ClientConfig();
		config.connectorProvider(new ApacheConnectorProvider());
		config.property(ApacheClientProperties.DISABLE_COOKIES, false);
		config.property(ClientProperties.FOLLOW_REDIRECTS, false);
		config.register(JacksonFeature.class);
		config.register(MultiPartFeature.class);
		return ClientBuilder.newClient(config);
	}

	public WebTarget getRoot() {
		return client.target(getUri());
	}

	public NewCookie login() {
		Form form = new Form();
		form.param("j_username", "sa");
		form.param("j_password", "sa");

		Response loginResponse = getRoot().path("j_security_check").request().post(Entity.form(form));
		String body = loginResponse.readEntity(String.class);
		if (loginResponse.getStatus() != 303) {
			throw new RuntimeException("Login failed: " + loginResponse.getStatus() + " " + body);
		} else {
			sessionCookie = loginResponse.getCookies().get("JSESSIONID");
			loginResponse.close();
			return sessionCookie;
		}
	}

	public void logout() {
		Response r = getRoot().path("mdm/logout").request().get();
		String body = r.readEntity(String.class);
		if (r.getStatus() != 302) {
			throw new RuntimeException("Logout failed: " + body);
		}

		r.close();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().equals(NewCookie.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return sessionCookie;
	}
}
