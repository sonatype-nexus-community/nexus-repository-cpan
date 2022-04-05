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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.cache.NegativeCacheFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheHandler;
import org.sonatype.nexus.repository.content.maintenance.SingleAssetMaintenanceFacet;
import org.sonatype.nexus.repository.content.search.SearchFacet;
import org.sonatype.nexus.repository.cpan.datastore.CpanContentFacet;
import org.sonatype.nexus.repository.cpan.internal.AssetKind;
import org.sonatype.nexus.repository.cpan.internal.CpanFormat;
import org.sonatype.nexus.repository.cpan.internal.CpanSecurityFacet;
import org.sonatype.nexus.repository.http.HttpHandlers;
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.proxy.ProxyHandler;
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler;
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.HandlerContributor;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.ActionMatcher;
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static org.sonatype.nexus.repository.cpan.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.cpan.internal.AssetKind.CHECKSUM;
import static org.sonatype.nexus.repository.cpan.internal.AssetKind.VARIOUS;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;

@Named(CpanFormat.NAME)
@Singleton
public class CpanProxyRecipe
    extends RecipeSupport
{
  public static final String NAME = "cpan-proxy";

  private final ExceptionHandler exceptionHandler;

  private final TimingHandler timingHandler;

  private final SecurityHandler securityHandler;

  private final PartialFetchHandler partialFetchHandler;

  private final ConditionalRequestHandler conditionalRequestHandler;

  private final ContentHeadersHandler contentHeadersHandler;

  private final ProxyHandler proxyHandler;

  private final NegativeCacheHandler negativeCacheHandler;

  private final HandlerContributor handlerContributor;

  private final Provider<CpanContentFacet> contentFacet;

  private final Provider<CpanProxyFacetImpl> proxyFacet;

  private final Provider<CpanSecurityFacet> securityFacet;

  private final Provider<ConfigurableViewFacet> viewFacet;

  private final Provider<SearchFacet> searchFacet;

  private final Provider<SingleAssetMaintenanceFacet> componentMaintenanceFacet;

  private final Provider<HttpClientFacet> httpClientFacet;

  private final Provider<NegativeCacheFacet> negativeCacheFacet;

  private final Provider<PurgeUnusedFacet> purgeUnusedFacet;

  @Inject
  public CpanProxyRecipe(
      final Type type,
      final Format format,
      final ExceptionHandler exceptionHandler,
      final TimingHandler timingHandler,
      final SecurityHandler securityHandler,
      final PartialFetchHandler partialFetchHandler,
      final ConditionalRequestHandler conditionalRequestHandler,
      final ContentHeadersHandler contentHeadersHandler,
      final ProxyHandler proxyHandler,
      final NegativeCacheHandler negativeCacheHandler,
      final HandlerContributor handlerContributor,
      final Provider<CpanContentFacet> contentFacet,
      final Provider<CpanProxyFacetImpl> proxyFacet,
      final Provider<CpanSecurityFacet> securityFacet,
      final Provider<ConfigurableViewFacet> viewFacet,
      final Provider<SearchFacet> searchFacet,
      final Provider<SingleAssetMaintenanceFacet> componentMaintenanceFacet,
      final Provider<HttpClientFacet> httpClientFacet,
      final Provider<NegativeCacheFacet> negativeCacheFacet,
      final Provider<PurgeUnusedFacet> purgeUnusedFacet)
  {
    super(type, format);
    this.exceptionHandler = exceptionHandler;
    this.timingHandler = timingHandler;
    this.securityHandler = securityHandler;
    this.partialFetchHandler = partialFetchHandler;
    this.conditionalRequestHandler = conditionalRequestHandler;
    this.contentHeadersHandler = contentHeadersHandler;
    this.proxyHandler = proxyHandler;
    this.negativeCacheHandler = negativeCacheHandler;
    this.handlerContributor = handlerContributor;
    this.contentFacet = contentFacet;
    this.proxyFacet = proxyFacet;
    this.securityFacet = securityFacet;
    this.viewFacet = viewFacet;
    this.searchFacet = searchFacet;
    this.componentMaintenanceFacet = componentMaintenanceFacet;
    this.httpClientFacet = httpClientFacet;
    this.negativeCacheFacet = negativeCacheFacet;
    this.purgeUnusedFacet = purgeUnusedFacet;
  }

  @Override
  public void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get());
    repository.attach(configure(viewFacet.get()));
    repository.attach(contentFacet.get());
    repository.attach(httpClientFacet.get());
    repository.attach(negativeCacheFacet.get());
    repository.attach(componentMaintenanceFacet.get());
    repository.attach(proxyFacet.get());
    repository.attach(searchFacet.get());
    repository.attach(purgeUnusedFacet.get());
  }

  private static Handler createAssetKindHandler(final AssetKind kind) {
    return context -> {
      context.getAttributes().set(AssetKind.class, kind);
      return context.proceed();
    };
  }

  /**
   * Matcher for archive mapping.
   */
  static Route.Builder checksumMatcher() {
    return new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET, HEAD),
            new TokenMatcher("/{path:.+}/CHECKSUMS")
        ));
  }

  /**
   * Matcher for archive mapping.
   */
  static Route.Builder archiveMatcher() {
    return new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET, HEAD),
            new TokenMatcher("/{path:.+}/{filename:.+}.{extension:.*[tT][aA][rR]\\.[gG][zZ]}")
        ));
  }

  /**
   * Matcher for authors
   */
  static Route.Builder variousMatcher() {
    return new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET, HEAD),
            new TokenMatcher("/{path:.+}/{filename:.+}")
        ));
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    builder.route(checksumMatcher()
        .handler(timingHandler)
        .handler(createAssetKindHandler(CHECKSUM))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(proxyHandler)
        .create());

    builder.route(archiveMatcher()
        .handler(timingHandler)
        .handler(createAssetKindHandler(ARCHIVE))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(proxyHandler)
        .create());

    builder.route(variousMatcher()
        .handler(timingHandler)
        .handler(createAssetKindHandler(VARIOUS))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(proxyHandler)
        .create());

    builder.defaultHandlers(HttpHandlers.notFound());
    facet.configure(builder.create());
    return facet;
  }
}
