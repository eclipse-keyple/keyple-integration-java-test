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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.integration.util.JacksonParser;
import org.eclipse.keyple.distributed.integration.util.NamedThreadFactory;
import org.eclipse.keyple.distributed.spi.AsyncEndpointClientSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Async client endpoint to test {@link RemotePluginServer}. Send and receive asynchronously json
 * serialized {@link MessageDto} with {@link StubAsyncEndpointServer}.
 */
public class StubAsyncEndpointClient implements AsyncEndpointClientSpi {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointClient.class);
  private final StubAsyncEndpointServer server;
  private final ExecutorService taskPool;
  private final Boolean simulateConnectionError;
  private final AtomicInteger messageSent = new AtomicInteger();
  private final String localServiceName;

  /**
   * Constructor
   *
   * @param server
   * @param simulateConnectionError
   */
  public StubAsyncEndpointClient(
      StubAsyncEndpointServer server, Boolean simulateConnectionError, String localServiceName) {
    this.server = server;
    this.taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("client-async-pool"));
    this.simulateConnectionError = simulateConnectionError;
    messageSent.set(0);
    this.localServiceName = localServiceName;
  }

  /**
   * Receive serialized keyple message dto from the server
   *
   * @param data not null json data
   */
  void onMessage(final String data) {
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            // create task
            MessageDto message = JacksonParser.fromJson(data);
            SmartCardServiceProvider.getService()
                .getDistributedLocalService(localServiceName)
                .getExtension(LocalServiceClient.class)
                .getAsyncNode()
                .onMessage(message);
          }
        });
  }

  @Override
  public void openSession(String sessionId) {
    SmartCardServiceProvider.getService()
        .getDistributedLocalService(localServiceName)
        .getExtension(LocalServiceClient.class)
        .getAsyncNode()
        .onOpen(sessionId);
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final StubAsyncEndpointClient thisClient = this;
    if (messageSent.incrementAndGet() == 2 && simulateConnectionError) {
      throw new StubNetworkConnectionException("Simulate a unreachable server exception");
    }
    // create task
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            String data = JacksonParser.toJson(msg);
            server.onData(data, thisClient);
          }
        });
  }

  @Override
  public void closeSession(String sessionId) {
    server.close(sessionId);
    // currentSessionIds.remove(sessionId);
    SmartCardServiceProvider.getService()
        .getDistributedLocalService(localServiceName)
        .getExtension(LocalServiceClient.class)
        .getAsyncNode()
        .onClose(sessionId);
  }
}
