package com.esri.geoevent.transport.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RabbitMQQueueingConsumer implements Consumer
{
	private final Channel									channel;
	private volatile String								consumerTag;
	private final BlockingQueue<Delivery>	queue	= new LinkedBlockingQueue<Delivery>();

	public RabbitMQQueueingConsumer(Channel channel)
	{
		this.channel = channel;
	}

	public Channel getChannel()
	{
		return channel;
	}

	public String getConsumerTag()
	{
		return consumerTag;
	}

	@Override
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException shutdownSignal)
	{
		// ignore
	}

	@Override
	public void handleCancel(String consumerTag) throws IOException
	{
		// ignore
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
	{
		queue.add(new Delivery(envelope, properties, body));
	}

	public Delivery nextDelivery() throws InterruptedException, ShutdownSignalException, ConsumerCancelledException
	{
		return queue.take();
	}

	public Delivery nextDelivery(long timeout) throws InterruptedException, ShutdownSignalException, ConsumerCancelledException
	{
		return queue.poll(timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void handleConsumeOk(String consumerTag)
	{
		this.consumerTag = consumerTag;
	}

	@Override
	public void handleCancelOk(String consumerTag)
	{
		// ignore
	}

	@Override
	public void handleRecoverOk(String consumerTag)
	{
		// ignore
	}

	public static class Delivery
	{
		private final Envelope							envelope;
		private final AMQP.BasicProperties	properties;
		private final byte[]								body;

		public Delivery(Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		{
			this.envelope = envelope;
			this.properties = properties;
			this.body = body;
		}

		public Envelope getEnvelope()
		{
			return envelope;
		}

		public BasicProperties getProperties()
		{
			return properties;
		}

		public byte[] getBody()
		{
			return body;
		}
	}
}
