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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.*;
import org.calypsonet.terminal.reader.ConfigurableCardReader;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.calypsonet.terminal.reader.selection.spi.CardSelection;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.*;
import org.eclipse.keyple.core.util.HexUtil;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemotePluginServerFactoryBuilder;
import org.eclipse.keyple.distributed.integration.readerclientside.app.PluginObservationExceptionHandler;
import org.eclipse.keyple.distributed.integration.readerclientside.app.RemotePluginServerObserver;
import org.eclipse.keyple.distributed.integration.readerclientside.model.InputDataDto;
import org.eclipse.keyple.distributed.integration.readerclientside.model.OutputDataDto;
import org.eclipse.keyple.distributed.integration.util.NamedThreadFactory;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.eclipse.keyple.plugin.stub.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseScenario {

  private static final Logger logger = LoggerFactory.getLogger(BaseScenario.class);
  public static final String ISO_CARD_PROTOCOL = "ISO_14443_4_CARD";

  public static final String LOCAL_PLUGIN_NAME = StubPluginFactoryBuilder.PLUGIN_NAME;
  public static final String LOCAL_READER_NAME = "stubReader";
  public static final String LOCAL_READER_NAME_2 = "stubReader2";

  public static final String REMOTE_PLUGIN_NAME = "remotePlugin";

  public static final String SERVICE_ID_1 = "EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION";
  public static final String SERVICE_ID_2 = "CREATE_CONFIGURE_OBS_VIRTUAL_READER";
  public static final String SERVICE_ID_3 = "EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION";
  public static final String SERVICE_ID_4 = "EXECUTE_ALL_METHODS";

  public static final String DEVICE_ID = "Xo99";

  String localServiceName;
  LocalServiceClient localServiceExtension;

  Plugin localPlugin;
  StubPlugin localPluginExtension;
  ConfigurableCardReader localReader;
  StubReader localReaderExtension;
  ConfigurableCardReader localReader2;
  StubReader localReaderExtension2;

  ObservablePlugin remotePlugin;
  RemotePluginServer remotePluginExtension;

  InputDataDto user1;
  InputDataDto user2;

  ExecutorService clientPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-pool"));

  /**
   * A successful aid selection is executed locally on the terminal followed by a remoteService call
   * to launch the remote Calypso session. The Card content is sent during this first called along
   * with custom data. All this information is received by the server to select and execute the
   * corresponding ticketing scenario.
   *
   * <p>At the end of a successful calypso session, custom data is sent back to the client as a
   * final result.
   *
   * <p>This scenario can be executed on Sync node and Async node.
   */
  abstract void execute_localSelection_remoteTransaction_successful();

  /**
   * Similar to scenario 1 without the local aid selection. In this case, the server application is
   * responsible for ordering the aid selection.
   */
  abstract void execute_remoteSelection_remoteTransaction_successful();

  /** Similar to scenario 3 with two concurrent clients. */
  abstract void execute_multiClients_remoteSelection_remoteTransaction_successful();

  abstract void execute_all_methods();

  /*
   * error cases
   */

  /**
   * Client application invokes remoteService which results in a remote calypso session. Local
   * Reader throws exception in the closing operation.
   */
  abstract void execute_transaction_closeSession_card_error();

  abstract void execute_transaction_host_network_error();

  abstract void execute_transaction_client_network_error();

  public static void unregisterPlugins() {
    SmartCardServiceProvider.getService().unregisterPlugin(LOCAL_PLUGIN_NAME);
    SmartCardServiceProvider.getService().unregisterPlugin(REMOTE_PLUGIN_NAME);
  }

  /** Init local stub plugin that can work with {@link StubSmartCard} */
  void initLocalStubPlugin() {

    localPlugin =
        SmartCardServiceProvider.getService()
            .registerPlugin(
                StubPluginFactoryBuilder.builder()
                    .withStubReader(LOCAL_READER_NAME, true, null)
                    .withStubReader(LOCAL_READER_NAME_2, true, null)
                    .build());
    localPluginExtension = localPlugin.getExtension(StubPlugin.class);

    // localReader should be reset
    localReader = (ConfigurableCardReader) localPlugin.getReader(LOCAL_READER_NAME);
    localReaderExtension = localPlugin.getReaderExtension(StubReader.class, localReader.getName());
    // activate ISO_14443_4
    localReader.activateProtocol(ISO_CARD_PROTOCOL, ISO_CARD_PROTOCOL);

    // localReader 2 should be reset
    localReader2 = (ConfigurableCardReader) localPlugin.getReader(LOCAL_READER_NAME_2);
    localReaderExtension2 =
        localPlugin.getReaderExtension(StubReader.class, localReader2.getName());
    // activate ISO_14443_4
    localReader2.activateProtocol(ISO_CARD_PROTOCOL, ISO_CARD_PROTOCOL);
  }

  void clearLocalReader() {
    localPluginExtension.unplugReader(LOCAL_READER_NAME);
    localPluginExtension.unplugReader(LOCAL_READER_NAME_2);
  }

  /** Init a Sync Remote Server Plugin (ie. http server) */
  void initRemotePluginWithSyncNode() {
    remotePlugin =
        (ObservablePlugin) SmartCardServiceProvider.getService().getPlugin(REMOTE_PLUGIN_NAME);
    if (remotePlugin == null) {
      remotePlugin =
          (ObservablePlugin)
              SmartCardServiceProvider.getService()
                  .registerPlugin(
                      RemotePluginServerFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                          .withSyncNode()
                          .build());
      remotePlugin.setPluginObservationExceptionHandler(new PluginObservationExceptionHandler());
      remotePlugin.addObserver(new RemotePluginServerObserver());
      remotePluginExtension = remotePlugin.getExtension(RemotePluginServer.class);
    }
  }

  /** Init a Async Remote Server Plugin with an async server endpoint */
  void initRemotePluginWithAsyncNode(AsyncEndpointServerSpi endpointServer) {
    remotePlugin =
        (ObservablePlugin) SmartCardServiceProvider.getService().getPlugin(REMOTE_PLUGIN_NAME);
    if (remotePlugin == null) {
      remotePlugin =
          (ObservablePlugin)
              SmartCardServiceProvider.getService()
                  .registerPlugin(
                      RemotePluginServerFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                          .withAsyncNode(endpointServer)
                          .build());
      remotePlugin.setPluginObservationExceptionHandler(new PluginObservationExceptionHandler());
      remotePlugin.addObserver(new RemotePluginServerObserver());
      remotePluginExtension = remotePlugin.getExtension(RemotePluginServer.class);
    }
  }

  StubSmartCard getStubSmartCard() {
    return StubSmartCard.builder()
        .withPowerOnData(HexUtil.toByteArray("1234"))
        .withProtocol(ISO_CARD_PROTOCOL)
        .withSimulatedCommand("0000000000", "56789000") // Select app
        .withSimulatedCommand("1111111111", "ABCD9000") // Read
        .build();
  }

  StubSmartCard getBadStubSmartCard() {
    return StubSmartCard.builder()
        .withPowerOnData(HexUtil.toByteArray("1234"))
        .withProtocol(ISO_CARD_PROTOCOL)
        .withSimulatedCommand("1111111111", "ABCD9000") // Read
        .build();
  }

  /**
   * Perform a calypso PO selection
   *
   * @return matching PO
   */
  SmartCard explicitCardSelection() {

    // Get the generic card extension service
    GenericExtensionService cardExtension = GenericExtensionService.getInstance();

    // Verify that the extension's API level is consistent with the current service.
    SmartCardServiceProvider.getService().checkCardExtension(cardExtension);

    // Get the core card selection manager.
    CardSelectionManager cardSelectionManager =
        SmartCardServiceProvider.getService().createCardSelectionManager();

    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    CardSelection cardSelection = cardExtension.createCardSelection();

    // Prepare the selection by adding the created generic selection to the card selection scenario.
    cardSelectionManager.prepareSelection(cardSelection);

    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult =
        cardSelectionManager.processCardSelectionScenario(localReader);

    return selectionResult.getActiveSmartCard();
  }

  Callable<Boolean> cardRemoved(final Reader cardReader) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return !cardReader.isCardPresent();
      }
    };
  }

  Callable<Boolean> executeTransaction(
      final LocalServiceClient localServiceExtension,
      final StubReader localReaderExtension,
      final InputDataDto user) {

    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {

        // insert stub card into stub
        // localReaderExtension.insertCard(getStubSmartCard());

        // execute remote service
        OutputDataDto output =
            localServiceExtension.executeRemoteService(
                SERVICE_ID_3, LOCAL_READER_NAME, null, user, OutputDataDto.class);

        // validate result
        assertThat(output.isSuccessful()).isTrue();
        assertThat(output.getUserId()).isEqualTo(user.getUserId());
        return true;
      }
    };
  }

  void localSelection_remoteTransaction_successful() {

    // insert stub card into stub
    localReaderExtension.insertCard(getStubSmartCard());

    // execute a local selection on local reader
    SmartCard card = explicitCardSelection();

    // execute remote service fed with the card
    OutputDataDto output =
        localServiceExtension.executeRemoteService(
            SERVICE_ID_1, LOCAL_READER_NAME, card, user1, OutputDataDto.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void remoteSelection_remoteTransaction_successful() {

    // insert stub card into stub
    localReaderExtension.insertCard(getStubSmartCard());

    // execute remote service
    OutputDataDto output =
        localServiceExtension.executeRemoteService(
            SERVICE_ID_3, LOCAL_READER_NAME, null, user1, OutputDataDto.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void multiClients_remoteSelection_remoteTransaction_successful() {

    user2 = new InputDataDto().setUserId("user2");

    // insert stub card into both readers
    localReaderExtension.insertCard(getStubSmartCard());
    localReaderExtension2.insertCard(getStubSmartCard());

    // execute remote service task concurrently on both readers
    final Future<Boolean> task1 =
        clientPool.submit(executeTransaction(localServiceExtension, localReaderExtension, user1));
    final Future<Boolean> task2 =
        clientPool.submit(executeTransaction(localServiceExtension, localReaderExtension2, user2));

    // wait for termination
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                return task1.isDone() && task2.isDone() && task1.get() && task2.get();
              }
            });
  }

  void transaction_closeSession_fail() {

    StubSmartCard failingCard = getBadStubSmartCard();

    localReaderExtension.insertCard(failingCard);

    // execute remote service
    OutputDataDto output =
        localServiceExtension.executeRemoteService(
            SERVICE_ID_3, LOCAL_READER_NAME, null, user1, OutputDataDto.class);

    // validate result is false
    assertThat(output.isSuccessful()).isFalse();
    assertThat(output.getUserId()).isEqualTo(user1.getUserId());
  }

  void remoteSelection_remoteTransaction() {

    // insert stub card into stub
    localReaderExtension.insertCard(getStubSmartCard());

    // execute remote service
    OutputDataDto output =
        localServiceExtension.executeRemoteService(
            SERVICE_ID_3, LOCAL_READER_NAME, null, user1, OutputDataDto.class);
  }

  void all_methods() {

    // insert stub card into stub
    localReaderExtension.insertCard(getStubSmartCard());

    // execute remote service
    OutputDataDto output =
        localServiceExtension.executeRemoteService(
            SERVICE_ID_4, LOCAL_READER_NAME, null, user1, OutputDataDto.class);

    // validate result
    assertThat(output.isSuccessful()).isTrue();
  }
}
