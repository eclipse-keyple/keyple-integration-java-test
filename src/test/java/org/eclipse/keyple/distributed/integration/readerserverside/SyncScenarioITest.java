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
import org.eclipse.keyple.distributed.*;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubSyncEndpointClient;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncScenarioITest extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(SyncScenarioITest.class);

  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {

    initLocalStubPlugin();

    localServiceName = testName.getMethodName() + "_sync";

    SyncEndpointClientSpi endpointClient = new StubSyncEndpointClient(localServiceName);

    localServiceExtension =
        SmartCardServiceProvider.getService()
            .registerDistributedLocalService(
                LocalServiceServerFactoryBuilder.builder(localServiceName).withSyncNode().build())
            .getExtension(LocalServiceServer.class);

    remotePluginClient =
        SmartCardServiceProvider.getService()
            .registerPlugin(
                RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                    .withSyncNode(endpointClient)
                    .withoutPluginObservation()
                    .withoutReaderObservation()
                    .build())
            .getExtension(RemotePluginClient.class);
  }

  @After
  public void tearDown() {
    SmartCardServiceProvider.getService().unregisterPlugin(REMOTE_PLUGIN_NAME);
  }

  @Test
  @Override
  public void execute_transaction_on_pool_reader() {}
}
