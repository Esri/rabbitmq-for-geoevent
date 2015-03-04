package com.esri.geoevent.transport.rabbitmq;

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;
import com.esri.ges.util.Validator;

public class RabbitMQQueue implements Validatable
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQExchange.class);
	private String							name;
	private RabbitMQDurability	durability;
	private boolean							exclusive;
	private boolean							autoDelete;

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
