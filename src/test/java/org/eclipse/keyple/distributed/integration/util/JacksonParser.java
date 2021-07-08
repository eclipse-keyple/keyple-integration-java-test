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
package org.eclipse.keyple.distributed.integration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.eclipse.keyple.distributed.MessageDto;

public class JacksonParser {

  private static ObjectMapper parser = new ObjectMapper();

  public static String toJson(MessageDto message) {
    try {
      return parser.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while serializing dto", e);
    }
  }

  public static MessageDto fromJson(String data) {
    try {
      return parser.readValue(data, MessageDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while deserializing dto", e);
    }
  }

  public static String toJson(List<MessageDto> messages) {
    try {
      return parser.writeValueAsString(messages);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while serializing dto", e);
    }
  }

  public static List<MessageDto> fromJsonList(String data) {
    try {
      return parser.readValue(data, new TypeReference<List<MessageDto>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while deserializing dto", e);
    }
  }
}
