package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RabbitMQConsumer extends RabbitMQComponentBase
{
	private static final Log					LOGGER	= LogFactory.getLog(RabbitMQConsumer.class);
	private RabbitMQQueueingConsumer	consumer;
	private RabbitMQQueue							queue;

	public RabbitMQConsumer(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange, RabbitMQQueue queue)
	{
		super(connectionInfo, exchange);
		this.queue = queue;
	}

	@Override
	protected synchronized void init() throws RabbitMQTransportException
	{
		super.init();
		try
		{
			channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(), null);
			channel.queueBind(queue.getName(), exchange.getName(), exchange.getRoutingKey());
		}
		catch (IOException e)
		{
			String msg = "AMQP_BASE_INIT_ERROR"; // TODO: ????
			LOGGER.error(msg);
			throw new RabbitMQTransportException(msg, e);
		}
		try
		{
			consumer = new RabbitMQQueueingConsumer(channel);
			channel.basicConsume(queue.getName(), true, consumer);
		}
		catch (IOException e)
		{
			String msg = "AMQP_FACTORY_CONSUMER_INIT_ERROR"; // TODO: ???
			LOGGER.error(msg);
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
							LOGGER.error("AMQP_FACTORY_CONSUMER_SHUTDOWN_CANCEL", e); // TODO: ???
							LOGGER.error(e.getMessage(), e);
						}
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
							;
						}
						consumer = null;
					}
				}
			}
		}
		super.disconnect(reason);
	}
}
