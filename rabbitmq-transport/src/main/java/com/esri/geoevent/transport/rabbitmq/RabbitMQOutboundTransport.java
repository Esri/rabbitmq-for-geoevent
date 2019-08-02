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

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.OutboundTransportBase;
import com.esri.ges.transport.TransportDefinition;

import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Observer;

public class RabbitMQOutboundTransport extends OutboundTransportBase implements Observer
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQOutboundTransport.class);
  private RabbitMQConnectionInfo    connectionInfo;
  private RabbitMQExchange          exchange;
  private RabbitMQProducer          producer;

  public RabbitMQOutboundTransport(TransportDefinition definition) throws ComponentException
  {
    super(definition);
  }

  @Override
  public synchronized void receive(final ByteBuffer buffer, String channelId)
  {
    try
    {
      if (producer != null)
        producer.send(buffer);
    }
    catch (RabbitMQTransportException e)
    {
      ;
    }
  }

  @SuppressWarnings("incomplete-switch")
  public synchronized void start() throws RunningException
  {
    switch (getRunningState())
    {
      case STOPPING:
      case STOPPED:
      case ERROR:
        connect();
        break;
    }
  }

  @Override
  public synchronized void stop()
  {
    if (!RunningState.STOPPED.equals(getRunningState()))
      disconnect("");
  }

  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    shutdownProducer();
    String password;
    try
    {
      password = getProperty("password").getDecryptedValue();
    }
    catch (Exception e)
    {
      password = getProperty("password").getValueAsString();
    }

    String host = getProperty("host").getValueAsString();
    String port = getProperty("port").getValueAsString();
    String virtualHost = getProperty("virtualHost").getValueAsString();
    if (virtualHost == null || virtualHost.trim().isEmpty())
      virtualHost = "/";
    String username = getProperty("username").getValueAsString();
    String ssl = getProperty("ssl").getValueAsString();
    connectionInfo = new RabbitMQConnectionInfo(host, port, virtualHost, username, password, ssl);

    String exchangeName = getProperty("exchangeName").getValueAsString();
    String exchangeType = getProperty("exchangeType").getValueAsString();
    String exchangeDurability = getProperty("exchangeDurability").getValueAsString();
    String exchangeAutoDelete = getProperty("exchangeAutoDelete").getValueAsString();
    String routingKey = getProperty("routingKey").getValueAsString();
    exchange = new RabbitMQExchange(exchangeName, exchangeType, exchangeDurability, exchangeAutoDelete, routingKey);
  }

  @Override
  public void validate() throws ValidationException
  {
    super.validate();
    connectionInfo.validate();
    exchange.validate();
  }

  private synchronized void connect()
  {
    disconnect("");
    setRunningState(RunningState.STARTING);
    try
    {
      if (producer == null)
      {
        producer = new RabbitMQProducer(connectionInfo, exchange);
        producer.addObserver(this);
      }
      producer.connect();
      setRunningState(RunningState.STARTED);
    }
    catch (RabbitMQTransportException e)
    {
      disconnect(e.getMessage());
      setRunningState(RunningState.ERROR);
    }
  }

  private synchronized void disconnect(String reason)
  {
    setRunningState(RunningState.STOPPING);
    if (producer != null)
      producer.disconnect(reason);
    setErrorMessage(reason);
    setRunningState(RunningState.STOPPED);
  }

  private synchronized void shutdownProducer()
  {
    if (producer != null)
    {
      producer.deleteObserver(this);
      producer.shutdown("");
      producer = null;
    }
  }

  public void shutdown()
  {
    shutdownProducer();
    super.shutdown();
  }

  @Override
  public void update(Observable observable, Object obj)
  {
    if (obj instanceof RabbitMQTransportEvent)
    {
      RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
      switch (event.getStatus())
      {
        case CREATED:
        case RECOVERY:
          try
          {
            start();
          }
          catch (RunningException e)
          {
            ;
          }
          break;
        case DISCONNECTED:
          disconnect("");
          break;
        case SHUTDOWN:
          shutdown();
          break;
        case RECOVERY_FAILED:
        case CREATION_FAILED:
          LOGGER.error(event.getDetails());
          disconnect(event.getDetails());
          setRunningState(RunningState.ERROR);
          break;
        case RECOVERY_STARTED:
          break;
        case RECOVERY_COMPLETED:
          break;
        default:
          break;
      }
    }
  }
}
