package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicies;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.*;

public class RabbitMQConnectionBroker extends RabbitMQObservable implements Observer
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConnectionBroker.class);
	private Connection									connection;
	private RabbitMQConnectionListener	connectionListener;
	private RabbitMQChannelListener			channelListener;
	private RabbitMQConsumerListener		consumerListener;
	private RabbitMQConnectionMonitor   monitor;
	private int timeout = 5000;

	public RabbitMQConnectionBroker(RabbitMQConnectionInfo connectionInfo)
	{
		connectionListener = new RabbitMQConnectionListener();
		connectionListener.addObserver(this);
		channelListener = new RabbitMQChannelListener();
		channelListener.addObserver(this);
		consumerListener = new RabbitMQConsumerListener();
		consumerListener.addObserver(this);
		monitor = new RabbitMQConnectionMonitor(connectionInfo);
		monitor.addObserver(this);
		new Thread(monitor).start();
	}

	public Channel createChannel() throws RabbitMQTransportException
	{
		if (isConnected())
		{
			try
			{
				return connection.createChannel();
			}
			catch (IOException e)
			{
        String msg = LOGGER.translate("CHANNEL_CREATE_ERROR", e.getMessage());
        LOGGER.error(msg, e);
				throw new RabbitMQTransportException(msg);
			}
		}
    String cause = LOGGER.translate("CONNECTION_BROKEN_ERROR", monitor.connectionInfo.getHost());
    String msg = LOGGER.translate("CHANNEL_CREATE_ERROR", cause);
		LOGGER.error(msg);
		throw new RabbitMQTransportException(msg);
	}

	public boolean isConnected()
	{
		return connection != null && connection.isOpen();
	}

	public void shutdown()
	{
		monitor.deleteObserver(this);
		monitor.stop();
		connectionListener.deleteObserver(this);
		channelListener.deleteObserver(this);
		consumerListener.deleteObserver(this);
		if (isConnected())
		{
			try
			{
				connection.close(timeout);
			}
			catch (IOException e)
			{
        String msg = LOGGER.translate("CONNECTION_CLOSE_ERROR", monitor.connectionInfo.getHost(), e.getMessage());
        LOGGER.error(msg, e);
			}
			finally
			{
				connection = null;
			}
		}
	}

	@Override
	public void update(Observable observable, Object obj)
	{
    if (obj instanceof RabbitMQTransportEvent)
    {
      RabbitMQTransportEvent event = (RabbitMQTransportEvent) obj;
      notifyObservers(event.getStatus(), event.getDetails());
    }
	}

  public RabbitMQConnectionInfo getConnectionInfo()
  {
    return monitor.connectionInfo;
  }

	private class RabbitMQConnectionMonitor extends RabbitMQObservable implements Runnable
	{
		private RabbitMQConnectionInfo 	connectionInfo;
		private volatile boolean				running    = false;
		private volatile boolean				errorState = false;

		public RabbitMQConnectionMonitor(RabbitMQConnectionInfo	connectionInfo)
		{
			this.connectionInfo = connectionInfo;
		}

    @Override
		public void run()
		{
			running = true;
			while (running)
			{
				if (!isConnected())
				{
					try
					{
						ConnectionFactory factory = new ConnectionFactory();
						factory.setHost(connectionInfo.getHost());
						factory.setPort(connectionInfo.getPort());
						if (connectionInfo.isSsl())
							factory.useSslProtocol();
						if (connectionInfo.getUsername() != null && connectionInfo.getPassword() != null)
						{
							factory.setUsername(connectionInfo.getUsername());
							factory.setPassword(connectionInfo.getPassword());
						}
						ConnectionOptions options = new ConnectionOptions().withConnectionFactory(factory);
						Config config = new Config().withRecoveryPolicy(RecoveryPolicies.recoverAlways()).withChannelListeners(channelListener).withConnectionListeners(connectionListener).withConsumerListeners(consumerListener).withConsumerRecovery(true);
						connection = Connections.create(options, config);
						connection.addShutdownListener(new ShutdownListener()
						{
							@Override
							public void shutdownCompleted(ShutdownSignalException cause)
							{
								LOGGER.error("CONNECTION_BROKEN_WITH_CAUSE_ERROR", connectionInfo.getHost(), cause.getMessage());
								notifyObservers(RabbitMQConnectionStatus.DISCONNECTED, cause.getMessage());
							}
						});
						errorState = false;
            String msg = LOGGER.translate("CONNECTION_ESTABLISH_SUCCESS", connectionInfo.getHost());
						LOGGER.info(msg);
						notifyObservers(RabbitMQConnectionStatus.CREATED, msg);
					}
					catch (Throwable th)
					{
						// only log the error message once
						if (!errorState)
						{
              String msg = LOGGER.translate("CONNECTION_ESTABLISH_FAILURE", connectionInfo.getHost(), th.getMessage());
							LOGGER.error(msg, th);
							notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, msg);
							errorState = true;
						}
					}
				}
				sleep();
			}
		}

		private void sleep()
		{
			try
			{
				Thread.sleep(timeout);
			}
			catch (InterruptedException e)
			{
				;
			}
		}

    public void stop()
		{
			running = false;
		}
	}

  public abstract static class RabbitMQComponentBase extends RabbitMQObservable implements Observer
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
        details = LOGGER.translate("CONNECTION_BROKEN_ERROR", broker.monitor.connectionInfo.getHost());
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
              String msg = LOGGER.translate("CHANNEL_CLOSE_ERROR", e.getMessage());
              LOGGER.error(msg, e);
            }
          }
          channel = null;
        }
      }
      connected = false;
      details = reason;
    }

    public void shutdown(String reason)
    {
      disconnect(reason);
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
}
