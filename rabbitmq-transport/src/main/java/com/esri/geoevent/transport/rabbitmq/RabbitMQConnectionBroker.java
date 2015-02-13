package com.esri.geoevent.transport.rabbitmq;

import java.io.IOException;
import java.util.*;

import com.rabbitmq.client.*;
import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.Connections;
import net.jodah.lyra.config.Config;
import net.jodah.lyra.config.RecoveryPolicies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RabbitMQConnectionBroker extends RabbitMQObservable implements Observer
{
	private static final Log						LOGGER = LogFactory.getLog(RabbitMQConnectionBroker.class);
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
				String msg = "RabbitMQ connection broker failed to create new channel: " + e.getMessage();
				LOGGER.error(msg);
				throw new RabbitMQTransportException(msg, e);
			}
		}
		String msg = "RabbitMQ connection broker failed to create new channel: connection is broken.";
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
				LOGGER.error("RabbitMQ connection broker failed to close connection on shutdown: " + e.getMessage(), e);
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
			switch (event.getStatus())
			{
				case RECOVERY:
          LOGGER.error(event.getDetails());
          notifyObservers(event.getStatus(), event.getDetails());
					break;
				case RECOVERY_STARTED:
					break;
				case RECOVERY_COMPLETED:
					break;
				case RECOVERY_FAILED:
					break;
				case CREATED:
					break;
				case CREATION_FAILED:
					LOGGER.error(event.getDetails());
					notifyObservers(event.getStatus(), event.getDetails());
					break;
				case DISCONNECTED:
					LOGGER.info(event.getDetails());
					notifyObservers(event.getStatus(), event.getDetails());
					break;
				case SHUTDOWN:
					break;
				default:
					break;
			}
		}
	}

	private class RabbitMQConnectionMonitor extends RabbitMQObservable implements Runnable
	{
		private RabbitMQConnectionInfo 	connectionInfo;
		private volatile boolean				running    = false;
		private volatile boolean				errorState = false;

		public RabbitMQConnectionMonitor(RabbitMQConnectionInfo 	connectionInfo)
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
								LOGGER.error("Connection to rabbit@" + connectionInfo.getHost() + " broken.");
								notifyObservers(RabbitMQConnectionStatus.DISCONNECTED, cause.getMessage());
							}
						});
						errorState = false;
						String details = "Connection to rabbit@" + connectionInfo.getHost() + " established.";
						LOGGER.info(details);
						notifyObservers(RabbitMQConnectionStatus.CREATED, details);
					}
					catch (Throwable th)
					{
						// only log the error message once
						if (!errorState)
						{
							String details = "Connection to rabbit@" + connectionInfo.getHost() + " cannot be established: " + th.getMessage();
							LOGGER.error(details, th);
							notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, details);
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
}
