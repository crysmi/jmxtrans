package com.googlecode.jmxtrans.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.jmxtrans.model.OutputWriter;
import com.googlecode.jmxtrans.model.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class QueryTests {

	public static final String MBEAN_NAME = "domain:type=SomeType";

	@Test
	public void queryToString() throws Exception {
		OutputWriter outputWriter = mock(OutputWriter.class);
		Query query = Query.builder()
				.setObj(MBEAN_NAME)
				.addAttr("DummyValue")
				.addOutputWriter(outputWriter)
				.build();

		assertThat(query.toString()).isEqualTo("Query [obj=domain:type=SomeType, resultAlias=null, attr=[DummyValue]]");

	}

}
