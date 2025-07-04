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
package org.eclipse.keyple.distributed.integration.readerclientside;

import java.util.UUID;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.LocalServiceClientFactoryBuilder;
import org.eclipse.keyple.distributed.NodeCommunicationException;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubAsyncEndpointClient;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubAsyncEndpointServer;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.distributed.integration.readerclientside.model.InputDataDto;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncScenarioITest extends BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(AsyncScenarioITest.class);

  private static StubAsyncEndpointServer endpointServer;
  StubAsyncEndpointClient endpointClient;

  @Rule public TestName testName = new TestName();

  @BeforeClass
  public static void globalSetUp() {

    /*
     * Server side :
     * <ul>
     *   <li>create an instance of the endpointServer</li>
     * </ul>
     */
    endpointServer = new StubAsyncEndpointServer();
  }

  @Before
  public void setUp() {

    localServiceName = testName + "_async";

    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a async server node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemotePluginWithAsyncNode(endpointServer);

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create local stub reader</li>
     *   <li>create an async client endpoint</li>
     * <li>generate userId</li>
     * </ul>
     */
    initLocalStubPlugin();
    endpointClient = new StubAsyncEndpointClient(endpointServer, false, localServiceName);
    initLocalService();
    user1 = new InputDataDto().setUserId(UUID.randomUUID().toString());
  }

  @After
  public void tearDown() {
    clearLocalReader();
    unregisterPlugins();
  }

  private void initLocalService() {
    localServiceExtension =
        SmartCardServiceProvider.getService()
            .registerDistributedLocalService(
                LocalServiceClientFactoryBuilder.builder(localServiceName)
                    .withAsyncNode(endpointClient, 2)
                    .build())
            .getExtension(LocalServiceClient.class);
  }

  @Override
  @Test
  public void execute_localSelection_remoteTransaction_successful() {
    localSelection_remoteTransaction_successful();
  }

  @Override
  @Test
  public void execute_remoteSelection_remoteTransaction_successful() {
    remoteSelection_remoteTransaction_successful();
  }

  @Test
  @Override
  public void execute_multiClients_remoteSelection_remoteTransaction_successful() {
    multiClients_remoteSelection_remoteTransaction_successful();
  }

  @Test
  @Override
  public void execute_transaction_closeSession_card_error() {
    transaction_closeSession_fail();
  }

  @Test(expected = StubNetworkConnectionException.class)
  @Override
  public void execute_transaction_host_network_error() {
    SmartCardServiceProvider.getService().unregisterDistributedLocalService(localServiceName);
    endpointClient = new StubAsyncEndpointClient(endpointServer, true, localServiceName);
    initLocalService();
    remoteSelection_remoteTransaction();
  }

  @Test(expected = NodeCommunicationException.class)
  @Override
  public void execute_transaction_client_network_error() {
    endpointServer.setSimulateConnectionError(true);
    remoteSelection_remoteTransaction();
  }

  @Override
  @Test
  public void execute_all_methods() {
    all_methods();
  }
}
