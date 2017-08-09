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

import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CpanPathUtils}
 */
public class CpanPathUtilsTest
    extends TestSupport
{
  private CpanPathUtils underTest;

  @Mock
  TokenMatcher.State matcherState;

  @Mock
  Context context;

  @Mock
  Map<String, String> tokens;

  @Mock
  AttributesMap attributesMap;

  @Before
  public void setup() {
    underTest = new CpanPathUtils();
  }

  @Test
  public void testMatch() {
    String result = "name";
    when(matcherState.getTokens()).thenReturn(tokens);
    when(tokens.get(any())).thenReturn(result);

    assertThat(underTest.path(matcherState), is(result));
  }

  @Test(expected = NullPointerException.class)
  public void matchWithNullResultThrowsNullPointerException() {
    when(matcherState.getTokens()).thenReturn(tokens);

    underTest.path(matcherState);
  }

  @Test(expected = NullPointerException.class)
  public void matchWithNullMatcherStateThrowsNullPointerException() {
    underTest.path(null);
  }

  @Test
  public void getFilename() {
    String result = "filename.extension";
    when(matcherState.getTokens()).thenReturn(tokens);
    when(tokens.get(any())).thenReturn(result);
    assertThat(underTest.filename(matcherState), is(result));
  }

  @Test
  public void getMatcherStateFromContext() {
    when(context.getAttributes()).thenReturn(attributesMap);
    when(attributesMap.require(TokenMatcher.State.class)).thenReturn(matcherState);
    assertThat(underTest.matcherState(context), is(matcherState));
  }

  @Test
  public void getPathWithPathAndFilename() {
    String path = "path";
    String filename = "filename.extension";
    String expectedResult = path + "/" + filename;
    assertThat(underTest.path(path, filename), is(expectedResult));
  }

  @Test
  public void getChecksumPath() {
    String path = "path";
    String expectedResult = path + "/CHECKSUM";
    assertThat(underTest.checksumPath(path), is(expectedResult));
  }
}
