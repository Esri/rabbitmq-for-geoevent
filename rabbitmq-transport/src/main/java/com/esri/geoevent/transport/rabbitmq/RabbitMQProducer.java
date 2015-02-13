package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RabbitMQProducer extends RabbitMQComponentBase
{
	private static final Log LOGGER = LogFactory.getLog(RabbitMQProducer.class);

	public RabbitMQProducer(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange)
	{
		super(connectionInfo, exchange);
	}

	public void send(final ByteBuffer buffer) throws RabbitMQTransportException
	{
		if (buffer == null || !buffer.hasRemaining())
		{
			String msg = "Nothing to send"; // TODO: ???
			LOGGER.error(msg);
			throw new RabbitMQTransportException(msg);
		}
		else if (isConnected())
		{
			try
			{
				channel.basicPublish(exchange.getName(), exchange.getRoutingKey(), null, buffer.array());
			}
			catch (IOException e)
			{
				String msg = "AMQP_FACTORY_SENT_FAILURE_ERROR"; // TODO: ???
				LOGGER.error(msg);
				throw new RabbitMQTransportException(msg, e);
			}
		}
	}
}
