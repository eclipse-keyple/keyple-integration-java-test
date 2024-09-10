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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;

import org.eclipse.keyple.card.calypso.crypto.legacysam.LegacySamExtensionService;
import org.eclipse.keyple.core.service.KeyplePluginException;
import org.eclipse.keyple.core.service.Plugin;
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

public abstract class BaseScenario {

  public static final String LOCAL_PLUGIN_NAME = StubPluginFactoryBuilder.PLUGIN_NAME;
  public static final String LOCAL_READER_NAME_1 = "stubReader1";
  public static final String LOCAL_READER_NAME_2 = "stubReader2";
  public static final String ISO_CARD_PROTOCOL = "ISO_7816_SAM";
  public static final String SAM_C1_POWER_ON_DATA = "3B3F9600805A4880C120501711223344829000";
  public static final String CARD_RESOURCE_PROFILE_NAME = "cardResourceProfile";
  public static final String LOCAL_CARDRESOURCE_PLUGIN_NAME = "cardResourcePlugin";
  public static final String LOCAL_SERVICE_NAME = "localService";

  public static final String REMOTE_PLUGIN_NAME = "remotePlugin";

  Plugin localPlugin;

  PoolPlugin remotePlugin;

  private StubSmartCard getStubCard() {
    return StubSmartCard.builder()
        .withPowerOnData(HexUtil.toByteArray(SAM_C1_POWER_ON_DATA))
        .withProtocol(ISO_CARD_PROTOCOL)
        .withSimulatedCommand("8084000008", "11223344556677889000") // Get Challenge
        .build();
  }

  abstract void execute_transaction_with_regular_plugin();

  abstract void execute_transaction_with_pool_plugin();

  void initLocalStubPlugin() {
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
