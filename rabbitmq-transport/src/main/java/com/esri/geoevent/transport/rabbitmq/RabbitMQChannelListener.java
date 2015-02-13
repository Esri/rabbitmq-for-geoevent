package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ChannelListener;

import com.rabbitmq.client.Channel;

public class RabbitMQChannelListener extends RabbitMQObservable implements ChannelListener
{
	@Override
	public void onCreate(Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATED, "Channel(" + channel.getChannelNumber() + ") created.", channel);
	}

	@Override
	public void onCreateFailure(Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, "Channel creation failed: " + failure.getMessage());
	}

	@Override
	public void onRecovery(Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY, "Channel(" + channel.getChannelNumber() + ") recovery.", channel);
	}

	@Override
	public void onRecoveryStarted(Channel channel)
	{
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, "Channel(" + channel.getChannelNumber() + ") recovery started.", channel);
	}

	@Override
	public void onRecoveryCompleted(Channel channel)
	{
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_COMPLETED, "Channel(" + channel.getChannelNumber() + ") recovery completed.", channel);
	}

	@Override
	public void onRecoveryFailure(Channel channel, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, "Channel(" + channel.getChannelNumber() + ") recovery failed: " + failure.getMessage(), channel);
	}
}
