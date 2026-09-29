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

import java.util.Collections;
import java.util.Set;
import org.eclipse.keyple.core.common.KeyplePluginExtension;
import org.eclipse.keyple.core.plugin.spi.PluginSpi;
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi;

/** Third-party plugin providing a single {@link ThirdPartyReader}. */
public class ThirdPartyPlugin implements PluginSpi, KeyplePluginExtension {

  public static final String NAME = "ThirdPartyPlugin";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<ReaderSpi> searchAvailableReaders() {
    return Collections.<ReaderSpi>singleton(new ThirdPartyReader());
  }

  @Override
  public void onUnregister() {}
}
