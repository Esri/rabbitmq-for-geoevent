package com.esri.geoevent.transport.rabbitmq;

public enum RabbitMQConnectionStatus
{
  CREATED, CREATION_FAILED, RECOVERY, RECOVERY_STARTED, RECOVERY_COMPLETED, RECOVERY_FAILED, DISCONNECTED, SHUTDOWN
}
