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

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.InboundTransportBase;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.util.Converter;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Observer;

public class RabbitMQInboundTransport extends InboundTransportBase implements Runnable, Observer
{
	private static final BundleLogger	LOGGER	= BundleLoggerFactory.getLogger(RabbitMQInboundTransport.class);
	private RabbitMQConnectionInfo		connectionInfo;
	private RabbitMQExchange					exchange;
	private RabbitMQQueue							queue;
	private int												prefetchCount;
	private RabbitMQConsumer					consumer;

	public RabbitMQInboundTransport(TransportDefinition definition) throws ComponentException
	{
		super(definition);
	}

	public boolean isClusterable()
	{
		return true;
	}

	@Override
	public void run()
	{
		setErrorMessage("");
		setRunningState(RunningState.STARTED);
		while (isRunning())
		{
			try
			{
				byte[] bytes = consumer.receive();
				if (bytes != null && bytes.length > 0)
				{
					ByteBuffer bb = ByteBuffer.allocate(bytes.length);
					bb.put(bytes);
					bb.flip();
					byteListener.receive(bb, "");
					bb.clear();
				}
			}
			catch (RabbitMQTransportException e)
			{
				LOGGER.error("", e);
			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public synchronized void start() throws RunningException
	{
		switch (getRunningState())
		{
			case STOPPING:
			case STOPPED:
			case ERROR:
				connect();
				break;
		}
	}

	@Override
	public synchronized void stop()
	{
		if (!RunningState.STOPPED.equals(getRunningState()))
			disconnect("");
	}

	@Override
	public void afterPropertiesSet()
	{
		shutdownConsumer();
		String password;
		try
		{
			password = getProperty("password").getDecryptedValue();
		}
		catch (Exception e)
		{
			password = getProperty("password").getValueAsString();
		}

		String host     		= getProperty("host").getValueAsString();
		String port     		= getProperty("port").getValueAsString();
		String virtualHost 	= getProperty("virtualHost").getValueAsString();
		String username 		= getProperty("username").getValueAsString();
		String ssl      		= getProperty("ssl").getValueAsString();
		connectionInfo 			= new RabbitMQConnectionInfo(host, port, virtualHost, username, password, ssl);

		String exchangeName       = getProperty("exchangeName").getValueAsString();
		String exchangeType       = getProperty("exchangeType").getValueAsString();
		String exchangeDurability = getProperty("exchangeDurability").getValueAsString();
		String exchangeAutoDelete = getProperty("exchangeAutoDelete").getValueAsString();
		String routingKey         = getProperty("routingKey").getValueAsString();
		exchange = new RabbitMQExchange(exchangeName, exchangeType, exchangeDurability, exchangeAutoDelete, routingKey);

		String queueName       = getProperty("queueName").getValueAsString();
		String queueDurability = getProperty("queueDurability").getValueAsString();
		String queueExclusive  = getProperty("queueExclusive").getValueAsString();
		String queueAutoDelete = getProperty("queueAutoDelete").getValueAsString();
		queue = new RabbitMQQueue(queueName, queueDurability, queueExclusive, queueAutoDelete);

		prefetchCount = Converter.convertToInteger(getProperty("prefetchCount").getValueAsString(), 1);
		super.afterPropertiesSet();
	}

	@Override
	public void validate() throws ValidationException
	{
		super.validate();
		connectionInfo.validate();
		exchange.validate();
		queue.validate();
	}

	private synchronized void connect()
	{
		disconnect("");
		setRunningState(RunningState.STARTING);
		try
		{
			if (consumer == null)
			{
				consumer = new RabbitMQConsumer(connectionInfo, exchange, queue);
				consumer.addObserver(this);
			}
			consumer.setPrefetchCount(prefetchCount);
			consumer.connect();
			new Thread(this).start();
		}
		catch (RabbitMQTransportException e)
		{
			disconnect(e.getMessage());
			setRunningState(RunningState.ERROR);
		}
	}

	private synchronized void disconnect(String reason)
	{
		setRunningState(RunningState.STOPPING);
		if (consumer != null)
			consumer.disconnect(reason);
		setErrorMessage(reason);
		setRunningState(RunningState.STOPPED);
	}

	private synchronized void shutdownConsumer()
	{
		if (consumer != null)
		{
			consumer.deleteObserver(this);
			consumer.shutdown("");
			consumer = null;
		}
	}

	public void shutdown()
	{
		shutdownConsumer();
		super.shutdown();
	}

	@Override
	public void update(Observable observable, Object obj)
	{
		if (obj instanceof RabbitMQTransportEvent)
		{
			RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
			switch (event.getStatus())
			{
				case CREATED:
				case RECOVERY:
					try
					{
						start();
					}
					catch (RunningException e)
					{
						;
					}
					break;
				case DISCONNECTED:
					disconnect("");
					break;
				case SHUTDOWN:
					shutdown();
					break;
				case RECOVERY_FAILED:
				case CREATION_FAILED:
					LOGGER.error(event.getDetails());
					disconnect(event.getDetails());
					setRunningState(RunningState.ERROR);
					break;
				case RECOVERY_STARTED:
					break;
				case RECOVERY_COMPLETED:
					break;
				default:
					break;
			}
		}
	}
}
