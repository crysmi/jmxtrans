package com.googlecode.jmxtrans.model.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;
import com.googlecode.jmxtrans.model.naming.KeyUtils;
import com.googlecode.jmxtrans.model.naming.StringUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Charsets.UTF_8;
import static com.googlecode.jmxtrans.model.PropertyResolver.resolveProps;

//RabbitMQ library imports
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

/**
 * This low latency and thread safe output writer sends data to a host/port combination
 * in the Graphite format.
 *
 * @see <a href="http://graphite.wikidot.com/getting-your-data-into-graphite">Getting your data into Graphite</a>
 *
 * @author jon
 */
@NotThreadSafe
public class RabbitMQGraphiteWriter extends BaseOutputWriter {
	private static final Logger log = LoggerFactory.getLogger(RabbitMQGraphiteWriter.class);

  private static final String DEFAULT_ROOT_PREFIX = "servers";
  private static final String DEFAULT_ROUTING_KEY = "jmxtrans";

  private final String rootPrefix;
  private final String exchange_name;
  private final String routing_key;


  private ConnectionFactory factory;
  private Channel channel;

	@JsonCreator
	public RabbitMQGraphiteWriter(
			@JsonProperty("typeNames") ImmutableList<String> typeNames,
			@JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled,
			@JsonProperty("rootPrefix") String rootPrefix,
      @JsonProperty("exchange_name") String exchange_name,
      @JsonProperty("routing_key") String routing_key,
      @JsonProperty("username") String username,
      @JsonProperty("password") String password,
      @JsonProperty("host") String host,
			@JsonProperty("port") Integer port,
			@JsonProperty("settings") Map<String, Object> settings) {
		super(typeNames, booleanAsNumber, debugEnabled, settings);
    this.rootPrefix = resolveProps(
            firstNonNull(
                    rootPrefix,
                    (String) getSettings().get("rootPrefix"),
                    DEFAULT_ROOT_PREFIX));
		host = resolveProps(host);
		if (host == null) {
			host = (String) getSettings().get(HOST);
		}
		if (host == null) {
			throw new NullPointerException("Host cannot be null.");
		}
		if (port == null) {
			port = Settings.getIntegerSetting(getSettings(), PORT, null);
		}
		if (port == null) {
			throw new NullPointerException("Port cannot be null.");
		}
    this.exchange_name = resolveProps(exchange_name);
    if (this.exchange_name == null) {
      throw new NullPointerException("queue_name cannot be null.");
    }
    this.routing_key = resolveProps(
            firstNonNull(
                    routing_key,
                    (String) getSettings().get("routing_key"),
                    DEFAULT_ROUTING_KEY));

    try {
      this.factory = createRabbitFactory(host, port, username, password);
      this.channel = createRabbitChannel(this.exchange_name);
    } catch (IOException e) {
      e.printStackTrace();
      throw new NullPointerException("factory/channel could not be created.");
    }


  }

  private ConnectionFactory createRabbitFactory(String host, Integer port, String username, String password) throws IOException {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);

    if(username != null) {
      factory.setUsername(username);
      factory.setUsername(password);
    }
    return factory;
  }

  private Channel createRabbitChannel(String exchange_name) throws IOException {

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(exchange_name, "topic", true);
    return channel;
  }

  private void writeRabbit(String message) throws IOException {
    channel.basicPublish(exchange_name, routing_key, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
  }

	public void validateSetup(Server server, Query query) throws ValidationException {
	}

	public void internalWrite(Server server, Query query, ImmutableList<Result> results) throws Exception {
		Socket socket = null;
		PrintWriter writer = null;

    List<String> typeNames = this.getTypeNames();

    for (Result result : results) {
      log.debug("Query result: {}", result);
      Map<String, Object> resultValues = result.getValues();
      if (resultValues != null) {
        for (Entry<String, Object> values : resultValues.entrySet()) {
          Object value = values.getValue();
          if (NumberUtils.isNumeric(value)) {

            String line = KeyUtils.getKeyString(server, query, result, values, typeNames, rootPrefix)
                .replaceAll("[()]", "_") + " " + value.toString() + " "
                + result.getEpoch() / 1000 + "\n";
            log.debug("RabbitMQGraphite Message: {}", line);
            writeRabbit(line);
          } else {
            log.warn("Unable to submit non-numeric value to Graphite: [{}] from result [{}]", value, result);
          }
        }
      }
    }
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final ImmutableList.Builder<String> typeNames = ImmutableList.builder();
		private boolean booleanAsNumber;
		private Boolean debugEnabled;
    private String exchange_name;
    private String routing_key;
    private String username;
    private String password;
    private String rootPrefix;
		private String host;
		private Integer port;

		private Builder() {}

		public Builder addTypeNames(List<String> typeNames) {
			this.typeNames.addAll(typeNames);
			return this;
		}

		public Builder addTypeName(String typeName) {
			typeNames.add(typeName);
			return this;
		}

		public Builder setBooleanAsNumber(boolean booleanAsNumber) {
			this.booleanAsNumber = booleanAsNumber;
			return this;
		}

		public Builder setDebugEnabled(boolean debugEnabled) {
			this.debugEnabled = debugEnabled;
			return this;
		}

		public Builder setRootPrefix(String rootPrefix) {
			this.rootPrefix = rootPrefix;
			return this;
		}

    public Builder setExchangeName(String exchange_name) {
      this.exchange_name = exchange_name;
      return this;
    }

    public Builder setRoutingKey(String routing_key) {
      this.routing_key = routing_key;
      return this;
    }

    public Builder setUsername(String username) {
      this.username = username;
      return this;
    }

    public Builder setPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder setHost(String host) {
      this.host = host;
      return this;
    }

		public Builder setPort(int port) {
			this.port  = port;
			return this;
		}

		public RabbitMQGraphiteWriter build() {
			return new RabbitMQGraphiteWriter(
					typeNames.build(),
					booleanAsNumber,
					debugEnabled,
					rootPrefix,
          exchange_name,
          routing_key,
          username,
          password,
					host,
					port,
					null
			);
		}
	}
}
