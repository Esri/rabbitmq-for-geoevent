package com.esri.geoevent.transport.rabbitmq;

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;
import com.esri.ges.util.Validator;

enum RabbitMQExchangeType
{
	direct, topic, fanout, headers
}

public class RabbitMQExchange implements Validatable
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQExchange.class);
	private String								    name;
	private RabbitMQExchangeType	    type;
	private RabbitMQDurability		    durability;
	private boolean								    autoDelete;
	private String								    routingKey;

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
