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
package org.eclipse.keyple.distributed.integration.readerserverside;

import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.*;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubAsyncEndpointClient;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubAsyncEndpointServer;
import org.junit.After;
import org.junit.Test;

public class AsyncScenarioITest extends BaseScenario {

  private final StubAsyncEndpointServer endpointServer =
      new StubAsyncEndpointServer(LOCAL_SERVICE_NAME);
  private final StubAsyncEndpointClient endpointClient =
      new StubAsyncEndpointClient(endpointServer);

  @After
  public void tearDown() {
    SmartCardServiceProvider.getService().unregisterPlugin(REMOTE_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterDistributedLocalService(LOCAL_SERVICE_NAME);
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_CARDRESOURCE_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_PLUGIN_NAME);
  }

  @Override
  public void execute_transaction_with_regular_plugin() {

    // Init server
    initLocalStubPlugin();
    SmartCardServiceProvider.getService()
        .registerDistributedLocalService(
            LocalServiceServerFactoryBuilder.builder(LOCAL_SERVICE_NAME)
                .withAsyncNode(endpointServer)
                .build())
        .getExtension(LocalServiceServer.class);

    // Init client
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withAsyncNode(endpointClient, 2)
                .build());

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  @Override
  public void execute_transaction_with_pool_plugin() {

    // Init server
    initLocalStubPlugin();
    initLocalCardResourceService();
    initLocalCardResourcePlugin();
    SmartCardServiceProvider.getService()
        .registerDistributedLocalService(
            LocalServiceServerFactoryBuilder.builder(LOCAL_SERVICE_NAME)
                .withAsyncNode(endpointServer)
                .withPoolPlugins(LOCAL_CARDRESOURCE_PLUGIN_NAME)
                .build())
        .getExtension(LocalServiceServer.class);

    // Init client
    remotePlugin =
        (PoolPlugin)
            SmartCardServiceProvider.getService()
                .registerPlugin(
                    RemotePoolPluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                        .withAsyncNode(endpointClient, 2)
                        .build());

    executePoolPluginScenario();
  }
}
