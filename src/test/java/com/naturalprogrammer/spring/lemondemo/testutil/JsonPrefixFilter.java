package com.naturalprogrammer.spring.lemondemo.testutil;

import com.naturalprogrammer.spring.lemon.LemonAutoConfiguration;

import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class JsonPrefixFilter implements Filter {

	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec, FilterContext ctx) {

		final Response response = ctx.next(requestSpec, responseSpec); // Invoke the request by delegating to the next filter in the filter chain.
		String responseBody = response.asString();
		if (responseBody.startsWith(LemonAutoConfiguration.JSON_PREFIX)) {
			String updatedResponseBody = responseBody.substring(LemonAutoConfiguration.JSON_PREFIX.length());
			return new ResponseBuilder().clone(response).setBody(updatedResponseBody).build();	    	 
		}
		return response;
	}

}
