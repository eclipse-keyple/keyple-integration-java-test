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
package org.eclipse.keyple.distributed.integration.readerclientside.endpoint;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.integration.util.JacksonParser;
import org.eclipse.keyple.distributed.integration.util.NamedThreadFactory;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of a {@link SyncEndpointClientSpi} to test {@link RemotePluginServer}. It
 * simulates synchronous invocation to a remote server.
 */
public class StubSyncEndpointClient implements SyncEndpointClientSpi {

  private static final Logger logger = LoggerFactory.getLogger(StubSyncEndpointClient.class);
  public static String REMOTE_PLUGIN_NAME = "remotePlugin";
  private static final ExecutorService taskPool =
      Executors.newCachedThreadPool(new NamedThreadFactory("syncPool"));
  private final Boolean simulateConnectionError;
  private int messageSent = 0;

  public StubSyncEndpointClient(Boolean simulateConnectionError) {
    this.simulateConnectionError = simulateConnectionError;
  }

  @Override
  public List<MessageDto> sendRequest(MessageDto msg) {
    if (messageSent++ == 2 && simulateConnectionError) {
      throw new StubNetworkConnectionException("Simulate a host unreacheable error");
    }

    final String responsesJson;
    // serialize request
    final String request = JacksonParser.toJson(msg);

    try {
      responsesJson = taskPool.submit(sendData(request)).get();

      List<MessageDto> responses = JacksonParser.fromJsonList(responsesJson);

      return responses;
    } catch (InterruptedException e) {
      throw new RuntimeException("Impossible to process incoming message", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Impossible to process incoming message", e);
    }
  }

  /**
   * @param data json serialized (keyple message dto)
   * @return json serialized data (list of keyple dto)
   */
  private Callable<String> sendData(final String data) {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {
        // Send the dto to the sync node
        List<MessageDto> responses =
            SmartCardServiceProvider.getService()
                .getPlugin(REMOTE_PLUGIN_NAME)
                .getExtension(RemotePluginServer.class)
                .getSyncNode()
                .onRequest(JacksonParser.fromJson(data));

        return JacksonParser.toJson(responses);
      }
    };
  }
}
