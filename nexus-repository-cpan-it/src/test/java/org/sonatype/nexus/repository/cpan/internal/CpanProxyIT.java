/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2019-present Sonatype, Inc.
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
import org.sonatype.nexus.testsuite.testsupport.FormatClientSupport;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.content;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.error;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class CpanProxyIT
    extends CpanITSupport
{
  private static final String TEST_PATH = "authors/id/E/EH/EHUELS/Test-Dependencies-0.24.tar.gz";

  private CpanClient proxyClient;

  private Repository proxyRepo;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-cpan")
    );
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    Server server = Server.withPort(0).serve("/*")
        .withBehaviours(error(HttpStatus.NOT_FOUND))
        .start();
    try {
      proxyRepo = repos.createCpanProxy("cpan-test-proxy-notfound", server.getUrl().toExternalForm());
      proxyClient = cpanClient(proxyRepo);
      MatcherAssert.assertThat(FormatClientSupport.status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
    }
    finally {
      server.stop();
    }
  }

  @Test
  public void retrieveTarGzWhenRemoteOnline() throws Exception {
    Server server = Server.withPort(0).serve("/*")
        .withBehaviours(Behaviours.file(testData.resolveFile("Test-Dependencies-0.24.tar.gz")))
        .start();
    try {
      proxyRepo = repos.createCpanProxy("cpan-test-proxy-online", server.getUrl().toExternalForm());
      proxyClient = cpanClient(proxyRepo);
      proxyClient.get(TEST_PATH);
    }
    finally {
      server.stop();
    }
    assertThat(status(proxyClient.get(TEST_PATH)), is(200));
  }
}
