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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.generic.GenericCardSelectionExtension;
import org.eclipse.keyple.card.generic.GenericExtensionService;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.HexUtil;
import org.eclipse.keyple.core.util.json.JsonUtil;
import org.eclipse.keyple.plugin.stub.StubPluginFactoryBuilder;
import org.eclipse.keyple.plugin.stub.StubSmartCard;
import org.eclipse.keypop.calypso.card.WriteAccessLevel;
import org.eclipse.keypop.calypso.card.card.CalypsoCardSelectionExtension;
import org.eclipse.keypop.reader.ConfigurableCardReader;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CardSelectionManagerTest {

  private static final String POWER_ON_DATA = "1234";
  private static final String AID1 = "1122334455";
  private static final String AID2 = "AABBCCDDEE";
  private static final byte SFI = 1;
  private static final int RECORD = 2;
  private static final String CALYPSO_CARD_PROTOCOL = "CALYPSO_CARD_PROTOCOL";
  private static final String GENERIC_CARD_PROTOCOL = "GENERIC_CARD_PROTOCOL";
  private static final String POWER_ON_DATA_REGEX = ".*";
  private static final String PLUGIN_NAME = "StubPlugin";
  private static final String READER_NAME = "stubReader";
  private static final String ISO_CARD_PROTOCOL = "ISO_14443_4_CARD";
  private static final String SELECT_APPLICATION1_CMD = "00A4040005" + AID1 + "00";
  private static final String SELECT_APPLICATION1_RSP = "6400";
  private static final String SELECT_APPLICATION2_CMD = "00A4040005" + AID2 + "00";
  private static final String SELECT_APPLICATION2_RSP =
      "6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C20051410019000";
  private static final String READ_CMD = "00B2020C00";
  private static final String READ_RSP = "ABCD9000";
  private static final String EXPORT_JSON_SCENARIO =
      "{"
          + "    \"multiSelectionProcessing\": \"FIRST_MATCH\","
          + "    \"channelControl\": \"KEEP_OPEN\","
          + "    \"cardSelectorsTypes\":"
          + "    ["
          + "        \"org.eclipse.keyple.core.service.IsoCardSelectorAdapter\","
          + "        \"org.eclipse.keyple.core.service.IsoCardSelectorAdapter\""
          + "    ],"
          + "    \"cardSelectors\":"
          + "    ["
          + "        {"
          + "            \"logicalProtocolName\": \"CALYPSO_CARD_PROTOCOL\","
          + "            \"powerOnDataRegex\": \".*\","
          + "            \"aid\": \"1122334455\","
          + "            \"fileOccurrence\": \"FIRST\","
          + "            \"fileControlInformation\": \"FCI\""
          + "        },"
          + "        {"
          + "            \"logicalProtocolName\": \"GENERIC_CARD_PROTOCOL\","
          + "            \"powerOnDataRegex\": \".*\","
          + "            \"aid\": \"1122334455\","
          + "            \"fileOccurrence\": \"FIRST\","
          + "            \"fileControlInformation\": \"FCI\""
          + "        }"
          + "    ],"
          + "    \"cardSelectionsTypes\":"
          + "    ["
          + "        \"org.eclipse.keyple.card.calypso.CalypsoCardSelectionExtensionAdapter\","
          + "        \"org.eclipse.keyple.card.generic.GenericCardSelectionExtensionAdapter\""
          + "    ],"
          + "    \"cardSelections\":"
          + "    ["
          + "        {"
          + "            \"commands\":"
          + "            ["
          + "                {"
          + "                    \"type\": \"org.eclipse.keyple.card.calypso.CommandReadRecords\","
          + "                    \"data\":"
          + "                    {"
          + "                        \"sfi\": \"01\","
          + "                        \"firstRecordNumber\": \"02\","
          + "                        \"recordSize\": \"00\","
          + "                        \"readMode\": \"ONE_RECORD\","
          + "                        \"commandRef\": \"READ_RECORDS\","
          + "                        \"commandContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false,"
          + "                            \"isEncryptionActive\": false"
          + "                        },"
          + "                        \"transactionContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false"
          + "                        },"
          + "                        \"le\": \"00\","
          + "                        \"apduRequest\":"
          + "                        {"
          + "                            \"apdu\": \"00B2020C00\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Records - SFI: 1h, REC: 2, READMODE: ONE_RECORD, EXPECTEDLENGTH: 0\""
          + "                        }"
          + "                    }"
          + "                },"
          + "                {"
          + "                    \"type\": \"org.eclipse.keyple.card.calypso.CommandReadBinary\","
          + "                    \"data\":"
          + "                    {"
          + "                        \"sfi\": \"01\","
          + "                        \"offset\": \"00\","
          + "                        \"commandRef\": \"READ_BINARY\","
          + "                        \"commandContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false,"
          + "                            \"isEncryptionActive\": false"
          + "                        },"
          + "                        \"transactionContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false"
          + "                        },"
          + "                        \"le\": \"0A\","
          + "                        \"apduRequest\":"
          + "                        {"
          + "                            \"apdu\": \"00B081000A\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Binary - SFI:01h, OFFSET:0, LENGTH:10\""
          + "                        }"
          + "                    }"
          + "                },"
          + "                {"
          + "                    \"type\": \"org.eclipse.keyple.card.calypso.CommandReadRecords\","
          + "                    \"data\":"
          + "                    {"
          + "                        \"sfi\": \"01\","
          + "                        \"firstRecordNumber\": \"01\","
          + "                        \"recordSize\": \"00\","
          + "                        \"readMode\": \"ONE_RECORD\","
          + "                        \"commandRef\": \"READ_RECORDS\","
          + "                        \"commandContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false,"
          + "                            \"isEncryptionActive\": false"
          + "                        },"
          + "                        \"transactionContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false"
          + "                        },"
          + "                        \"le\": \"03\","
          + "                        \"apduRequest\":"
          + "                        {"
          + "                            \"apdu\": \"00B2010C03\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Records - SFI: 1h, REC: 1, READMODE: ONE_RECORD, EXPECTEDLENGTH: 3\""
          + "                        }"
          + "                    }"
          + "                },"
          + "                {"
          + "                    \"type\": \"org.eclipse.keyple.card.calypso.CommandOpenSecureSession\","
          + "                    \"data\":"
          + "                    {"
          + "                        \"writeAccessLevel\": \"DEBIT\","
          + "                        \"isExtendedModeAllowed\": true,"
          + "                        \"sfi\": \"00\","
          + "                        \"recordNumber\": \"00\","
          + "                        \"commandRef\": \"OPEN_SECURE_SESSION\","
          + "                        \"commandContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false,"
          + "                            \"isEncryptionActive\": false"
          + "                        },"
          + "                        \"transactionContext\":"
          + "                        {"
          + "                            \"isSecureSessionOpen\": false"
          + "                        },"
          + "                        \"le\": \"00\","
          + "                        \"apduRequest\":"
          + "                        {"
          + "                            \"apdu\": \"008A0302010000\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Open Secure Session - KEYINDEX:3, SFI:00h, REC:0 - PREOPEN\""
          + "                        }"
          + "                    }"
          + "                }"
          + "            ],"
          + "            \"transactionContext\":"
          + "            {"
          + "                \"isSecureSessionOpen\": false"
          + "            },"
          + "            \"commandContext\":"
          + "            {"
          + "                \"isSecureSessionOpen\": false,"
          + "                \"isEncryptionActive\": false"
          + "            },"
          + "            \"isPreOpenPrepared\": true,"
          + "            \"isInvalidatedCardAccepted\": true"
          + "        },"
          + "        {"
          + "            \"successfulSelectionStatusWords\":"
          + "            ["
          + "                \"9000\""
          + "            ]"
          + "        }"
          + "    ],"
          + "    \"defaultCardSelections\":"
          + "    ["
          + "        {"
          + "            \"cardSelectionRequest\":"
          + "            {"
          + "                \"cardRequest\":"
          + "                {"
          + "                    \"apduRequests\":"
          + "                    ["
          + "                        {"
          + "                            \"apdu\": \"00B2020C00\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Records - SFI: 1h, REC: 2, READMODE: ONE_RECORD, EXPECTEDLENGTH: 0\""
          + "                        },"
          + "                        {"
          + "                            \"apdu\": \"00B081000A\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Binary - SFI:01h, OFFSET:0, LENGTH:10\""
          + "                        },"
          + "                        {"
          + "                            \"apdu\": \"00B2010C03\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Read Records - SFI: 1h, REC: 1, READMODE: ONE_RECORD, EXPECTEDLENGTH: 3\""
          + "                        },"
          + "                        {"
          + "                            \"apdu\": \"008A0302010000\","
          + "                            \"successfulStatusWords\":"
          + "                            ["
          + "                                \"9000\""
          + "                            ],"
          + "                            \"info\": \"Open Secure Session - KEYINDEX:3, SFI:00h, REC:0 - PREOPEN\""
          + "                        }"
          + "                    ],"
          + "                    \"stopOnUnsuccessfulStatusWord\": false"
          + "                },"
          + "                \"successfulSelectionStatusWords\":"
          + "                ["
          + "                    \"9000\","
          + "                    \"6283\""
          + "                ]"
          + "            }"
          + "        },"
          + "        {"
          + "            \"cardSelectionRequest\":"
          + "            {"
          + "                \"successfulSelectionStatusWords\":"
          + "                ["
          + "                    \"9000\""
          + "                ]"
          + "            }"
          + "        }"
          + "    ]"
          + "}";
  private static JsonObject expectedJsonScenario;
  private static final String EXPORT_JSON_PROCESSED_SCENARIO =
      "["
          + "   {"
          + "      \"hasMatched\":false,"
          + "      \"powerOnData\":\"1234\","
          + "      \"selectApplicationResponse\":{"
          + "         \"apdu\":\"6400\","
          + "         \"statusWord\":\"6400\""
          + "      },"
          + "      \"cardResponse\":{"
          + "         \"apduResponses\":["
          + "         ],"
          + "         \"isLogicalChannelOpen\":false"
          + "      }"
          + "   },"
          + "   {"
          + "      \"hasMatched\":true,"
          + "      \"powerOnData\":\"1234\","
          + "      \"selectApplicationResponse\":{"
          + "         \"apdu\":\"6F238409315449432E49434131A516BF0C13C708000000001122334453070A3C20051410019000\","
          + "         \"statusWord\":\"9000\""
          + "      },"
          + "      \"cardResponse\":{"
          + "         \"apduResponses\":["
          + "            {"
          + "               \"apdu\":\"ABCD9000\","
          + "               \"statusWord\":\"9000\""
          + "            }"
          + "         ],"
          + "         \"isLogicalChannelOpen\":true"
          + "      }"
          + "   }"
          + "]";
  private static JsonArray expectedJsonProcessedScenario;
  private static ReaderApiFactory readerApiFactory;

  ConfigurableCardReader reader;

  @BeforeClass
  public static void beforeClass() {
    readerApiFactory = SmartCardServiceProvider.getService().getReaderApiFactory();
    expectedJsonScenario = JsonUtil.getParser().fromJson(EXPORT_JSON_SCENARIO, JsonObject.class);
    expectedJsonProcessedScenario =
        JsonUtil.getParser().fromJson(EXPORT_JSON_PROCESSED_SCENARIO, JsonArray.class);
  }

  @AfterClass
  public static void afterClass() {
    SmartCardServiceProvider.getService().unregisterPlugin(PLUGIN_NAME);
  }

  private CardSelectionManager initManagerForExportImportScenario() {
    CardSelectionManager manager = readerApiFactory.createCardSelectionManager();

    CardSelector<IsoCardSelector> cardSelector =
        readerApiFactory
            .createIsoCardSelector()
            .filterByCardProtocol(CALYPSO_CARD_PROTOCOL)
            .filterByPowerOnData(POWER_ON_DATA_REGEX)
            .filterByDfName(AID1)
            .setFileControlInformation(CommonIsoCardSelector.FileControlInformation.FCI)
            .setFileOccurrence(CommonIsoCardSelector.FileOccurrence.FIRST);
    CalypsoCardSelectionExtension calypsoCardSelectionExtension =
        CalypsoExtensionService.getInstance()
            .getCalypsoCardApiFactory()
            .createCalypsoCardSelectionExtension()
            .acceptInvalidatedCard()
            .prepareReadRecord(SFI, RECORD)
            .prepareReadBinary(SFI, 0, 10)
            .prepareReadCounter(SFI, 1)
            .preparePreOpenSecureSession(WriteAccessLevel.DEBIT);
    manager.prepareSelection(cardSelector, calypsoCardSelectionExtension);

    cardSelector =
        readerApiFactory
            .createIsoCardSelector()
            .filterByCardProtocol(GENERIC_CARD_PROTOCOL)
            .filterByPowerOnData(POWER_ON_DATA_REGEX)
            .filterByDfName(AID1);

    GenericCardSelectionExtension genericCardSelectionExtension =
        GenericExtensionService.getInstance().createGenericCardSelectionExtension();

    manager.prepareSelection(cardSelector, genericCardSelectionExtension);
    return manager;
  }

  private CardSelectionManager initManagerForExportImportProcessedScenario() {
    StubSmartCard card =
        StubSmartCard.builder()
            .withPowerOnData(HexUtil.toByteArray(POWER_ON_DATA))
            .withProtocol(ISO_CARD_PROTOCOL)
            .withSimulatedCommand(
                SELECT_APPLICATION1_CMD, SELECT_APPLICATION1_RSP) // Select app 1 not found
            .withSimulatedCommand(SELECT_APPLICATION2_CMD, SELECT_APPLICATION2_RSP) // Select app 2
            .withSimulatedCommand(READ_CMD, READ_RSP) // Read
            .build();
    Plugin plugin =
        SmartCardServiceProvider.getService()
            .registerPlugin(
                StubPluginFactoryBuilder.builder().withStubReader(READER_NAME, true, card).build());
    reader = (ConfigurableCardReader) plugin.getReader(READER_NAME);
    reader.activateProtocol(ISO_CARD_PROTOCOL, ISO_CARD_PROTOCOL);
    CardSelectionManager manager = readerApiFactory.createCardSelectionManager();
    CardSelector<IsoCardSelector> cardSelector =
        readerApiFactory.createIsoCardSelector().filterByDfName(AID1);
    GenericCardSelectionExtension genericCardSelectionExtension =
        GenericExtensionService.getInstance().createGenericCardSelectionExtension();
    manager.prepareSelection(cardSelector, genericCardSelectionExtension);
    cardSelector = readerApiFactory.createIsoCardSelector().filterByDfName(AID2);
    CalypsoCardSelectionExtension calypsoCardSelectionExtension =
        CalypsoExtensionService.getInstance()
            .getCalypsoCardApiFactory()
            .createCalypsoCardSelectionExtension()
            .prepareReadRecord(SFI, RECORD);
    manager.prepareSelection(cardSelector, calypsoCardSelectionExtension);
    return manager;
  }

  @Test
  public void exportImportCardSelectionScenario() {
    CardSelectionManager manager = initManagerForExportImportScenario();

    // Export 1
    String export1 = manager.exportCardSelectionScenario();

    JsonObject resultJson1 = JsonUtil.getParser().fromJson(export1, JsonObject.class);
    assertThat(resultJson1).isEqualTo(expectedJsonScenario);

    // Import
    CardSelectionManager manager2 = readerApiFactory.createCardSelectionManager();
    int index = manager2.importCardSelectionScenario(export1);
    assertThat(index).isEqualTo(1);

    // Export 2
    String export2 = manager2.exportCardSelectionScenario();

    JsonObject resultJson2 = JsonUtil.getParser().fromJson(export2, JsonObject.class);
    assertThat(resultJson2).isEqualTo(expectedJsonScenario);
  }

  @Test
  public void exportImportProcessedCardSelectionScenario() {
    CardSelectionManager manager = initManagerForExportImportProcessedScenario();
    String scenario = manager.exportCardSelectionScenario();

    // Export 1
    CardSelectionResult result1 = manager.processCardSelectionScenario(reader);
    String export1 = manager.exportProcessedCardSelectionScenario();

    JsonArray resultJson1 = JsonUtil.getParser().fromJson(export1, JsonArray.class);
    assertThat(resultJson1).isEqualTo(expectedJsonProcessedScenario);

    // Import
    CardSelectionManager manager2 = readerApiFactory.createCardSelectionManager();
    manager2.importCardSelectionScenario(scenario);
    CardSelectionResult result2 = manager2.importProcessedCardSelectionScenario(export1);

    assertThat(result2).usingRecursiveComparison().isEqualTo(result1);

    // Export 2
    String export2 = manager2.exportProcessedCardSelectionScenario();

    JsonArray resultJson2 = JsonUtil.getParser().fromJson(export2, JsonArray.class);
    assertThat(resultJson2).isEqualTo(expectedJsonProcessedScenario);
  }
}
