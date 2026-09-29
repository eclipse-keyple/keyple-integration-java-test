/* **************************************************************************************
 * Copyright (c) 2026 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Distribution License 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 ************************************************************************************** */
package org.example.thirdparty;

import org.eclipse.keyple.core.common.CommonApiProperties;
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory;
import org.eclipse.keyple.core.plugin.PluginApiProperties;
import org.eclipse.keyple.core.plugin.spi.PluginFactorySpi;
import org.eclipse.keyple.core.plugin.spi.PluginSpi;

/** Factory of the {@link ThirdPartyPlugin}. */
public class ThirdPartyPluginFactory implements KeyplePluginExtensionFactory, PluginFactorySpi {

  @Override
  public String getPluginApiVersion() {
    return PluginApiProperties.VERSION;
  }

  @Override
  public String getCommonApiVersion() {
    return CommonApiProperties.VERSION;
  }

  @Override
  public String getPluginName() {
    return ThirdPartyPlugin.NAME;
  }

  @Override
  public PluginSpi getPlugin() {
    return new ThirdPartyPlugin();
  }
}
