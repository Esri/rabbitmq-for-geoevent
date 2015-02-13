package com.esri.geoevent.transport.rabbitmq;

public class RabbitMQTransportException extends Exception
{
	private static final long	serialVersionUID	= -1L;

	public RabbitMQTransportException(String message)
	{
		super(message);
	}

	public RabbitMQTransportException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
