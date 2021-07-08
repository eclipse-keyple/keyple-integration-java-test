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
package org.eclipse.keyple.distributed.integration.readerserverside;

import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceServer;
import org.eclipse.keyple.distributed.LocalServiceServerFactoryBuilder;
import org.eclipse.keyple.distributed.RemotePluginClient;
import org.eclipse.keyple.distributed.RemotePluginClientFactoryBuilder;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubAsyncEndpointClient;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubAsyncEndpointServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncScenarioITest extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(AsyncScenarioITest.class);

  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {

    initLocalStubPlugin();

    localServiceName = testName.getMethodName() + "_async";

    StubAsyncEndpointServer endpointServer = new StubAsyncEndpointServer(localServiceName);
    StubAsyncEndpointClient endpointClient = new StubAsyncEndpointClient(endpointServer);

    localServiceExtension =
        SmartCardServiceProvider.getService()
            .registerDistributedLocalService(
                LocalServiceServerFactoryBuilder.builder(localServiceName)
                    .withAsyncNode(endpointServer)
                    .build())
            .getExtension(LocalServiceServer.class);

    remotePluginClient =
        SmartCardServiceProvider.getService()
            .registerPlugin(
                RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                    .withAsyncNode(endpointClient, 2)
                    .build())
            .getExtension(RemotePluginClient.class);
  }

  @After
  public void tearDown() {
    SmartCardServiceProvider.getService().unregisterPlugin(REMOTE_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterDistributedLocalService(localServiceName);
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_PLUGIN_NAME);
  }

  @Test
  @Override
  public void execute_transaction_on_pool_reader() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
