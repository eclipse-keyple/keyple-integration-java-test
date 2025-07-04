/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
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
package org.eclipse.keyple.distributed.integration.readerclientside.app;

import org.eclipse.keyple.core.service.spi.PluginObservationExceptionHandlerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginObservationExceptionHandler implements PluginObservationExceptionHandlerSpi {

  private static final Logger logger =
      LoggerFactory.getLogger(PluginObservationExceptionHandler.class);

  @Override
  public void onPluginObservationError(String pluginName, Throwable e) {
    logger.error(pluginName, e);
  }
}
