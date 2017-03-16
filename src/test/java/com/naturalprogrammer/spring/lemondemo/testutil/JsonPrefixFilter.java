package com.naturalprogrammer.spring.lemondemo.testutil;

import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import com.naturalprogrammer.spring.lemon.LemonConfig;

public class JsonPrefixFilter implements Filter {

	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec, FilterContext ctx) {

		final Response response = ctx.next(requestSpec, responseSpec); // Invoke the request by delegating to the next filter in the filter chain.
		String responseBody = response.asString();
		if (responseBody.startsWith(LemonConfig.JSON_PREFIX)) {
			String updatedResponseBody = responseBody.substring(LemonConfig.JSON_PREFIX.length());
			return new ResponseBuilder().clone(response).setBody(updatedResponseBody).build();	    	 
		}
		return response;
	}

}
