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
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.card.calypso.crypto.legacysam.LegacySamExtensionService;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.KeyplePluginException;
import org.eclipse.keyple.core.service.ObservablePlugin;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginEvent;
import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResourceProfileConfigurator;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.service.resource.PluginsConfigurator;
import org.eclipse.keyple.core.service.resource.spi.ReaderConfiguratorSpi;
import org.eclipse.keyple.core.util.HexUtil;
import org.eclipse.keyple.plugin.cardresource.CardResourcePluginFactoryBuilder;
import org.eclipse.keyple.plugin.stub.*;
import org.eclipse.keypop.calypso.crypto.legacysam.sam.LegacySam;
import org.eclipse.keypop.reader.CardReader;
import org.eclipse.keypop.reader.CardReaderEvent;
import org.eclipse.keypop.reader.ChannelControl;
import org.eclipse.keypop.reader.ObservableCardReader;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.CardSelectionManager;
import org.eclipse.keypop.reader.selection.CardSelectionResult;
import org.eclipse.keypop.reader.selection.spi.SmartCard;

public abstract class BaseScenario {

  public static final String LOCAL_PLUGIN_NAME = StubPluginFactoryBuilder.PLUGIN_NAME;
  public static final String LOCAL_READER_NAME_1 = "stubReader1";
  public static final String LOCAL_READER_NAME_2 = "stubReader2";
  public static final String LOCAL_READER_NAME_3 = "stubReader3";
  public static final String ISO_CARD_PROTOCOL = "ISO_7816_SAM";
  public static final String SAM_C1_POWER_ON_DATA = "3B3F9600805A4880C120501711223344829000";
  public static final String CARD_RESOURCE_PROFILE_NAME = "cardResourceProfile";
  public static final String LOCAL_CARDRESOURCE_PLUGIN_NAME = "cardResourcePlugin";
  public static final String LOCAL_SERVICE_NAME = "localService";

  public static final String REMOTE_PLUGIN_NAME = "remotePlugin";

  Plugin localPlugin;

  PoolPlugin remotePlugin;

  StubSmartCard getStubCard() {
    return StubSmartCard.builder()
        .withPowerOnData(HexUtil.toByteArray(SAM_C1_POWER_ON_DATA))
        .withProtocol(ISO_CARD_PROTOCOL)
        .withSimulatedCommand("8084000008", "11223344556677889000") // Get Challenge
        .build();
  }

  abstract void execute_transaction_with_regular_plugin();

  abstract void execute_transaction_with_pool_plugin();

  void initLocalStubPlugin() {
    SmartCardServiceProvider.getService()
        .checkCardExtension(LegacySamExtensionService.getInstance());
    localPlugin =
        SmartCardServiceProvider.getService()
            .registerPlugin(
                StubPluginFactoryBuilder.builder()
                    .withStubReader(LOCAL_READER_NAME_1, false, getStubCard())
                    .withStubReader(LOCAL_READER_NAME_2, false, getStubCard())
                    .build());
  }

  void initLocalCardResourceService() {
    CardResourceServiceProvider.getService()
        .getConfigurator()
        .withPlugins(
            PluginsConfigurator.builder()
                .addPlugin(
                    localPlugin,
                    new ReaderConfiguratorSpi() {
                      @Override
                      public void setupReader(CardReader cardReader) {}
                    })
                .build())
        .withCardResourceProfiles(
            CardResourceProfileConfigurator.builder(
                    CARD_RESOURCE_PROFILE_NAME,
                    LegacySamExtensionService.getInstance()
                        .createLegacySamResourceProfileExtension(
                            LegacySamExtensionService.getInstance()
                                .getLegacySamApiFactory()
                                .createLegacySamSelectionExtension()))
                .build())
        .configure();
    CardResourceServiceProvider.getService().start();
  }

  void initLocalCardResourcePlugin() {
    SmartCardServiceProvider.getService()
        .registerPlugin(
            CardResourcePluginFactoryBuilder.builder(
                    LOCAL_CARDRESOURCE_PLUGIN_NAME,
                    CardResourceServiceProvider.getService(),
                    CARD_RESOURCE_PROFILE_NAME)
                .build());
  }

