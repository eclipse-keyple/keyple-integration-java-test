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
package org.example.thirdparty;

import org.eclipse.keyple.core.common.KeypleReaderExtension;
import org.eclipse.keyple.core.plugin.ReaderIOException;
import org.eclipse.keyple.core.plugin.spi.reader.ReaderSpi;

/** Reader of a third-party plugin, whose card presence check can be configured to fail. */
public class ThirdPartyReader implements ReaderSpi, KeypleReaderExtension {

  public static final String NAME = "thirdPartyReader";
  public static final String ERROR_MESSAGE = "Third-party reader error";

  /** The failure raised by the card presence check. */
  public enum FailureMode {
    NONE,
    READER_IO,
    THIRD_PARTY
  }

  private static volatile FailureMode failureMode = FailureMode.NONE;

  public static void setFailureMode(FailureMode mode) {
    failureMode = mode;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void openPhysicalChannel() {}

  @Override
  public void closePhysicalChannel() {}

  @Override
  public boolean isPhysicalChannelOpen() {
    return false;
  }

  @Override
  public boolean checkCardPresence() throws ReaderIOException {
    switch (failureMode) {
      case READER_IO:
        throw new ReaderIOException(ERROR_MESSAGE);
      case THIRD_PARTY:
        throw new ThirdPartyReaderException(ERROR_MESSAGE);
      default:
        return false;
    }
  }

  @Override
  public String getPowerOnData() {
    return "";
  }

  @Override
  public byte[] transmitApdu(byte[] apdu) {
    return new byte[] {(byte) 0x90, (byte) 0x00};
  }

  @Override
  public boolean isContactless() {
    return true;
  }

  @Override
  public void onUnregister() {}
}
