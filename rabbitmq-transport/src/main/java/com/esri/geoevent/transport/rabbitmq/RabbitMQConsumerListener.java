package com.esri.geoevent.transport.rabbitmq;

import net.jodah.lyra.event.ConsumerListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class RabbitMQConsumerListener extends RabbitMQObservable implements ConsumerListener
{
	@Override
	public void onRecoveryStarted(Consumer consumer, Channel channel)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_STARTED, "Consumer from channel(" + channel.getChannelNumber() + "): recovery started.", consumer, channel);
	}

	@Override
  public void onRecoveryCompleted(Consumer consumer, Channel channel) {
    notifyObservers(RabbitMQConnectionStatus.RECOVERY_COMPLETED, "Consumer from channel(" + channel.getChannelNumber() + "): recovery completed.", consumer, channel);
  }

	@Override
	public void onRecoveryFailure(Consumer consumer, Channel channel, Throwable failure)
	{
		notifyObservers(RabbitMQConnectionStatus.RECOVERY_FAILED, "Consumer from channel(" + channel.getChannelNumber() + ") recovery failed: " + failure.getMessage(), consumer, channel);
	}
}
