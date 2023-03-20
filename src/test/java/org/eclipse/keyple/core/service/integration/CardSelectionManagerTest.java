/* **************************************************************************************
 * Copyright (c) 2022 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.service.integration;

import static org.assertj.core.api.Assertions.*;

import com.google.gson.JsonObject;
import org.calypsonet.terminal.calypso.WriteAccessLevel;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.spi.CardSelection;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.generic.GenericCardSelection;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CardSelectionManagerTest {

  private static final String AID = "1122334455";
  private static final byte SFI = 1;
  private static final int RECORD = 2;
  private static final String CALYPSO_CARD_PROTOCOL = "CALYPSO_CARD_PROTOCOL";
  private static final String GENERIC_CARD_PROTOCOL = "GENERIC_CARD_PROTOCOL";
  private static final String POWER_ON_DATA_REGEX = ".*";
  private static final String EXPORT_JSON =
      "{"
          + "   \"multiSelectionProcessing\":\"FIRST_MATCH\","
          + "   \"channelControl\":\"KEEP_OPEN\","
          + "   \"cardSelectionsTypes\":["
          + "      \"org.eclipse.keyple.card.calypso.CalypsoCardSelectionAdapter\","
          + "      \"org.eclipse.keyple.card.generic.GenericCardSelectionAdapter\""
          + "   ],"
          + "   \"cardSelections\":["
          + "      {"
          + "         \"commands\":["
          + "            {"
          + "               \"type\":\"org.eclipse.keyple.card.calypso.CmdCardReadRecords\","
          + "               \"data\":{"
          + "                  \"sfi\":\"01\","
          + "                  \"firstRecordNumber\":\"02\","
          + "                  \"recordSize\":\"00\","
          + "                  \"readMode\":\"ONE_RECORD\","
          + "                  \"commandRef\":\"READ_RECORDS\","
          + "                  \"le\":\"00\","
          + "                  \"apduRequest\":{"
          + "                     \"apdu\":\"00B2020C00\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Records - SFI: 1h, REC: 2, READMODE: ONE_RECORD, EXPECTEDLENGTH: 0\""
          + "                  }"
          + "               }"
          + "            },"
          + "            {"
          + "               \"type\":\"org.eclipse.keyple.card.calypso.CmdCardReadBinary\","
          + "               \"data\":{"
          + "                  \"sfi\":\"01\","
          + "                  \"offset\":\"00\","
          + "                  \"commandRef\":\"READ_BINARY\","
          + "                  \"le\":\"0A\","
          + "                  \"apduRequest\":{"
          + "                     \"apdu\":\"00B081000A\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Binary - SFI:01h, OFFSET:0, LENGTH:10\""
          + "                  }"
          + "               }"
          + "            },"
          + "            {"
          + "               \"type\":\"org.eclipse.keyple.card.calypso.CmdCardReadRecords\","
          + "               \"data\":{"
          + "                  \"sfi\":\"01\","
          + "                  \"firstRecordNumber\":\"01\","
          + "                  \"recordSize\":\"00\","
          + "                  \"readMode\":\"ONE_RECORD\","
          + "                  \"commandRef\":\"READ_RECORDS\","
          + "                  \"le\":\"03\","
          + "                  \"apduRequest\":{"
          + "                     \"apdu\":\"00B2010C03\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Records - SFI: 1h, REC: 1, READMODE: ONE_RECORD, EXPECTEDLENGTH: 3\""
          + "                  }"
          + "               }"
          + "            },"
          + "            {"
          + "               \"type\":\"org.eclipse.keyple.card.calypso.CmdCardOpenSecureSession\","
          + "               \"data\":{"
          + "                  \"writeAccessLevel\":\"DEBIT\","
          + "                  \"isExtendedModeAllowed\":true,"
          + "                  \"sfi\":\"00\","
          + "                  \"recordNumber\":\"00\","
          + "                  \"commandRef\":\"OPEN_SECURE_SESSION\","
          + "                  \"le\":\"00\","
          + "                  \"apduRequest\":{"
          + "                     \"apdu\":\"008A0302010000\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Open Secure Session - KEYINDEX:3, SFI:00h, REC:0 - PREOPEN\""
          + "                  }"
          + "               }"
          + "            }"
          + "         ],"
          + "         \"cardSelector\":{"
          + "            \"cardProtocol\":\"CALYPSO_CARD_PROTOCOL\","
          + "            \"powerOnDataRegex\":\".*\","
          + "            \"aid\":\"1122334455\","
          + "            \"fileOccurrence\":\"FIRST\","
          + "            \"fileControlInformation\":\"FCI\","
          + "            \"successfulSelectionStatusWords\":["
          + "               \"9000\","
          + "               \"6283\""
          + "            ]"
          + "         }"
          + "      },"
          + "      {"
          + "         \"cardSelector\":{"
          + "            \"cardProtocol\":\"GENERIC_CARD_PROTOCOL\","
          + "            \"powerOnDataRegex\":\".*\","
          + "            \"aid\":\"1122334455\","
          + "            \"fileOccurrence\":\"FIRST\","
          + "            \"fileControlInformation\":\"FCI\","
          + "            \"successfulSelectionStatusWords\":["
          + "               \"9000\""
          + "            ]"
          + "         }"
          + "      }"
          + "   ],"
          + "   \"defaultCardSelections\":["
          + "      {"
          + "         \"cardSelectionRequest\":{"
          + "            \"cardSelector\":{"
          + "               \"cardProtocol\":\"CALYPSO_CARD_PROTOCOL\","
          + "               \"powerOnDataRegex\":\".*\","
          + "               \"aid\":\"1122334455\","
          + "               \"fileOccurrence\":\"FIRST\","
          + "               \"fileControlInformation\":\"FCI\","
          + "               \"successfulSelectionStatusWords\":["
          + "                  \"9000\","
          + "                  \"6283\""
          + "               ]"
          + "            },"
          + "            \"cardRequest\":{"
          + "               \"apduRequests\":["
          + "                  {"
          + "                     \"apdu\":\"00B2020C00\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Records - SFI: 1h, REC: 2, READMODE: ONE_RECORD, EXPECTEDLENGTH: 0\""
          + "                  },"
          + "                  {"
          + "                     \"apdu\":\"00B081000A\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Binary - SFI:01h, OFFSET:0, LENGTH:10\""
          + "                  },"
          + "                  {"
          + "                     \"apdu\":\"00B2010C03\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Read Records - SFI: 1h, REC: 1, READMODE: ONE_RECORD, EXPECTEDLENGTH: 3\""
          + "                  },"
          + "                  {"
          + "                     \"apdu\":\"008A0302010000\","
          + "                     \"successfulStatusWords\":["
          + "                        \"9000\""
          + "                     ],"
          + "                     \"info\":\"Open Secure Session - KEYINDEX:3, SFI:00h, REC:0 - PREOPEN\""
          + "                  }"
          + "               ],"
          + "               \"stopOnUnsuccessfulStatusWord\":false"
          + "            }"
          + "         }"
          + "      },"
          + "      {"
          + "         \"cardSelectionRequest\":{"
          + "            \"cardSelector\":{"
          + "               \"cardProtocol\":\"GENERIC_CARD_PROTOCOL\","
          + "               \"powerOnDataRegex\":\".*\","
          + "               \"aid\":\"1122334455\","
          + "               \"fileOccurrence\":\"FIRST\","
          + "               \"fileControlInformation\":\"FCI\","
          + "               \"successfulSelectionStatusWords\":["
          + "                  \"9000\""
          + "               ]"
          + "            }"
          + "         }"
          + "      }"
          + "   ]"
          + "}";
  private static JsonObject expectedJson;

  private CardSelectionManager manager;

  @BeforeClass
  public static void beforeClass() {
    expectedJson = JsonUtil.getParser().fromJson(EXPORT_JSON, JsonObject.class);
  }

  @Before
  public void setUp() {
    CardSelection calypsoCardSelection =
        CalypsoExtensionService.getInstance()
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByCardProtocol(CALYPSO_CARD_PROTOCOL)
            .filterByPowerOnData(POWER_ON_DATA_REGEX)
            .filterByDfName(AID)
            .setFileControlInformation(CalypsoCardSelection.FileControlInformation.FCI)
            .setFileOccurrence(CalypsoCardSelection.FileOccurrence.FIRST)
            .prepareReadRecord(SFI, RECORD)
            .prepareReadBinary(SFI, 0, 10)
            .prepareReadCounter(SFI, 1)
            .preparePreOpenSecureSession(WriteAccessLevel.DEBIT);
    CardSelection genericCardSelection =
        GenericExtensionService.getInstance()
            .createCardSelection()
            .filterByCardProtocol(GENERIC_CARD_PROTOCOL)
            .filterByPowerOnData(POWER_ON_DATA_REGEX)
            .filterByDfName(AID)
            .setFileControlInformation(GenericCardSelection.FileControlInformation.FCI)
            .setFileOccurrence(GenericCardSelection.FileOccurrence.FIRST);
    manager = SmartCardServiceProvider.getService().createCardSelectionManager();
    manager.prepareSelection(calypsoCardSelection);
    manager.prepareSelection(genericCardSelection);
  }

  @Test
  public void exportImportCardSelectionScenario() {
    // Export 1
    String export1 = manager.exportCardSelectionScenario();

    JsonObject resultJson1 = JsonUtil.getParser().fromJson(export1, JsonObject.class);
    assertThat(resultJson1).isEqualTo(expectedJson);

    // Import
    CardSelectionManager manager2 =
        SmartCardServiceProvider.getService().createCardSelectionManager();
    int index = manager2.importCardSelectionScenario(export1);
    assertThat(index).isEqualTo(1);

    // Export 2
    String export2 = manager2.exportCardSelectionScenario();

    JsonObject resultJson2 = JsonUtil.getParser().fromJson(export2, JsonObject.class);
    assertThat(resultJson2).isEqualTo(expectedJson);
  }
}
