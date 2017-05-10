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
/*global Ext, NX*/

/**
 * Repository "Settings" form for a Cpan Proxy repository
 */
Ext.define('NX.cpanui.view.repository.recipe.CpanProxy', {
  extend: 'NX.cpanui.view.repository.RepositorySettingsForm',
  alias: 'widget.nx-cpanui-repository-cpan-proxy',
  requires: [
    'NX.cpanui.view.repository.facet.CpanFacet',
    'NX.cpanui.view.repository.facet.ProxyFacet',
    'NX.cpanui.view.repository.facet.StorageFacet',
    'NX.cpanui.view.repository.facet.HttpClientFacet',
    'NX.cpanui.view.repository.facet.NegativeCacheFacet'
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = [
      {xtype: 'nx-cpanui-repository-cpan-facet'},
      {xtype: 'nx-cpanui-repository-proxy-facet'},
      {xtype: 'nx-cpanui-repository-storage-facet'},
      {xtype: 'nx-cpanui-repository-negativecache-facet'},
      {xtype: 'nx-cpanui-repository-httpclient-facet'}
    ];

    me.callParent();
  }
});