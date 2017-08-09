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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.*;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.cpan.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.cpan.internal.AssetKind.CHECKSUM;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

/**
 * CPAN {@link ProxyFacet}
 */
@Named
public class CpanProxyFacetImpl
    extends ProxyFacetSupport
{
  private final CpanParser cpanParser;
  private final CpanPathUtils cpanPathUtils;
  private final CpanDataAccess cpanDataAccess;

  @Inject
  public CpanProxyFacetImpl(final CpanParser cpanParser, final CpanPathUtils cpanPathUtils, final CpanDataAccess cpanDataAccess) {
    this.cpanParser = checkNotNull(cpanParser);
    this.cpanPathUtils = checkNotNull(cpanPathUtils);
    this.cpanDataAccess = checkNotNull(cpanDataAccess);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = cpanPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        log.debug("ARCHIVE" + cpanPathUtils.path(matcherState));
        return putArchive(cpanPathUtils.path(matcherState), cpanPathUtils.filename(matcherState), content);
      case CHECKSUM:
        log.debug("CHECKSUM" + cpanPathUtils.path(matcherState));
        return putChecksum(cpanPathUtils.path(matcherState), content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putChecksum(final String path, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), CpanDataAccess.HASH_ALGORITHMS)) {
      return doPutChecksum(path, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutChecksum(final String path,
                                final TempBlob metadataContent,
                                final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    String assetPath = cpanPathUtils.checksumPath(path);

    Asset asset = cpanDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, CHECKSUM.name());
    }
    return cpanDataAccess.saveAsset(tx, asset, metadataContent, payload);
  }

  private Content putArchive(final String path, final String filename, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), CpanDataAccess.HASH_ALGORITHMS)) {
      return doPutArchive(path, filename, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutArchive(final String path,
                                 final String filename,
                                 final TempBlob archiveContent,
                                 final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    String assetPath = cpanPathUtils.path(path, filename);

    CpanAttributes cpanAttributes;

    try (InputStream in = archiveContent.get()) {
      cpanAttributes = cpanParser.parse(in);
    }

    Component component = cpanDataAccess.findComponent(tx, getRepository(), cpanAttributes.getName(), cpanAttributes.getVersion());
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(cpanAttributes.getName())
          .version(cpanAttributes.getVersion());
    }
    tx.saveComponent(component);

    Asset asset = cpanDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, ARCHIVE.name());
    }
    return cpanDataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent CPAN asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = cpanPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return getAsset(cpanPathUtils.path(cpanPathUtils.path(matcherState), cpanPathUtils.filename(matcherState)));
      case CHECKSUM:
        return getAsset(cpanPathUtils.checksumPath(cpanPathUtils.path(matcherState)));
      default:
        throw new IllegalStateException();
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = cpanDataAccess.findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return cpanDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }
}
