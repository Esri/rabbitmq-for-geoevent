package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

public class RabbitMQConsumer extends RabbitMQConnectionBroker.RabbitMQComponentBase
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConsumer.class);
	private RabbitMQQueueingConsumer	consumer;
	private RabbitMQQueue							queue;
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