  /**
   * Selects the card inserted in the local reader #1 through the remote reader of the regular
   * remote plugin, then transmits a command to it.
   */
  void executeRegularPluginScenario() {

    CardReader reader =
        SmartCardServiceProvider.getService()
            .getPlugin(REMOTE_PLUGIN_NAME)
            .getReader(LOCAL_READER_NAME_1);
    assertThat(reader).isNotNull();
    assertThat(reader.isCardPresent()).isTrue();

    // Remote card selection
    ReaderApiFactory readerApiFactory = SmartCardServiceProvider.getService().getReaderApiFactory();
    CardSelectionManager cardSelectionManager = readerApiFactory.createCardSelectionManager();
    cardSelectionManager.prepareSelection(
        readerApiFactory.createBasicCardSelector(),
        GenericExtensionService.getInstance()
            .getGenericCardApiFactory()
            .createGenericCardSelectionExtension());
    CardSelectionResult result = cardSelectionManager.processCardSelectionScenario(reader);
    SmartCard card = result.getActiveSmartCard();
    assertThat(card).isNotNull();
    assertThat(card.getPowerOnData()).isEqualTo(SAM_C1_POWER_ON_DATA);

    // Remote card transaction (Get Challenge)
    List<String> responses =
        GenericExtensionService.getInstance()
            .getGenericCardApiFactory()
            .createCardTransaction(reader, card)
            .prepareApdu("8084000008")
            .processCommands(ChannelControl.CLOSE_AFTER)
            .getResponsesAsHexStrings();
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0)).startsWith("1122334455667788");
  }

  /**
   * Observes the card events of the remote reader linked to the local reader #1: the card is
   * removed and then inserted again in the local reader, after an optional delay.
   *
   * @param delayBeforeEventsMillis The delay before the first card event (in milliseconds).
   */
  void executeReaderObservationScenario(long delayBeforeEventsMillis) {

    final ObservableCardReader reader =
        (ObservableCardReader)
            SmartCardServiceProvider.getService()
                .getPlugin(REMOTE_PLUGIN_NAME)
                .getReader(LOCAL_READER_NAME_1);
    final List<CardReaderEvent.Type> events = new CopyOnWriteArrayList<CardReaderEvent.Type>();
    reader.setReaderObservationExceptionHandler((pluginName, readerName, e) -> {});
    reader.addObserver(
        event -> {
          events.add(event.getType());
          // As done by an application, the card processing is finalized after the card insertion,
          // so that the reader then monitors the card removal.
          if (event.getType() != CardReaderEvent.Type.CARD_REMOVED) {
            reader.finalizeCardProcessing();
          }
        });
    reader.startCardDetection(ObservableCardReader.DetectionMode.REPEATING);

    try {
      // The card is initially inserted
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(() -> events.contains(CardReaderEvent.Type.CARD_INSERTED));
      if (delayBeforeEventsMillis > 0) {
        Thread.sleep(delayBeforeEventsMillis);
      }
      StubReader localReader =
          localPlugin.getReaderExtension(StubReader.class, LOCAL_READER_NAME_1);

      localReader.removeCard();
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(() -> events.contains(CardReaderEvent.Type.CARD_REMOVED));

      localReader.insertCard(getStubCard());
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(
              () ->
                  events.lastIndexOf(CardReaderEvent.Type.CARD_INSERTED)
                      > events.indexOf(CardReaderEvent.Type.CARD_REMOVED));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } finally {
      reader.stopCardDetection();
    }
  }

  /**
   * Observes the reader events of the remote plugin: a reader is plugged and then unplugged in the
   * local plugin.
   */
  void executePluginObservationScenario() {

    ObservablePlugin plugin =
        (ObservablePlugin) SmartCardServiceProvider.getService().getPlugin(REMOTE_PLUGIN_NAME);
    final List<String> events = new CopyOnWriteArrayList<String>();
    plugin.setPluginObservationExceptionHandler((pluginName, e) -> {});
    plugin.addObserver(
        event -> {
          for (String readerName : event.getReaderNames()) {
            events.add(event.getType() + ":" + readerName);
          }
        });

    // Reference to an existing remote reader, kept by the client application
    final CardReader existingReader = plugin.getReader(LOCAL_READER_NAME_1);
    assertThat(existingReader).isNotNull();
    if (existingReader instanceof ObservableCardReader) {
      ((ObservableCardReader) existingReader)
          .setReaderObservationExceptionHandler((pluginName, readerName, e) -> {});
      ((ObservableCardReader) existingReader).addObserver(event -> {});
    }
    int initialReaderCount = plugin.getReaders().size();

    StubPlugin localPluginExtension = localPlugin.getExtension(StubPlugin.class);

    localPluginExtension.plugReader(LOCAL_READER_NAME_3, false, getStubCard());
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () -> events.contains(PluginEvent.Type.READER_CONNECTED + ":" + LOCAL_READER_NAME_3));
    // The observers are notified once the remote reader is available, built as the initial ones
    CardReader connectedReader = plugin.getReader(LOCAL_READER_NAME_3);
    assertThat(connectedReader).isNotNull();
    assertThat(connectedReader.getClass()).isEqualTo(existingReader.getClass());
    assertThat(connectedReader.isCardPresent()).isTrue();
    assertThat(plugin.getReaders()).hasSize(initialReaderCount + 1);
    // The existing reader is unchanged and still usable
    checkExistingReaderIsUnchanged(plugin, existingReader);

    localPluginExtension.unplugReader(LOCAL_READER_NAME_3);
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(
            () ->
                events.contains(PluginEvent.Type.READER_DISCONNECTED + ":" + LOCAL_READER_NAME_3));
    assertThat(plugin.getReader(LOCAL_READER_NAME_3)).isNull();
    assertThat(plugin.getReaders()).hasSize(initialReaderCount);
    // The existing reader is unchanged and still usable
    checkExistingReaderIsUnchanged(plugin, existingReader);
  }

  /**
   * Checks that the remote reader kept by the client application is still the one provided by the
   * plugin, with its observer, and that it is still usable.
   */
  private static void checkExistingReaderIsUnchanged(Plugin plugin, CardReader existingReader) {
    assertThat(plugin.getReader(LOCAL_READER_NAME_1)).isSameAs(existingReader);
    assertThat(existingReader.isCardPresent()).isTrue();
    if (existingReader instanceof ObservableCardReader) {
      assertThat(((ObservableCardReader) existingReader).countObservers()).isEqualTo(1);
    }
  }

  void executePoolPluginScenario() {

    // Get card resource #1
    CardReader r1 = remotePlugin.allocateReader(CARD_RESOURCE_PROFILE_NAME);
    assertThat(r1).isNotNull();
    String r1Name = r1.getName();
    assertThat(r1Name).isNotEmpty();

    LegacySam sam1 = (LegacySam) remotePlugin.getSelectedSmartCard(r1);
    assertThat(sam1).isNotNull();
    assertThat(sam1.getPowerOnData()).isEqualTo(SAM_C1_POWER_ON_DATA);

    // Get card resource #2
    CardReader r2 = remotePlugin.allocateReader(CARD_RESOURCE_PROFILE_NAME);
    assertThat(r2).isNotNull();
    String r2Name = r2.getName();
    assertThat(r2Name).isNotEqualTo(r1Name);

    LegacySam sam2 = (LegacySam) remotePlugin.getSelectedSmartCard(r2);
    assertThat(sam2).isNotNull();
    assertThat(sam2.getPowerOnData()).isEqualTo(SAM_C1_POWER_ON_DATA);

    // Get card resource #3
    try {
      // No resource available
      remotePlugin.allocateReader(CARD_RESOURCE_PROFILE_NAME);
      shouldHaveThrown(KeyplePluginException.class);
    } catch (KeyplePluginException ignored) {
    }

    remotePlugin.releaseReader(r1);

    CardReader r3 = remotePlugin.allocateReader(CARD_RESOURCE_PROFILE_NAME);
    assertThat(r3).isNotNull();
    String r3Name = r3.getName();
    assertThat(r3Name).isEqualTo(r1Name);

    LegacySam sam3 = (LegacySam) remotePlugin.getSelectedSmartCard(r3);
    assertThat(sam3).isNotNull();
    assertThat(sam3.getPowerOnData()).isEqualTo(SAM_C1_POWER_ON_DATA);

    remotePlugin.releaseReader(r2);
    remotePlugin.releaseReader(r3);
  }
}
