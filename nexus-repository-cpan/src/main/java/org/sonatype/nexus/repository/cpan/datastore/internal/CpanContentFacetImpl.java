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
package org.sonatype.nexus.repository.cpan.datastore.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.content.facet.ContentFacetSupport;
import org.sonatype.nexus.repository.content.fluent.FluentAsset;
import org.sonatype.nexus.repository.content.fluent.FluentAssetBuilder;
import org.sonatype.nexus.repository.content.fluent.FluentComponent;
import org.sonatype.nexus.repository.content.store.FormatStoreManager;
import org.sonatype.nexus.repository.cpan.datastore.CpanContentFacet;
import org.sonatype.nexus.repository.cpan.internal.AssetKind;
import org.sonatype.nexus.repository.cpan.internal.CpanAttributes;
import org.sonatype.nexus.repository.cpan.internal.CpanParser;
import org.sonatype.nexus.repository.cpan.internal.CpanPathUtils;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import com.google.common.collect.ImmutableList;

import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;

@Named
public class CpanContentFacetImpl
    extends ContentFacetSupport
    implements CpanContentFacet
{
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA1);

  private final CpanParser cpanParser;

  private final CpanPathUtils cpanPathUtils;

  @Inject
  protected CpanContentFacetImpl(
      final FormatStoreManager formatStoreManager,
      final CpanParser cpanParser,
      final CpanPathUtils cpanPathUtils)
  {
    super(formatStoreManager);
    this.cpanParser = cpanParser;
    this.cpanPathUtils = cpanPathUtils;
  }

  @Override
  public Optional<Content> get(final String path) {
    return assets().path(path)
        .find()
        .map(FluentAsset::download);
  }

  @Override
  public Content putArchive(final String path, final String filename, final String extension, final Payload payload) {
    try (TempBlob blob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      CpanAttributes cpanAttributes  = parse(blob);

      FluentComponent component = components().name(cpanAttributes.getName())
          .version(cpanAttributes.getVersion())
          .getOrCreate();

      String assetPath = cpanPathUtils.archivePath(path, filename, extension);

      return createAsset(Optional.of(component), assetPath, AssetKind.ARCHIVE, payload, blob);
    }
  }

  @Override
  public Content putChecksum(final String path, final Payload payload) {
    String assetPath = cpanPathUtils.checksumPath(path);

    return createAsset(Optional.empty(), assetPath, AssetKind.CHECKSUM, payload);
  }

  @Override
  public Content putVarious(final String path, final String filename, final Payload payload) {
    String assetPath = cpanPathUtils.path(path, filename);

    return createAsset(Optional.empty(), assetPath, AssetKind.VARIOUS, payload);
  }

  private Content createAsset(
      final Optional<FluentComponent> component,
      final String path,
      final AssetKind kind,
      final Payload payload) {
    try (TempBlob blob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      return createAsset(component, path, kind, payload, blob);
    }
  }

  private Content createAsset(
      final Optional<FluentComponent> component,
      final String path,
      final AssetKind kind,
      final Payload payload,
      final TempBlob blob)
  {
    FluentAssetBuilder build = assets().path(path)
        .blob(blob)
        .kind(AssetKind.ARCHIVE.name());

    component.ifPresent(build::component);

    return build.save()
        .markAsCached(payload)
        .download();
  }

  private CpanAttributes parse(final TempBlob blob) {
    try (InputStream in = blob.get()) {
      return cpanParser.parse(in);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
