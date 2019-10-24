/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
*/

package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ConnectionListener;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Connection;

public class RabbitMQConnectionListener extends RabbitMQObservable implements ConnectionListener
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConnectionListener.class);

  private RabbitMQConnectionInfo    connectionInfo;

  public RabbitMQConnectionListener(RabbitMQConnectionInfo connectionInfo)
  {
    this.connectionInfo = connectionInfo;
  }

  @Override
  public void onCreate(Connection connection)
  {
    notifyObservers(RabbitMQConnectionStatus.CREATED, LOGGER.translate("CONNECTION_ESTABLISH_SUCCESS", connection.getAddress().getCanonicalHostName()), connection);
  }

  @Override
  public void onCreateFailure(Throwable failure)
  {
    String msg = "Unknown Error";
    if (failure != null)
    {
      String failureMsg = failure.getMessage();
      if (failureMsg == null || failureMsg.trim().isEmpty())
        msg = failure.toString();
    }
    notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, LOGGER.translate("CONNECTION_ESTABLISH_FAILURE", (connectionInfo != null ? connectionInfo.getHost() : "UnknownHost"), msg));
  }

  @Override
  public void onRecovery(Connection connection)
  {

    notifyObservers(RabbitMQConnectionStatus.RECOVERY, LOGGER.translate("CONNECTION_RECOVERED", connection.getAddress().getCanonicalHostName()), connection);
  }

  @Override
  public void onRecoveryStarted(Connection connection)
  {
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, LOGGER.translate("CONNECTION_RECOVERY_STARTED", connection.getAddress().getCanonicalHostName()), connection);
  }

  @Override
  public void onRecoveryCompleted(Connection connection)
  {
    notifyObservers(new RabbitMQTransportEvent(RabbitMQConnectionStatus.RECOVERY_COMPLETED, LOGGER.translate("CONNECTION_RECOVERY_COMPLETED", connection.getAddress().getCanonicalHostName()), connection));
  }

  @Override
  public void onRecoveryFailure(Connection connection, Throwable failure)
  {
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, LOGGER.translate("CONNECTION_RECOVERY_FAILED", connection.getAddress().getCanonicalHostName(), failure.getMessage()), connection);
  }
}
