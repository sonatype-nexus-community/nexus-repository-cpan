<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2018-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
## CPAN Repositories

### Introduction

### Proxying CPAN Repositories

You can set up an P2 proxy repository to access a remote repository location, for example to proxy the official CPAN
repository at [https://www.cpan.org/](https://www.cpan.org/)

To proxy a CPAN repository, you simply create a new 'cpan (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/display/NXRM3/Configuration#Configuration-RepositoryManagement) in
details. Minimal configuration steps are:

- Define 'Name'
- Define URL for 'Remote storage' e.g. [https://www.cpan.org/](https://www.cpan.org/)
- Select a 'Blob store' for 'Storage'

### Configuring CPAN for use with Nexus Repository

To configure CPAN to use Nexus Repository as a Proxy for remote CPAN sites, there is a great article [available here](http://perltricks.com/article/44/2013/10/20/Find-CPAN-mirrors-and-configure-the-local-CPAN-mirror-list/).

To help you out, here's how to set up a site using CPAN on Mac OS X:

- Start `cpan` in a terminal
- Run `o conf urllist` to show you the current mirrors that are setup
- Run `o conf urllist push http://nexusUrl:nexusPort/repository/cpan-proxy/` to add a CPAN proxy you setup in the previous step

NOTE: CPAN includes other mirror sites by default, you will want to remove these if you intend to proxy solely through Nexus Repository.
If you plan on doing so, just run the following command, omitting the `push` will replace your entire list:
 - `o conf urllist http://nexusUrl:nexusPort/repository/cpan-proxy/`
 
After you have done these steps, installs should get routed through Nexus Repository.

### Browsing CPAN Repository Packages

You can browse CPAN repositories in the user interface inspecting the components and assets and their details, as
described in [Browsing Repositories and Repository Groups](https://help.sonatype.com/display/NXRM3/Browsing+Repositories+and+Repository+Groups).
