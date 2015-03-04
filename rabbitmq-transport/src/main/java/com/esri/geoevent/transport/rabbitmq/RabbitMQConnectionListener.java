package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ConnectionListener;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Connection;

public class RabbitMQConnectionListener extends RabbitMQObservable implements ConnectionListener
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConnectionListener.class);

	@Override
	public void onCreate(Connection connection)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATED, LOGGER.translate("CONNECTION_ESTABLISH_SUCCESS", connection.getAddress().getCanonicalHostName()), connection);
	}

	@Override
	public void onCreateFailure(Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, LOGGER.translate("CONNECTION_ESTABLISH_FAILURE", "", failure.getMessage()));
	}

	@Override
	public void onRecovery(Connection connection)
	{

		notifyObservers(RabbitMQConnectionStatus.RECOVERY, LOGGER.translate("CONNECTION_RECOVERED", connection.getAddress().getCanonicalHostName()), connection);
	}

	@Override
	public void onRecoveryStarted(Connection connection)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, LOGGER.translate("CONNECTION_RECOVERY_STARTED", connection.getAddress().getCanonicalHostName()), connection);
	}

	@Override
	public void onRecoveryCompleted(Connection connection)
	{
		notifyObservers(new RabbitMQTransportEvent(RabbitMQConnectionStatus.RECOVERY_COMPLETED, LOGGER.translate("CONNECTION_RECOVERY_COMPLETED", connection.getAddress().getCanonicalHostName()), connection));
	}

	@Override
	public void onRecoveryFailure(Connection connection, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, LOGGER.translate("CONNECTION_RECOVERY_FAILED", connection.getAddress().getCanonicalHostName(), failure.getMessage()), connection);
	}
}
