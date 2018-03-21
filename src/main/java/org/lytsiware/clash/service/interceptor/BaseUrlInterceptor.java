package org.lytsiware.clash.service.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Deprecated
public class BaseUrlInterceptor extends HandlerInterceptorAdapter {

    //	@Name("${base.url}")
    String baseUrl;
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView == null) {
			modelAndView = new ModelAndView();
		}
		modelAndView.addObject("baseUrl", baseUrl);
		
	}

}
