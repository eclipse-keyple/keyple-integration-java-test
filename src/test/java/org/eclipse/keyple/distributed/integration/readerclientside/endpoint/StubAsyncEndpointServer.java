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
package org.eclipse.keyple.distributed.integration.readerclientside.endpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.integration.util.JacksonParser;
import org.eclipse.keyple.distributed.integration.util.NamedThreadFactory;
import org.eclipse.keyple.distributed.spi.AsyncEndpointServerSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulate a async server to test {@link RemotePluginServer}. Send and receive asynchronously
 * serialized {@link MessageDto} with connected {@link StubAsyncEndpointClient}
 */
public class StubAsyncEndpointServer implements AsyncEndpointServerSpi {

  private static final Logger logger = LoggerFactory.getLogger(StubAsyncEndpointServer.class);
  public static String REMOTE_PLUGIN_NAME = "remotePlugin";
  private final Map<String, StubAsyncEndpointClient> clients; // sessionId_client
  private final Map<String, Integer> messageCounts; // sessionId_counts
  private final ExecutorService taskPool;

  private boolean simulateConnectionError;

  public StubAsyncEndpointServer() {
    clients = new ConcurrentHashMap<String, StubAsyncEndpointClient>();
    messageCounts = new ConcurrentHashMap<String, Integer>();
    taskPool = Executors.newCachedThreadPool(new NamedThreadFactory("server-async-pool"));
    simulateConnectionError = false;
  }

  /** Simulate a close socket operation */
  void close(String sessionId) {
    messageCounts.remove(sessionId);
    clients.remove(sessionId);
    SmartCardServiceProvider.getService()
        .getPlugin(REMOTE_PLUGIN_NAME)
        .getExtension(RemotePluginServer.class)
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
                .getPlugin(REMOTE_PLUGIN_NAME)
                .getExtension(RemotePluginServer.class)
                .getAsyncNode()
                .onMessage(message);
          }
        });
  }

  @Override
  public void sendMessage(final MessageDto msg) {
    final String data = JacksonParser.toJson(msg);
    if (incrementCountInSession(msg.getSessionId()) == 2 && simulateConnectionError) {
      simulateConnectionError = false; // reinit flag
      throw new StubNetworkConnectionException("Simulate a unreachable client exception");
    }
    final StubAsyncEndpointClient client = clients.get(msg.getSessionId());
    taskPool.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              client.onMessage(data);
            } catch (Throwable t) {
              SmartCardServiceProvider.getService()
                  .getPlugin(REMOTE_PLUGIN_NAME)
                  .getExtension(RemotePluginServer.class)
                  .getAsyncNode()
                  .onError(msg.getSessionId(), t);
            }
          }
        });
  }

  /**
   * Set to true to simulate a connection for the 2nd message received
   *
   * @param simulateConnectionError non nullable Boolean
   */
  public void setSimulateConnectionError(boolean simulateConnectionError) {
    this.simulateConnectionError = simulateConnectionError;
  }

  private Integer incrementCountInSession(String sessionId) {
    messageCounts.put(
        sessionId, messageCounts.get(sessionId) == null ? 1 : messageCounts.get(sessionId) + 1);
    return messageCounts.get(sessionId);
  }
}
