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

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;
import com.esri.ges.util.Validator;

enum RabbitMQExchangeType
{
	direct, fanout
}

public class RabbitMQExchange implements Validatable
{
	private static final BundleLogger	LOGGER	= BundleLoggerFactory.getLogger(RabbitMQExchange.class);
	private String										name;
	private RabbitMQExchangeType			type;
	private RabbitMQDurability				durability;
	private boolean										autoDelete;
	private String										routingKey;

	public RabbitMQExchange(String name, String type, String durability, String autoDelete, String routingKey)
	{
		this.name = name;
		this.type = Validator.valueOfIgnoreCase(RabbitMQExchangeType.class, type, RabbitMQExchangeType.direct);
		this.durability = Validator.valueOfIgnoreCase(RabbitMQDurability.class, durability, RabbitMQDurability.Transient);
		this.autoDelete = Converter.convertToBoolean(autoDelete, true);
		this.routingKey = routingKey;
	}

	public String getName()
	{
		return name;
	}

	public RabbitMQExchangeType getType()
	{
		return type;
	}

	public boolean isDurable()
	{
		return RabbitMQDurability.Durable.equals(durability);
	}

	public boolean isAutoDelete()
	{
		return autoDelete;
	}

	public String getRoutingKey()
	{
		return routingKey;
	}

	@Override
	public void validate() throws ValidationException
	{
		if (name == null || name.isEmpty())
			throw new ValidationException(LOGGER.translate("EXCHANGE_VALIDATE_ERROR"));
	}
}
