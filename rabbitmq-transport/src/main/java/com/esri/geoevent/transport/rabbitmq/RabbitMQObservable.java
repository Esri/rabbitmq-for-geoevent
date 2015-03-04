package com.esri.geoevent.transport.rabbitmq;

import java.util.Observable;

public abstract class RabbitMQObservable extends Observable
{
  public void notifyObservers(RabbitMQConnectionStatus status, String details, Object... args)
	{
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
