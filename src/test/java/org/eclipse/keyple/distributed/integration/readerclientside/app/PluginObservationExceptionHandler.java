/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
