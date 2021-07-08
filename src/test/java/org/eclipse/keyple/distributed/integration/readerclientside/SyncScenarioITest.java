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
package org.eclipse.keyple.distributed.integration.readerclientside;

import java.util.UUID;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.LocalServiceClientFactoryBuilder;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubSyncEndpointClient;
import org.eclipse.keyple.distributed.integration.readerclientside.model.InputDataDto;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.junit.*;
import org.junit.rules.TestName;

public class SyncScenarioITest extends BaseScenario {

  SyncEndpointClientSpi endpointClient;

  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {

    localServiceName = testName.getMethodName() + "_sync";

    /*
     * Server side :
     * <ul>
     *   <li>initialize the plugin with a sync node</li>
     *   <li>attach the plugin observer</li>
     * </ul>
     */
    initRemotePluginWithSyncNode();

    /*
     * Client side :
     * <ul>
     *   <li>register stub plugin</li>
     *   <li>create local stub reader</li>
     *   <li>create a sync client endpoint</li>
     *  <li>generate userId</li>
     * </ul>
     */
    initLocalStubPlugin();
    endpointClient = new StubSyncEndpointClient(false);
    initLocalService();
    user1 = new InputDataDto().setUserId(UUID.randomUUID().toString());
  }

  private void initLocalService() {
    localServiceExtension =
        SmartCardServiceProvider.getService()
            .registerDistributedLocalService(
                LocalServiceClientFactoryBuilder.builder(localServiceName)
                    .withSyncNode(endpointClient)
                    .build())
            .getExtension(LocalServiceClient.class);
  }

  @After
  public void tearDown() {
    clearLocalReader();
    unregisterPlugins();
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

  @Override
  @Test
  public void execute_multiClients_remoteSelection_remoteTransaction_successful() {
    multiClients_remoteSelection_remoteTransaction_successful();
  }

  @Override
  @Test
  public void execute_transaction_closeSession_card_error() {
    transaction_closeSession_fail();
  }

  @Override
  @Test(expected = StubNetworkConnectionException.class)
  public void execute_transaction_host_network_error() {
    SmartCardServiceProvider.getService().unregisterDistributedLocalService(localServiceName);
    endpointClient = new StubSyncEndpointClient(true);
    initLocalService();
    remoteSelection_remoteTransaction();
  }

  @Test
  @Override
  public void execute_transaction_client_network_error() {
    // not needed for sync node
  }

  @Test
  @Override
  public void execute_all_methods() {
    all_methods();
  }
}
