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

/** Exception specific to the third-party reader (not belonging to a Keyple/Keypop package). */
public class ThirdPartyReaderException extends RuntimeException {

  public ThirdPartyReaderException(String message) {
    super(message);
  }
}
