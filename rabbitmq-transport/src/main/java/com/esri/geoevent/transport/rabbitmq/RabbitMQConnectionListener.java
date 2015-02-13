package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ConnectionListener;

import com.rabbitmq.client.Connection;

public class RabbitMQConnectionListener extends RabbitMQObservable implements ConnectionListener
{
	@Override
	public void onCreate(Connection connection)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATED, "Connection to rabbit@" + connection.getAddress().getCanonicalHostName() + " established.", connection);
	}

	@Override
	public void onCreateFailure(Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, "Connection to RabbitMQ failed: " + failure.getMessage());
	}

	@Override
	public void onRecovery(Connection connection)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY, "Connection to rabbit@" + connection.getAddress().getCanonicalHostName() + " recovered.", connection);
	}

	@Override
	public void onRecoveryStarted(Connection connection)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, "Connection to rabbit@" + connection.getAddress().getCanonicalHostName() + " started recovering.", connection);
	}

	@Override
	public void onRecoveryCompleted(Connection connection)
	{
		notifyObservers(new RabbitMQTransportEvent(RabbitMQConnectionStatus.RECOVERY_FAILED, "Connection to rabbit@" + connection.getAddress().getCanonicalHostName() + " completed recovering.", connection));
	}

	@Override
	public void onRecoveryFailure(Connection connection, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, "Connection to rabbit@" + connection.getAddress().getCanonicalHostName() + " recovery failed: " + failure.getMessage(), connection);
	}
}
