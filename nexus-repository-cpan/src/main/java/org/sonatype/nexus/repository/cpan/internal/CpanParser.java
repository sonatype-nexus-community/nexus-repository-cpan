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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

/**
 * Parses a CPAN package into {@link CpanAttributes}
 */
@Named
@Singleton
public class CpanParser
    extends ComponentSupport
{
  private static final String METADATA_OLD_FILENAME = "META.yml";
  private static final String METADATA_FILENAME = "META.json";

  public CpanAttributes parse(final InputStream in) {
    Map metadata = readMetadataFromTar(in);
    return new CpanAttributes()
        .setName(read("name", metadata))
        .setVersion(read("version", metadata))
        .setAbstractName(read("abstract", metadata))
        .setLicense(read("license", metadata))
        .setAuthor(read("author", metadata))
        .setGeneratedBy(read("generated_by", metadata))
        .setDistributionType(read("distribution_type", metadata));
        //.setRequires(readRequires(metadata));
  }

  private List<CpanRequired> readRequires(final Map metadata) {
    Map<String, Object> metadataRequires = (Map<String, Object>) metadata.get("requires");
    return metadataRequires.entrySet()
        .stream()
        .map(e -> new CpanRequired().setName(e.getKey()).setVersion(objectToString(e.getValue())))
        .collect(toList());
  }

  private Map readMetadataFromTar(final InputStream in) {
    try {
      try(GzipCompressorInputStream gzip = new GzipCompressorInputStream(in)) {
        try (TarArchiveInputStream tar = new TarArchiveInputStream(gzip)) {
          TarArchiveEntry entry;
          while ((entry = tar.getNextTarEntry()) != null) {
            if (entry.getName().contains(METADATA_FILENAME)) {
              return readJSONFile(tar);
            }
            else if (entry.getName().contains(METADATA_OLD_FILENAME)) {
              return readYAMLFile(tar);
            }
          }
        }
      }
    }
    catch (IOException e) {
      log.error("Failed to parse CPAN attributes", e);
    }
    return emptyMap();
  }

  private Map readYAMLFile(final TarArchiveInputStream tar) throws IOException {
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int length;
    while ((length = tar.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, length);
    }
    Yaml yaml = new Yaml();
    return (Map) yaml.load(new String(byteArrayOutputStream.toByteArray()));
  }

  private Map readJSONFile(final TarArchiveInputStream tar) throws IOException {
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int length;
    while ((length = tar.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, length);
    }
    ObjectMapper mapper = new ObjectMapper();
    return (Map) mapper.readValue(new String(byteArrayOutputStream.toByteArray()), Map.class);
  }

  private static String read(final String key, final Map metadata) {
    if (!metadata.containsKey(key)) {
      return null;
    }
    return objectToString(metadata.get(key));
  }

  private static String objectToString(final Object value) {
    if (value == null) {
      return null;
    }
    return String.valueOf(value);
  }
}
