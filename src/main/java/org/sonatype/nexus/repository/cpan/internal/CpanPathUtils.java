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

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.name;

/**
 * Utility Methods for working CPAN routes and paths
 */
public final class CpanPathUtils {
    /**
     * * Returns the name from a {@link TokenMatcher.State}.
     */
    static String path(final TokenMatcher.State state) {
        return match(state, "path");
    }

    private static String match(TokenMatcher.State state, String path) {
        checkNotNull(state);
        String result = state.getTokens().get(name);
        checkNotNull(result);
        return result;
    }

    private CpanPathUtils() {
        // empty
    }

    /**
     * Builds a path to an archive for a particular path and filename.
     */
    static String path(final String path, final String filename) {
        return path + "/" + filename;
    }

    /**
     * Returns the filename from a {@link TokenMatcher.State}.
     */
    static String filename(final TokenMatcher.State state) {
        return match(state, "filename");
    }

    /**
     * Returns the {@link TokenMatcher.State} for the content.
     */
    static TokenMatcher.State matcherState(final Context context) {
        return context.getAttributes().require(TokenMatcher.State.class);
    }
}