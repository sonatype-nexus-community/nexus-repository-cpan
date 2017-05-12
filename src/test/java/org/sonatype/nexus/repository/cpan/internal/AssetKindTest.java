package org.sonatype.nexus.repository.cpan.internal;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.cache.CacheControllerHolder;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test for {@link AssetKind}
 */
public class AssetKindTest
    extends TestSupport
{
  @Test
  public void testArchiveIsContentCacheType() {
    assertThat(AssetKind.ARCHIVE.getCacheType(), is(equalTo(CacheControllerHolder.CONTENT)));
  }
}
