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

public class RabbitMQQueue implements Validatable
{
	private static final BundleLogger	LOGGER	= BundleLoggerFactory.getLogger(RabbitMQExchange.class);
	private String										name;
	private RabbitMQDurability				durability;
	private boolean										exclusive;
	private boolean										autoDelete;

	public RabbitMQQueue(String name, String durability, String exclusive, String autoDelete)
	{
		this.name = name;
		this.durability = Validator.valueOfIgnoreCase(RabbitMQDurability.class, durability, RabbitMQDurability.Transient);
		this.exclusive = Converter.convertToBoolean(exclusive, false);
		this.autoDelete = Converter.convertToBoolean(autoDelete, true);
	}

	public String getName()
	{
		return name;
	}

	public boolean isDurable()
	{
		return RabbitMQDurability.Durable.equals(durability);
	}

	public boolean isExclusive()
	{
		return exclusive;
	}

	public boolean isAutoDelete()
	{
		return autoDelete;
	}

	@Override
	public void validate() throws ValidationException
	{
		if (name == null || name.isEmpty())
			throw new ValidationException(LOGGER.translate("QUEUE_NAME_VALIDATE_ERROR"));
	}
}
