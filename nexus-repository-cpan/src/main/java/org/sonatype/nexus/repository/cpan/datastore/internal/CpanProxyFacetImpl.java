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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.content.facet.ContentProxyFacetSupport;
import org.sonatype.nexus.repository.cpan.datastore.CpanContentFacet;
import org.sonatype.nexus.repository.cpan.internal.AssetKind;
import org.sonatype.nexus.repository.cpan.internal.CpanPathUtils;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
public class CpanProxyFacetImpl
  extends ContentProxyFacetSupport
{
  private final CpanPathUtils cpanPathUtils;

  @Inject
  public CpanProxyFacetImpl(final CpanPathUtils cpanPathUtils) {
    this.cpanPathUtils = checkNotNull(cpanPathUtils);
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
        return putArchive(cpanPathUtils.path(matcherState), cpanPathUtils.filename(matcherState),
            cpanPathUtils.extension(matcherState), content);
      case VARIOUS:
        log.debug(("VARIOUS" + cpanPathUtils.path(matcherState)));
        return putVarious(cpanPathUtils.path(matcherState), cpanPathUtils.filename(matcherState), content);
      case CHECKSUM:
        log.debug("CHECKSUM" + cpanPathUtils.path(matcherState));
        return putChecksum(cpanPathUtils.path(matcherState), content);
      default:
        throw new IllegalStateException();
    }
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = cpanPathUtils.matcherState(context);
    String path;
    switch (assetKind) {
      case ARCHIVE:
        path = cpanPathUtils.archivePath(cpanPathUtils.path(matcherState),
            cpanPathUtils.filename(matcherState), cpanPathUtils.extension(matcherState));
        return content().get(path)
            .orElse(null);
      case VARIOUS:
        path = cpanPathUtils.path(cpanPathUtils.path(matcherState), cpanPathUtils.filename(matcherState));
        return content().get(path)
            .orElse(null);
      case CHECKSUM:
        return content().get(cpanPathUtils.checksumPath(cpanPathUtils.path(matcherState)))
            .orElse(null);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putArchive(
      final String path,
      final String filename,
      final String extension,
      final Content content) throws IOException
  {
    return content().putArchive(path, filename, extension, content);
  }

  private Content putChecksum(final String path, final Content content) throws IOException {
    return content().putChecksum(path, content);
  }

  private Content putVarious(final String path, final String filename, final Content content) throws IOException {
    return content().putVarious(path, filename, content);
  }

  private CpanContentFacet content() {
    return getRepository().facet(CpanContentFacet.class);
  }
}
