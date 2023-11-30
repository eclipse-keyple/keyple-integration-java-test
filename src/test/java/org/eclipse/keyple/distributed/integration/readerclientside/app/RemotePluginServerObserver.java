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
package org.eclipse.keyple.distributed.integration.readerclientside.app;

import java.util.List;
import org.eclipse.keyple.card.generic.ChannelControl;
import org.eclipse.keyple.card.generic.GenericCardSelectionExtension;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginEvent;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.spi.PluginObserverSpi;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.eclipse.keyple.distributed.integration.readerclientside.BaseScenario;
import org.eclipse.keyple.distributed.integration.readerclientside.endpoint.StubNetworkConnectionException;
import org.eclipse.keyple.distributed.integration.readerclientside.model.InputDataDto;
import org.eclipse.keyple.distributed.integration.readerclientside.model.OutputDataDto;
import org.eclipse.keypop.reader.CardReader;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.CardSelectionManager;
import org.eclipse.keypop.reader.selection.CardSelectionResult;
import org.eclipse.keypop.reader.selection.CardSelector;
import org.eclipse.keypop.reader.selection.IsoCardSelector;
import org.eclipse.keypop.reader.selection.spi.SmartCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotePluginServerObserver implements PluginObserverSpi {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginServerObserver.class);
  private static final ReaderApiFactory readerApiFactory =
      SmartCardServiceProvider.getService().getReaderApiFactory();

  public RemotePluginServerObserver() {}

  @Override
  public void onPluginEvent(PluginEvent event) {

    switch (event.getType()) {
      case READER_CONNECTED:
        // retrieve serviceId from reader
        String remoteReaderName = event.getReaderNames().first();

        Plugin plugin = SmartCardServiceProvider.getService().getPlugin(event.getPluginName());
        RemotePluginServer pluginExtension = plugin.getExtension(RemotePluginServer.class);

        CardReader reader = plugin.getReader(remoteReaderName);

        // execute the business logic based on serviceId
        Object output = executeService(plugin, reader);

        // end service
        pluginExtension.endRemoteService(remoteReaderName, output);

        break;
    }
  }

  Object executeService(Plugin plugin, CardReader reader) {

    RemoteReaderServer readerExtension =
        plugin.getReaderExtension(RemoteReaderServer.class, reader.getName());

    // EXECUTE_CALYPSO_SESSION_FROM_LOCAL_SELECTION
    if (BaseScenario.SERVICE_ID_1.equals(readerExtension.getServiceId())) {
      SmartCard card = (SmartCard) readerExtension.getInitialCardContent();
      InputDataDto inputDataDto = readerExtension.getInputData(InputDataDto.class);
      try {
        // execute a transaction
        List<String> results =
            GenericExtensionService.getInstance()
                .createCardTransaction(reader, card)
                .prepareApdu("0000000000")
                .processApdusToHexStrings(ChannelControl.CLOSE_AFTER);
        return new OutputDataDto().setUserId(inputDataDto.getUserId()).setSuccessful(true);
      } catch (RuntimeException e) {
        if (e instanceof StubNetworkConnectionException) {
          throw e;
        }
        logger.error(e.getMessage(), e);
        return new OutputDataDto().setSuccessful(false).setUserId(inputDataDto.getUserId());
      }
    }

    // EXECUTE_CALYPSO_SESSION_FROM_REMOTE_SELECTION
    if (BaseScenario.SERVICE_ID_3.equals(readerExtension.getServiceId())) {
      InputDataDto inputDataDto = readerExtension.getInputData(InputDataDto.class);
      // remote selection
      SmartCard card = explicitCardSelection(reader);
      try {
        // execute a transaction
        List<String> results =
            GenericExtensionService.getInstance()
                .createCardTransaction(reader, card)
                .prepareApdu("0000000000")
                .processApdusToHexStrings(ChannelControl.CLOSE_AFTER);
        return new OutputDataDto().setUserId(inputDataDto.getUserId()).setSuccessful(true);
      } catch (RuntimeException e) {
        if (e instanceof StubNetworkConnectionException) {
          throw e;
        }
        logger.error(e.getMessage(), e);
        return new OutputDataDto().setSuccessful(false).setUserId(inputDataDto.getUserId());
      }
    }

    // EXECUTE ALL METHODS
    if (BaseScenario.SERVICE_ID_4.equals(readerExtension.getServiceId())) {
      InputDataDto inputDataDto = readerExtension.getInputData(InputDataDto.class);
      if (!reader.isCardPresent()) {
        throw new IllegalStateException("Card should be inserted");
      }
      if (!reader.isContactless()) {
        throw new IllegalStateException("Reader should be contactless");
      }
      return new OutputDataDto().setUserId(inputDataDto.getUserId()).setSuccessful(true);
    }

    throw new IllegalArgumentException("Service Id not recognized");
  }

  /**
   * Perform a calypso PO selection
   *
   * @return matching PO
   */
  SmartCard explicitCardSelection(CardReader reader) {

    // Get the generic card extension service
    GenericExtensionService cardExtension = GenericExtensionService.getInstance();

    // Verify that the extension's API level is consistent with the current service.
    SmartCardServiceProvider.getService().checkCardExtension(cardExtension);

    // Get the core card selection manager.
    CardSelectionManager cardSelectionManager = readerApiFactory.createCardSelectionManager();

    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    CardSelector<IsoCardSelector> cardSelector = readerApiFactory.createIsoCardSelector();

    // Prepare the selection by adding the created generic selection to the card selection scenario.
    GenericCardSelectionExtension genericCardSelectionExtension =
        GenericExtensionService.getInstance().createGenericCardSelectionExtension();
    cardSelectionManager.prepareSelection(cardSelector, genericCardSelectionExtension);

    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult = cardSelectionManager.processCardSelectionScenario(reader);

    return selectionResult.getActiveSmartCard();
  }
}
