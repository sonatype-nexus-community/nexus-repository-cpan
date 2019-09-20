/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.cpan.internal;

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.error;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class CpanProxyIT
    extends CpanITSupport
{
  private static final String PACKAGE_NAME = "Test-Dependencies-0.24.tar.gz";

  private static final String INVALID_PACKAGE_NAME = "Test-Dependencies-0.24.zip";

  private static final String BASE_PATH = "authors/id/E/EH/EHUELS/";

  private static final String BAD_PATH = "/this/path/is/not/valid";

  private static final String VALID_PACKAGE_URL = BASE_PATH + PACKAGE_NAME;

  private static final String INVALID_PACKAGE_URL = BASE_PATH + INVALID_PACKAGE_NAME;

  private CpanClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-cpan")
    );
  }

  @Before
  public void setup() throws Exception {
    server = Server.withPort(0)
        .serve("/" + VALID_PACKAGE_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(PACKAGE_NAME)))
        .start();

    proxyRepo = repos.createCpanProxy("cpan-test-proxy", server.getUrl().toExternalForm());
    proxyClient = cpanClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    assertThat(status(proxyClient.get(BAD_PATH)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveTarGzFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(VALID_PACKAGE_URL)), is(HttpStatus.OK));
  }

  @Test
  public void retrieveZipFromProxyShouldNotWork() throws Exception {
    assertThat(status(proxyClient.get(INVALID_PACKAGE_URL)), is(HttpStatus.NOT_FOUND));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
