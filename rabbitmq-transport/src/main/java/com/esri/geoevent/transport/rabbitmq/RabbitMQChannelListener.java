package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ChannelListener;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.rabbitmq.client.Channel;

public class RabbitMQChannelListener extends RabbitMQObservable implements ChannelListener
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQChannelListener.class);

	@Override
	public void onCreate(Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATED, LOGGER.translate("CHANNEL_CREATED", channel.getChannelNumber()), channel);
	}

	@Override
	public void onCreateFailure(Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.CREATION_FAILED, LOGGER.translate("CHANNEL_CREATION_FAILED", failure.getMessage()));
	}

	@Override
	public void onRecovery(Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY, LOGGER.translate("CHANNEL_RECOVERED", channel.getChannelNumber()), channel);
	}

	@Override
	public void onRecoveryStarted(Channel channel)
	{
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, LOGGER.translate("CHANNEL_RECOVERY_STARTED", channel.getChannelNumber()), channel);
	}

	@Override
	public void onRecoveryCompleted(Channel channel)
	{
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_COMPLETED, LOGGER.translate("CHANNEL_RECOVERY_COMPLETED", channel.getChannelNumber()), channel);
	}

	@Override
	public void onRecoveryFailure(Channel channel, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, LOGGER.translate("CHANNEL_RECOVERY_FAILED", channel.getChannelNumber(), failure.getMessage()), channel);
	}
}
