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

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol;
import org.eclipse.keyple.distributed.LocalServiceServer;
import org.eclipse.keyple.distributed.RemotePluginClient;
import org.eclipse.keyple.plugin.stub.*;

public abstract class BaseScenario {

  public static final String LOCAL_PLUGIN_NAME = StubPluginFactoryBuilder.PLUGIN_NAME;
  public static final String LOCAL_READER_NAME = "stubReader";
  public static final String LOCAL_READER_NAME_2 = "stubReader2";

  public static String REMOTE_PLUGIN_NAME = "remotePlugin";

  protected String localServiceName;
  protected LocalServiceServer localServiceExtension;

  Plugin localPlugin;
  StubPlugin localPluginExtension;
  Reader localReader;
  StubReader localReaderExtension;
  Reader localReader2;
  StubReader localReaderExtension2;

  protected RemotePluginClient remotePluginClient;

  abstract void execute_transaction_on_pool_reader();

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
    localReader = localPlugin.getReader(LOCAL_READER_NAME);
    localReaderExtension = (StubReader) localReader.getExtension(StubReader.class);
    // activate ISO_14443_4
    localReader.activateProtocol(
        ContactlessCardCommonProtocol.ISO_14443_4.name(),
        ContactlessCardCommonProtocol.ISO_14443_4.name());

    // localReader 2 should be reset
    localReader2 = localPlugin.getReader(LOCAL_READER_NAME_2);
    localReaderExtension2 = (StubReader) localReader2.getExtension(StubReader.class);
    // activate ISO_14443_4
    localReader2.activateProtocol(
        ContactlessCardCommonProtocol.ISO_14443_4.name(),
        ContactlessCardCommonProtocol.ISO_14443_4.name());
  }
}
