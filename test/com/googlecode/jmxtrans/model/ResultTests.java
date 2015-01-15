package com.googlecode.jmxtrans.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.jmxtrans.model.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class ResultTests {

	public static final String ATTRIBUTE_NAME = "domain:type=SomeType";
	public static final String CLASS_NAME = "SomeClass";
	public static final String CLASS_NAME_ALIAS = "SomeClassAlias";
	public static final String TYPE_NAME = "SomeType";
	public static final String KEY_NAME = "someKey";
	public static final Integer VAL_OBJ = new Integer(1);
	public static final long timestamp = System.currentTimeMillis();
	
	private StringBuilder sb;

	@Before
	public void createObjs() {
		sb = new StringBuilder();
		sb.append("Result [attributeName=");
		sb.append(ATTRIBUTE_NAME);
		sb.append(", className=");
		sb.append(CLASS_NAME);
		sb.append(", typeName=");
		sb.append(TYPE_NAME);
		sb.append(", values={");
		sb.append(KEY_NAME);
		sb.append("=");
		sb.append(VAL_OBJ);
		sb.append("}, epoch=");
		sb.append(timestamp);
		sb.append("]");
	}

	@Test
	public void resultToString() throws Exception {
		Map<String, Object> mapValues = new HashMap<String, Object>();
		mapValues.put(KEY_NAME,VAL_OBJ);
		Result res = new Result(timestamp, ATTRIBUTE_NAME, CLASS_NAME, CLASS_NAME_ALIAS, TYPE_NAME, mapValues);

		assertThat(res.toString()).isEqualTo(sb.toString());

	}

}
