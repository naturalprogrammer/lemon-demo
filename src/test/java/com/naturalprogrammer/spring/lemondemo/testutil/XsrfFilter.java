package com.naturalprogrammer.spring.lemondemo.testutil;

import org.apache.commons.lang3.StringUtils;

import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class XsrfFilter implements Filter {

	private String xsrfToken = null;
	
	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec, FilterContext ctx) {
		
		if (!requestSpec.getMethod().equals(Method.GET.name()) && // method not GET
		     xsrfToken != null ) {
			requestSpec.cookie("XSRF-TOKEN", xsrfToken);
			requestSpec.header("X-XSRF-TOKEN", xsrfToken);
		}
		
		final Response response = ctx.next(requestSpec, responseSpec);
		final String token = response.cookie(LemonSecurityConfig.XSRF_TOKEN_COOKIE_NAME);
		
		if (StringUtils.isNotBlank(token))
			xsrfToken = token;

		return response;
	}
}
