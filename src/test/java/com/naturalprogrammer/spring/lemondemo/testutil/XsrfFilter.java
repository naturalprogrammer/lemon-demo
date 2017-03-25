package com.naturalprogrammer.spring.lemondemo.testutil;

import org.apache.commons.lang3.StringUtils;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.internal.http.Method;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import com.naturalprogrammer.spring.lemon.security.LemonSecurityConfig;

public class XsrfFilter implements Filter {

	private String xsrfToken = null;
	
	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec, FilterContext ctx) {
		
		if (!requestSpec.getMethod().name().equals(Method.GET.name()) && // method not GET
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
