package com.googlecode.jmxtrans.model.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.googlecode.jmxtrans.ConfigurationParser;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RabbitMQGraphiteWriterTests {

	@Test(expected = NullPointerException.class)
	public void hostIsRequired() throws ValidationException {
		try {
			RabbitMQGraphiteWriter.builder()
					.setPort(123)
          .setExchangeName("queuename")
					.build();
		} catch (NullPointerException npe) {
			assertThat(npe).hasMessage("Host cannot be null.");
			throw npe;
		}
	}

  @Test(expected = NullPointerException.class)
  public void portIsRequired() throws ValidationException {
    try {
      RabbitMQGraphiteWriter.builder()
              .setHost("localhost")
              .setExchangeName("queuename")
              .build();
    } catch (NullPointerException npe) {
      assertThat(npe).hasMessage("Port cannot be null.");
      throw npe;
    }
  }

  @Test(expected = NullPointerException.class)
  public void queuenameIsRequired() throws ValidationException {
    try {
      RabbitMQGraphiteWriter.builder()
              .setHost("localhost")
              .setPort(123)
              .build();
    } catch (NullPointerException npe) {
      assertThat(npe).hasMessage("queue_name cannot be null.");
      throw npe;
    }
  }
}
