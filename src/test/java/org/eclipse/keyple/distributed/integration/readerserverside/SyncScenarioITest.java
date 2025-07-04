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

import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.*;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubSyncEndpointClient;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.eclipse.keypop.reader.ObservableCardReader;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.CardSelectionManager;
import org.eclipse.keypop.reader.selection.CommonIsoCardSelector;
import org.junit.After;
import org.junit.Test;

public class SyncScenarioITest extends BaseScenario {

  private final SyncEndpointClientSpi endpointClient =
      new StubSyncEndpointClient(LOCAL_SERVICE_NAME);

  @After
  public void tearDown() {
    SmartCardServiceProvider.getService().unregisterPlugin(REMOTE_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterDistributedLocalService(LOCAL_SERVICE_NAME);
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_CARDRESOURCE_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_PLUGIN_NAME);
  }

  @Test
  @Override
  public void execute_transaction_with_regular_plugin() {

    // Init server
    initLocalStubPlugin();
    SmartCardServiceProvider.getService()
        .registerDistributedLocalService(
            LocalServiceServerFactoryBuilder.builder(LOCAL_SERVICE_NAME).withSyncNode().build())
        .getExtension(LocalServiceServer.class);

    // Init client
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withSyncNode(endpointClient)
                .withoutPluginObservation()
                .withReaderObservation()
                .withReaderPollingStrategy(1000)
                .build());

    // Schedule a card selection scenario
    ObservableCardReader reader =
        (ObservableCardReader)
            SmartCardServiceProvider.getService()
                .getPlugin(REMOTE_PLUGIN_NAME)
                .getReader(LOCAL_READER_NAME_1);

    ReaderApiFactory readerApiFactory = SmartCardServiceProvider.getService().getReaderApiFactory();

    CardSelectionManager cardSelectionManager = readerApiFactory.createCardSelectionManager();
    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createBasicCardSelector()
            .filterByCardProtocol("AA")
            .filterByPowerOnData("BB"),
        GenericExtensionService.getInstance().createGenericCardSelectionExtension());
    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createIsoCardSelector()
            .filterByCardProtocol("CC")
            .filterByPowerOnData("DD")
            .filterByDfName("EE")
            .setFileOccurrence(CommonIsoCardSelector.FileOccurrence.FIRST)
            .setFileControlInformation(CommonIsoCardSelector.FileControlInformation.FCI),
        CalypsoExtensionService.getInstance()
            .getCalypsoCardApiFactory()
            .createCalypsoCardSelectionExtension());
    cardSelectionManager.scheduleCardSelectionScenario(
        reader, ObservableCardReader.NotificationMode.ALWAYS);
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
                .withSyncNode()
                .withPoolPlugins(LOCAL_CARDRESOURCE_PLUGIN_NAME)
                .build())
        .getExtension(LocalServiceServer.class);

    // Init client
    remotePlugin =
        (PoolPlugin)
            SmartCardServiceProvider.getService()
                .registerPlugin(
                    RemotePoolPluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                        .withSyncNode(endpointClient)
                        .build());

    executePoolPluginScenario();
  }
}
