/*
  Copyright 1995-2014 Esri

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

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.transport.TransportDefinitionBase;
import com.esri.ges.transport.TransportType;

import java.util.Arrays;

public class RabbitMQInboundTransportDefinition extends TransportDefinitionBase
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQInboundTransportDefinition.class);

  public RabbitMQInboundTransportDefinition()
  {
    super(TransportType.INBOUND);
    try
    {
      // Connection properties
      propertyDefinitions.put("host", new PropertyDefinition("host", PropertyType.String, "localhost", "Host", "Host", true, false));
      propertyDefinitions.put("port", new PropertyDefinition("port", PropertyType.Integer, "5672", "Port", "Port", true, false));
      propertyDefinitions.put("username", new PropertyDefinition("username", PropertyType.String, null, "User Name", "User Name", false, false));
      propertyDefinitions.put("password", new PropertyDefinition("password", PropertyType.Password, null, "Password", "Password", false, false));
      propertyDefinitions.put("ssl", new PropertyDefinition("ssl", PropertyType.Boolean, false, "SSL", "SSL", true, false));

      // Exchange properties
      propertyDefinitions.put("exchangeName", new PropertyDefinition("exchangeName", PropertyType.String, null, "RabbitMQ Exchange Name", "RabbitMQ Exchange Name", true, false));
      propertyDefinitions.put("exchangeType", new PropertyDefinition("exchangeType", PropertyType.String, RabbitMQExchangeType.direct.toString(), "RabbitMQ Exchange Type", "RabbitMQ Exchange Type", true, false,
              Arrays.asList(
                  new LabeledValue(RabbitMQExchangeType.direct.toString(), RabbitMQExchangeType.direct.toString()),
                  new LabeledValue(RabbitMQExchangeType.fanout.toString(), RabbitMQExchangeType.fanout.toString()),
                  new LabeledValue(RabbitMQExchangeType.topic.toString(), RabbitMQExchangeType.topic.toString()),
                  new LabeledValue(RabbitMQExchangeType.headers.toString(), RabbitMQExchangeType.headers.toString()))
          )
      );
      propertyDefinitions.put("exchangeDurability", new PropertyDefinition("exchangeDurability", PropertyType.String, RabbitMQDurability.Transient.toString(), "RabbitMQ Exchange Durability", "Durable exchanges survive broker restart whereas transient exchanges do not.", true, false,
              Arrays.asList(
                  new LabeledValue(RabbitMQDurability.Transient.toString(), RabbitMQDurability.Transient.toString()),
                  new LabeledValue(RabbitMQDurability.Durable.toString(), RabbitMQDurability.Durable.toString()))
          )
      );
      propertyDefinitions.put("exchangeAutoDelete", new PropertyDefinition("exchangeAutoDelete", PropertyType.Boolean, "true", "RabbitMQ Exchange Auto delete", "If 'true', the exchange will delete itself after at least one queue or exchange has been bound to this one, and then all queues or exchanges have been unbound.", true, false));

      // Queue properties
      propertyDefinitions.put("queueName", new PropertyDefinition("queueName", PropertyType.String, null, "RabbitMQ Queue Name", "RabbitMQ Queue Name", true, false));
      propertyDefinitions.put("queueDurability", new PropertyDefinition("queueDurability", PropertyType.String, RabbitMQDurability.Transient.toString(), "RabbitMQ Queue Durability", "Durable queues survive broker restart whereas transient queues do not.", true, false,
              Arrays.asList(
                  new LabeledValue(RabbitMQDurability.Transient.toString(), RabbitMQDurability.Transient.toString()),
                  new LabeledValue(RabbitMQDurability.Durable.toString(), RabbitMQDurability.Durable.toString()))
          )
      );
      propertyDefinitions.put("queueExclusive", new PropertyDefinition("queueExclusive", PropertyType.Boolean, "false", "RabbitMQ Exclusive Queue", "Exclusive Queue is used by only one connection and will be deleted when that connection closes.", true, false));
      propertyDefinitions.put("queueAutoDelete", new PropertyDefinition("queueAutoDelete", PropertyType.Boolean, "true", "RabbitMQ Queue Auto-delete", "If 'true', the queue will delete itself after at least one consumer has connected, and then all consumers have disconnected.", true, false));

      propertyDefinitions.put("prefetchCount", new PropertyDefinition("prefetchCount", PropertyType.Integer, 1, "Quality of Service", "Tell RabbitMQ how many unacknowledged messages each worker can handle at a time.", false, false));
      propertyDefinitions.put("routingKey", new PropertyDefinition("routingKey", PropertyType.String, "", "Routing Key", "Routing Key", false, false));
    }
    catch (PropertyException e)
    {
      String errorMsg = LOGGER.translate("IN_INIT_ERROR", e.getMessage());
      LOGGER.error(errorMsg, e);
      throw new RuntimeException(errorMsg, e);
    }
  }

  @Override
  public String getName()
  {
    return "RabbitMQ";
  }

  @Override
  public String getDomain()
  {
    return "com.esri.geoevent.transport.inbound";
  }

  @Override
  public String getVersion()
  {
    return "10.3.0";
  }

  @Override
  public String getLabel()
  {
    return "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_LABEL}";
  }

  @Override
  public String getDescription()
  {
    return "${com.esri.geoevent.transport.rabbitmq-transport.TRANSPORT_IN_DESC}";
  }
}
