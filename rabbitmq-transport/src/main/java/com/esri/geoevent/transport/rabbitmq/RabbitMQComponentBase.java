package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class RabbitMQComponentBase extends RabbitMQObservable implements Observer
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQComponentBase.class);
  private RabbitMQConnectionBroker	broker;
	protected RabbitMQExchange				exchange;
	protected volatile boolean				connected	= false;
	private String										details		= "";
	protected Channel									channel;

	public RabbitMQComponentBase(RabbitMQConnectionInfo connectionInfo, RabbitMQExchange exchange)
	{
		broker = new RabbitMQConnectionBroker(connectionInfo);
		broker.addObserver(this);
		this.exchange = exchange;
	}

	protected synchronized void init() throws RabbitMQTransportException
	{
		try
		{
			channel.addShutdownListener(new ShutdownListener()
				{
					@Override
					public void shutdownCompleted(ShutdownSignalException cause)
					{
            disconnect(cause.getMessage());
					}
				});
			channel.exchangeDeclare(
					exchange.getName(),
					exchange.getType().toString(),
					exchange.isDurable(),
					exchange.isAutoDelete(),
					null
			);
		}
		catch (IOException e)
		{
			String msg = LOGGER.translate("EXCHANGE_CREATE_ERROR", e.getMessage());
			LOGGER.error(msg, e);
			throw new RabbitMQTransportException(msg);
		}
	}

	public String getStatusDetails()
  {
    return details;
  }

	public boolean isConnected()
	{
		return connected;
	}

	protected synchronized void connect() throws RabbitMQTransportException
  {
		disconnect(null);
    if (broker.isConnected())
    {
      if (channel == null)
        channel = broker.createChannel();
      init();
      details = "";
      connected = true;
    }
    else
    {
      details = LOGGER.translate("CONNECTION_BROKEN_ERROR", broker.getConnectionInfo().getHost());
      LOGGER.error(details);
      throw new RabbitMQTransportException(details);
    }
  }

	protected synchronized void disconnect(String reason)
	{
		if (connected)
		{
			if (channel != null)
			{
				if (channel.isOpen())
				{
					try
					{
						channel.close();
					}
					catch (IOException e)
					{
						LOGGER.error("CHANNEL_CLOSE_ERROR", e.getMessage(), e);
					}
				}
				channel = null;
			}
		}
		connected = false;
		details = reason;
	}

	public void shutdown()
	{
		disconnect("");
		broker.deleteObserver(this);
		broker.shutdown();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void update(Observable observable, Object obj)
	{
		if (obj instanceof RabbitMQTransportEvent)
		{
			RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
			notifyObservers(event.getStatus(), event.getDetails());
		}
	}
}
