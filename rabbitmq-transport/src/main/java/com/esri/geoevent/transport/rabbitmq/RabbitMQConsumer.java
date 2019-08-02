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

import java.io.IOException;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

public class RabbitMQConsumer extends RabbitMQConnectionBroker.RabbitMQComponentBase
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConsumer.class);
  private RabbitMQQueueingConsumer  consumer;
  private RabbitMQQueue             queue;
  private int                       prefetchCount;

  public RabbitMQConsumer(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange, RabbitMQQueue queue)
  {
    super(connectionInfo, exchange);
    this.queue = queue;
    this.prefetchCount = 1;
  }

  public void setPrefetchCount(int value)
  {
    this.prefetchCount = value;
  }

  @Override
  protected synchronized void init() throws RabbitMQTransportException
  {
    super.init();
    try
    {
      channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(), null);
      channel.queueBind(queue.getName(), exchange.getName(), exchange.getRoutingKey());
      channel.basicQos(prefetchCount);
    }
    catch (IOException e)
    {
      String msg = LOGGER.translate("CHANNEL_INIT_ERROR", e.getMessage());
      LOGGER.error(msg, e);
      throw new RabbitMQTransportException(msg, e);
    }
    try
    {
      consumer = new RabbitMQQueueingConsumer(channel);
      channel.basicConsume(queue.getName(), true, consumer);
    }
    catch (IOException e)
    {
      String msg = LOGGER.translate("CONSUMER_INIT_ERROR", e.getMessage());
      LOGGER.error(msg, e);
      throw new RabbitMQTransportException(msg, e);
    }
  }

  public byte[] receive() throws RabbitMQTransportException
  {
    RabbitMQQueueingConsumer.Delivery delivery = null;
    try
    {
      delivery = consumer.nextDelivery(100);
    }
    catch (Exception e)
    {
      // ignore
    }
    return (delivery != null) ? delivery.getBody() : null;
  }

  @Override
  protected synchronized void disconnect(String reason)
  {
    if (connected)
    {
      if (channel != null)
      {
        if (channel.isOpen())
        {
          if (consumer != null)
          {
            try
            {
              channel.basicCancel(consumer.getConsumerTag());
            }
            catch (IOException e)
            {
              LOGGER.error("CONSUMER_CANCEL_ERROR", e.getMessage(), e);
            }
            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
              // ignore
            }
            consumer = null;
          }
        }
      }
    }
    super.disconnect(reason);
  }
}
