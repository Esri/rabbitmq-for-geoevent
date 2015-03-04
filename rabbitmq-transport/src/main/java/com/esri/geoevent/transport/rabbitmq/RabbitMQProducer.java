package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

public class RabbitMQProducer extends RabbitMQConnectionBroker.RabbitMQComponentBase
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQProducer.class);

	public RabbitMQProducer(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange)
	{
		super(connectionInfo, exchange);
	}

	public void send(final ByteBuffer buffer) throws RabbitMQTransportException
	{
		if (buffer == null || !buffer.hasRemaining())
		{
			String msg = LOGGER.translate("TRANSPORT_OUT_NO_MESSAGE_TO_SEND_ERROR");
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
				String msg = LOGGER.translate("TRANSPORT_OUT_FAILED_TO_SEND_MESSAGE_ERROR");
				LOGGER.error(msg);
				throw new RabbitMQTransportException(msg, e);
			}
		}
	}
}
