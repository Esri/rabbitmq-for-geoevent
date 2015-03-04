package com.esri.geoevent.transport.rabbitmq;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import net.jodah.lyra.event.ConsumerListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class RabbitMQConsumerListener extends RabbitMQObservable implements ConsumerListener
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConsumerListener.class);

	@Override
	public void onRecoveryStarted(Consumer consumer, Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, LOGGER.translate("CONSUMER_RECOVERY_STARTED", channel.getChannelNumber()), consumer, channel);
	}

	@Override
  public void onRecoveryCompleted(Consumer consumer, Channel channel)
  {
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_COMPLETED, LOGGER.translate("CONSUMER_RECOVERY_COMPLETED", channel.getChannelNumber()), consumer, channel);
  }

	@Override
	public void onRecoveryFailure(Consumer consumer, Channel channel, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, LOGGER.translate("CONSUMER_RECOVERY_FAILED", channel.getChannelNumber(), failure.getMessage()), consumer, channel);
	}
}
