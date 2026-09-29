/* **************************************************************************************
 * Copyright (c) 2026 Calypso Networks Association https://calypsonet.org/
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
package org.eclipse.keyple.core.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.CardSelectionManager;
import org.junit.Test;

/**
 * Checks that the card extensions provided by the Eclipse Keyple project can be used in an imported
 * card selection scenario without having been registered with {@code checkCardExtension(...)}, as
 * in the applications developed before this method was needed.
 *
 * <p>This class runs in its own JVM (see {@code forkEvery} in the build), so that no card extension
 * has been registered by another test.
 */
public class CardExtensionWithoutRegistrationTest {

  @Test
  public void
      importCardSelectionScenario_whenCardExtensionsAreNotRegistered_shouldRebuildSelections() {

    ReaderApiFactory readerApiFactory = SmartCardServiceProvider.getService().getReaderApiFactory();

    CardSelectionManager manager = readerApiFactory.createCardSelectionManager();
    manager.prepareSelection(
        readerApiFactory.createIsoCardSelector().filterByDfName("1122334455"),
        CalypsoExtensionService.getInstance()
            .getCalypsoCardApiFactory()
            .createCalypsoCardSelectionExtension());
    manager.prepareSelection(
        readerApiFactory.createIsoCardSelector().filterByDfName("AABBCCDDEE"),
        GenericExtensionService.getInstance()
            .getGenericCardApiFactory()
            .createGenericCardSelectionExtension());
    String export1 = manager.exportCardSelectionScenario();

    CardSelectionManager manager2 = readerApiFactory.createCardSelectionManager();
    int index = manager2.importCardSelectionScenario(export1);
    String export2 = manager2.exportCardSelectionScenario();

    assertThat(index).isEqualTo(1);
    // The card selections are rebuilt with their original types (not replaced by a default type)
    assertThat(JsonUtil.getParser().fromJson(export2, JsonObject.class))
        .isEqualTo(JsonUtil.getParser().fromJson(export1, JsonObject.class));
  }
}
