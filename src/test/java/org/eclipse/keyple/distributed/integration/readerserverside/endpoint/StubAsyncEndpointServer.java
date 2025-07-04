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
package org.eclipse.keyple.distributed.integration.readerserverside.endpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceServer;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.RemotePluginClient;
import org.eclipse.keyple.distributed.integration.util.JacksonParser;
import org.eclipse.keyple.distributed.integration.util.NamedThreadFactory;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a async server to test {@link RemotePluginClient}. Send and receive asynchronously
 * serialized {@link MessageDto} with connected {@link StubAsyncEndpointClient}
 */
public class StubAsyncEndpointServer implements AsyncEndpointServerSpi {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointServer.class);
  private final Map<String, StubAsyncEndpointClient> clients; // sessionId_client
  private final Map<String, Integer> messageCounts; // sessionId_counts
  private final ExecutorService taskPool;
  private final String localServiceName;

  public StubAsyncEndpointServer(String localServiceName) {
    clients = new ConcurrentHashMap<String, StubAsyncEndpointClient>();
    messageCounts = new ConcurrentHashMap<String, Integer>();
    taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("server-async-pool"));
    this.localServiceName = localServiceName;
  }

  /** Simulate a close socket operation */
  void close(String sessionId) {
    messageCounts.remove(sessionId);
    clients.remove(sessionId);
    SmartCardServiceProvider.getService()
        .getDistributedLocalService(localServiceName)
        .getExtension(LocalServiceServer.class)
        .getAsyncNode()
        .onClose(sessionId);
  }

  /**
   * Simulate data received by the socket
   *
   * @param jsonData incoming json data
   */
  void onData(final String jsonData, final StubAsyncEndpointClient client) {
    final MessageDto message = JacksonParser.fromJson(jsonData);
    clients.put(message.getSessionId(), client);
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            SmartCardServiceProvider.getService()
                .getDistributedLocalService(localServiceName)
                .getExtension(LocalServiceServer.class)
                .getAsyncNode()
                .onMessage(message);
          }
        });
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final String data = JacksonParser.toJson(msg);
    final StubAsyncEndpointClient client = clients.get(msg.getSessionId());
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              client.onMessage(data);
            } catch (Throwable t) {
              SmartCardServiceProvider.getService()
                  .getDistributedLocalService(localServiceName)
                  .getExtension(LocalServiceServer.class)
                  .getAsyncNode()
                  .onError(msg.getSessionId(), t);
            }
          }
        });
  }
}
