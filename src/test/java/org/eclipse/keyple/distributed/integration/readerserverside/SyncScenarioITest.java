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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.ObservablePlugin;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.*;
import org.eclipse.keyple.distributed.integration.readerserverside.endpoint.StubSyncEndpointClient;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.eclipse.keypop.reader.CardReader;
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
        GenericExtensionService.getInstance()
            .getGenericCardApiFactory()
            .createGenericCardSelectionExtension());
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

  /** Registers the local service on the server, with a sync node and the local stub plugin. */
  private void initServerWithSyncNode() {
    initLocalStubPlugin();
    // The server application must define the observation exception handlers of its local plugin and
    // readers, so that the remote clients can observe them.
    ((ObservablePlugin) localPlugin).setPluginObservationExceptionHandler((pluginName, e) -> {});
    for (CardReader localReader : localPlugin.getReaders()) {
      ((ObservableCardReader) localReader)
          .setReaderObservationExceptionHandler((pluginName, readerName, e) -> {});
    }
    SmartCardServiceProvider.getService()
        .registerDistributedLocalService(
            LocalServiceServerFactoryBuilder.builder(LOCAL_SERVICE_NAME).withSyncNode().build());
  }

  @Test
  public void execute_readerObservation_withLongPolling_shouldNotifyCardEvents() {

    initServerWithSyncNode();
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withSyncNode(endpointClient)
                .withoutPluginObservation()
                .withReaderObservation()
                .withReaderLongPollingStrategy(5000)
                .build());

    executeReaderObservationScenario(0);
  }

  /**
   * The long polling duration requested by the client (60 s) exceeds the server timeout (20 s by
   * default): the server returns an empty response after its timeout and the client must keep on
   * observing the events.
   */
  @Test
  public void
      execute_readerObservation_withLongPollingLongerThanServerTimeout_shouldNotifyCardEvents() {

    initServerWithSyncNode();
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withSyncNode(endpointClient)
                .withoutPluginObservation()
                .withReaderObservation()
                .withReaderLongPollingStrategy(60000)
                .build());

    executeReaderObservationScenario(22000);
  }

  @Test
  public void execute_pluginObservation_withLongPolling_shouldNotifyReaderEvents() {

    initServerWithSyncNode();
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withSyncNode(endpointClient)
                .withPluginObservation()
                .withPluginLongPollingStrategy(5000)
                .withoutReaderObservation()
                .build());

    executePluginObservationScenario();
  }

  @Test
  public void
      execute_pluginAndReaderObservation_withLongPolling_shouldProvideObservableConnectedReaders() {

    initServerWithSyncNode();
    SmartCardServiceProvider.getService()
        .registerPlugin(
            RemotePluginClientFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                .withSyncNode(endpointClient)
                .withPluginObservation()
                .withPluginLongPollingStrategy(5000)
                .withReaderObservation()
                .withReaderLongPollingStrategy(5000)
                .build());
    assertThat(
            SmartCardServiceProvider.getService()
                .getPlugin(REMOTE_PLUGIN_NAME)
                .getReader(LOCAL_READER_NAME_1))
        .isInstanceOf(ObservableCardReader.class);

    executePluginObservationScenario();
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
