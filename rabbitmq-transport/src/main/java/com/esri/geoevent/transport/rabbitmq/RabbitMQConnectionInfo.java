package com.esri.geoevent.transport.rabbitmq;

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;

public class RabbitMQConnectionInfo implements Validatable
{
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(RabbitMQConnectionInfo.class);
	private String	host			= "localhost";
	private int			port			= 5672;
	private String	username	= null;
	private String	password	= null;
	private boolean	ssl				= true;

	public RabbitMQConnectionInfo(String host, String port, String username, String password, String ssl)
	{
		this.host = host;
		this.port = Converter.convertToInteger(port, 5672);
		this.username = username;
		this.password = password;
		this.ssl = Converter.convertToBoolean(ssl, false);
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public boolean isSsl()
	{
		return ssl;
	}

	@Override
	public void validate() throws ValidationException
	{
		if (host == null || host.isEmpty())
      throw new ValidationException(LOGGER.translate("CONNECTION_HOST_VALIDATE_ERROR"));
		if (port <= 0)
      throw new ValidationException(LOGGER.translate("CONNECTION_PORT_VALIDATE_ERROR"));
	}
}
