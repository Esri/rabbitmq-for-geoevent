package com.esri.geoevent.transport.rabbitmq;

public class RabbitMQTransportEvent
{
  private RabbitMQConnectionStatus status;
  private String details = "";
  private Object[] args;

  public RabbitMQTransportEvent(RabbitMQConnectionStatus status, String details, Object... args)
  {
    this.status = status;
    this.details = details;
    this.args = args;
  }

  public RabbitMQConnectionStatus getStatus()
  {
    return status;
  }

  public String getDetails()
  {
    return details;
  }

  public Object[] getArgs()
  {
    return args;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("RabbitMQTransportEvent(");
    sb.append(status);
    sb.append(", ");
    sb.append(details);
    sb.append(")");
    return sb.toString();
  }
}
