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
package org.sonatype.nexus.repository.cpan.internal.proxy;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.cpan.internal.CpanFormat;
import org.sonatype.nexus.repository.cpan.internal.CpanProxyRecipe;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

public class CpanProxyRecipeTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  Context context;

  CpanProxyRecipe recipe;

  @Before
  public void setup() {
    recipe = new CpanProxyRecipe(new ProxyType(), new CpanFormat());
    when(context.getRequest()).thenReturn(request);
  }

  @After
  public void tearDown() {
    System.getProperties().remove("nexus.cpan.enabled");
  }

  @Test
  public void enableYum() throws Exception {
    System.setProperty("nexus.cpan.enabled", "true");
  }
}
