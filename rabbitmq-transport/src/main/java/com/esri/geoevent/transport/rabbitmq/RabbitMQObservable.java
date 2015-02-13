package com.esri.geoevent.transport.rabbitmq;

import java.util.Observable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class RabbitMQObservable extends Observable
{
  private static final Log LOGGER = LogFactory.getLog(RabbitMQObservable.class);

	public void notifyObservers(RabbitMQConnectionStatus status, String details, Object... args)
	{
    LOGGER.debug(details);
		notifyObservers(new RabbitMQTransportEvent(status, details, args));
	}

  public void notifyObservers(Object event)
  {
    if (event != null)
    {
      setChanged();
      super.notifyObservers(event);
      clearChanged();
    }
  }
}
