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

import java.util.List;

public class CpanAttributes
{
  private String name;

  private String version;

  private String abstractName;

  private String license;

  private String author;

  private String generatedBy;

  private String distributionType;

  private List<CpanRequired> requires;

  public String getName() {
    return name;
  }

  public CpanAttributes setName(String name) {
    this.name = name;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public CpanAttributes setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getAbstractName() {
    return abstractName;
  }

  public CpanAttributes setAbstractName(String abstractName) {
    this.abstractName = abstractName;
    return this;
  }

  public String getLicense() {
    return license;
  }

  public CpanAttributes setLicense(String license) {
    this.license = license;
    return this;
  }

  public String getAuthor() {
    return author;
  }

  public CpanAttributes setAuthor(String author) {
    this.author = author;
    return this;
  }

  public String getGeneratedBy() {
    return generatedBy;
  }

  public CpanAttributes setGeneratedBy(String generatedBy) {
    this.generatedBy = generatedBy;
    return this;
  }

  public String getDistributionType() {
    return distributionType;
  }

  public CpanAttributes setDistributionType(String distributionType) {
    this.distributionType = distributionType;
    return this;
  }

  public List<CpanRequired> getRequires() {
    return requires;
  }

  public CpanAttributes setRequires(List<CpanRequired> requires) {
    this.requires = requires;
    return this;
  }
}
