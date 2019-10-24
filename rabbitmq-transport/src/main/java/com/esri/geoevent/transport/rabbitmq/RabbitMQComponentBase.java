/*
  Copyright 1995-2015  Esri

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

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeoutException;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitMQComponentBase extends RabbitMQObservable implements Observer
{
  private static final BundleLogger LOGGER    = BundleLoggerFactory.getLogger(RabbitMQComponentBase.class);
  private RabbitMQConnectionBroker  broker;
  protected RabbitMQExchange        exchange;
  protected volatile boolean        connected = false;
  private String                    details   = "";
  protected Channel                 channel;

  public RabbitMQComponentBase(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange)
  {
    broker = new RabbitMQConnectionBroker(connectionInfo);
    broker.addObserver(this);
    this.exchange = exchange;
  }

  protected synchronized void init() throws RabbitMQTransportException
  {
    try
    {
      channel.addShutdownListener(new ShutdownListener()
        {
          @Override
          public void shutdownCompleted(ShutdownSignalException cause)
          {
            disconnect(cause.getMessage());
          }
        });

      channel.exchangeDeclare(exchange.getName(), exchange.getType().toString(), exchange.isDurable(), exchange.isAutoDelete(), null);
    }
    catch (IOException e)
    {
      String msg = LOGGER.translate("EXCHANGE_CREATE_ERROR", e.getMessage());
      LOGGER.error(msg, e);
      throw new RabbitMQTransportException(msg);
    }
  }

  public String getStatusDetails()
  {
    return details;
  }

  public boolean isConnected()
  {
    return connected;
  }

  protected synchronized void connect() throws RabbitMQTransportException
  {
    disconnect(null);
    if (broker.isConnected())
    {
      if (channel == null)
        channel = broker.createChannel();
      init();
      details = "";
      connected = true;
    }
    else
    {
      details = LOGGER.translate("CONNECTION_BROKEN_ERROR", broker.getConnectionInfo().getHost());
      LOGGER.error(details);
      throw new RabbitMQTransportException(details);
    }
  }

  protected synchronized void disconnect(String reason)
  {
    if (connected)
    {
      if (channel != null)
      {
        if (channel.isOpen())
        {
          try
          {
            channel.close();
          }
          catch (IOException | TimeoutException e)
          {
            LOGGER.error("CHANNEL_CLOSE_ERROR", e.getMessage(), e);
          }
        }
        channel = null;
      }
    }
    connected = false;
    details = reason;
  }

  public void shutdown()
  {
    disconnect("");
    broker.deleteObserver(this);
    broker.shutdown();
  }

  @Override
  public void update(Observable observable, Object obj)
  {
    if (obj instanceof RabbitMQTransportEvent)
    {
      RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
      notifyObservers(event.getStatus(), event.getDetails());
    }
  }
}
