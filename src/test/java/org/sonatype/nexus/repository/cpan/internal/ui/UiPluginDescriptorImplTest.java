package org.sonatype.nexus.repository.cpan.internal.ui;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UiPluginDescriptorImplTest
    extends TestSupport
{
  private static final String NAMESPACE = "NX.cpanui";
  private static final String CONFIG_CLASS_NAME = "NX.cpanui.app.PluginConfig";

  private UiPluginDescriptorImpl underTest;

  @Before
  public void setup() {
    underTest = new UiPluginDescriptorImpl();
  }

  @Test
  public void checkUiPluginDescriptorValues() {
    assertThat(underTest.getNamespace(), is(NAMESPACE));
    assertThat(underTest.getConfigClassName(), is(CONFIG_CLASS_NAME));
  }
}