/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CpanProxyFacetImpl}
 */
public class CpanProxyFacetImplTest
    extends TestSupport
{
  private CpanProxyFacetImpl underTest;
  private CpanParser cpanParser;
  private CpanPathUtils cpanPathUtils;
  private CpanDataAccess cpanDataAccess;

  @Mock
  Context context;

  @Mock
  Request request;

  @Before
  public void setUp() throws Exception {
    cpanParser = new CpanParser();
    cpanPathUtils = new CpanPathUtils();
    cpanDataAccess = new CpanDataAccess();
    underTest = new CpanProxyFacetImpl(cpanParser, cpanPathUtils, cpanDataAccess);
  }

  @Test
  public void getUrl() throws Exception {
    String testUrl = "/repository/cpan-proxy";
    String expectedUrl = testUrl.substring(1);

    when(context.getRequest()).thenReturn(request);
    when(request.getPath()).thenReturn(testUrl);

    assertThat(underTest.getUrl(context), is(expectedUrl));
  }
}