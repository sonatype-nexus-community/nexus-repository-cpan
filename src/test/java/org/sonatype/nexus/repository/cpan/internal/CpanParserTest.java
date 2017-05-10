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

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class CpanParserTest
    extends TestSupport
{
  static final String PACKAGE_NAME = "Geo-Calc-0.12.tar";

  InputStream cpanPackage;

  CpanAttributes attributes;

  CpanParser underTest;

  @Before
  public void setup() throws Exception {
    underTest = new CpanParser();
    cpanPackage = getClass().getResourceAsStream(PACKAGE_NAME);
    this.attributes = underTest.parse(cpanPackage);
  }

  @Test
  public void parseName() throws Exception {
    assertThat(attributes.getName(), is(equalTo("Geo-Calc")));
  }

  @Test
  public void parseVersion() throws Exception {
    assertThat(attributes.getVersion(), is(equalTo("0.12")));
  }

  @Test
  public void parseAbstract() throws Exception {
    assertThat(attributes.getAbstractName(), is(equalTo("Geographical Calc")));
  }

  @Test
  public void parseLicense() throws Exception {
    assertThat(attributes.getLicense(), is(equalTo("perl")));
  }

  @Test
  public void parseAuthor() throws Exception {
    assertThat(attributes.getAuthor(), is(equalTo("Sorin Pop")));
  }

  @Test
  public void parseGeneratedBy() throws Exception {
    assertThat(attributes.getGeneratedBy(), is(equalTo("ExtUtils::MakeMaker version 6.42")));
  }

  @Test
  public void parseDistributionType() throws Exception {
    assertThat(attributes.getDistributionType(), is(equalTo("module")));
  }

/*  @Test
  public void setRequires() throws Exception {
    assertThat(attributes.getRequires().size(), is(equalTo(8)));
    assertRequired(attributes.getRequires().get(0), "Math::Trig", "1.04");
    assertRequired(attributes.getRequires().get(1), "Test::More", "0.47");
    assertRequired(attributes.getRequires().get(2), "Moose", "1.19");
    assertRequired(attributes.getRequires().get(3), "MooseX::FollowPBP", "0.04");
    assertRequired(attributes.getRequires().get(4), "MooseX::Method::Signatures", "0.36");
    assertRequired(attributes.getRequires().get(5), "Math::BigFloat", "1.6");
    assertRequired(attributes.getRequires().get(6), "Math::Units", "1.3");
    assertRequired(attributes.getRequires().get(7), "POSIX", null);
  }*/

  private void assertRequired(final CpanRequired firstRequired, final String name, final String version) {
    assertThat(firstRequired.getName(), is(equalTo(name)));
    assertThat(firstRequired.getVersion(), is(equalTo(version)));
  }
}
