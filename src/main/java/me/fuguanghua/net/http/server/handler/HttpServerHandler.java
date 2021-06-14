package me.fuguanghua.net.http.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import me.fuguanghua.net.Lynx;
import me.fuguanghua.net.http.server.ControllerFactory;
import me.fuguanghua.net.http.server.HttpController;
import me.fuguanghua.net.http.server.annotation.Intercept;
import me.fuguanghua.net.http.server.annotation.UrlRouter;
import me.fuguanghua.net.http.server.filter.BeforeFilter;
import me.fuguanghua.net.http.server.router.RouteAction;
import me.fuguanghua.net.http.server.router.RouteResult;
import me.fuguanghua.net.http.server.router.Router;
import me.fuguanghua.net.utils.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;

@ChannelHandler.Sharable
public class HttpServerHandler extends BaseServerHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);

	private Router<RouteAction> routers = new Router<>();

	private ControllerFactory controllerFactory;
	private boolean enableCookies;

	public HttpServerHandler(ControllerFactory controllerFactory, boolean enableCookies) {
		this.controllerFactory = controllerFactory;
		this.enableCookies = enableCookies;
		scanRouters();
	}

	private BeforeFilter[] getBeforeFilter(Intercept intercept) {
		if (intercept == null) {
			return new BeforeFilter[0];
		}

		Class<? extends BeforeFilter>[] filter = intercept.value();
		if (filter == null) {
			return new BeforeFilter[0];
		}

		BeforeFilter[] filters = new BeforeFilter[filter.length];
		for (int i = 0; i < filters.length; i++) {
			try {
				filters[i] = filter[i].newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return filters;
	}

	void scanController(Lynx lynx) {
		Collection<Class<? extends HttpController>> classes = PathResolver.scanPkgWithFather(HttpController.class, lynx.packagesName());
		for (Class c : classes) {
			if (c.isInterface()) {
				continue;
			}
			addRoute(c);
		}
	}

	private void addRoute(Class<? extends HttpController> clazz) {
		Intercept controllerIntercept = clazz.getAnnotation(Intercept.class);
		BeforeFilter[] controllerFilters = getBeforeFilter(controllerIntercept);
		Method[] methodList = clazz.getDeclaredMethods();
		for (Method m : methodList) {
			m.setAccessible(true);
			UrlRouter urlRouter = m.getAnnotation(UrlRouter.class);
			if (urlRouter != null) {
				Intercept actionIntercept = m.getAnnotation(Intercept.class);
				BeforeFilter[] actionFilters = getBeforeFilter(actionIntercept);
				RouteAction action = RouteAction.valueOf(clazz, m, actionFilters, controllerFilters);

				String[] paths = urlRouter.path();
				for (String p : paths) {
					if (urlRouter.post()) {
						routers.POST(p, action);
					}
					if (urlRouter.get()) {
						routers.GET(p, action);
					}
				}
			}
		}
	}

	private void scanRouters() {
		for (Class<? extends HttpController> clazz : controllerFactory.getControllers()) {
			addRoute(clazz);
		}
		LOGGER.info("scan controller is completed! router size:{}", routers.size());
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
		if (req.uri().equals("/favicon.ico")) {
			writeHttpStatus(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		runController(ctx, req);
	}

	private void runController(ChannelHandlerContext ctx, FullHttpRequest req) {
		final RouteResult<RouteAction> routeResult = routers.route(req.method(), req.uri());
		if (routeResult == null) {
			writeHttpStatus(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}

		try {
			final HttpController instance = controllerFactory.newInstance(routeResult.target().controllerClazz);
			instance.init(ctx.channel(), req, routeResult, this.enableCookies);

			boolean filterResult = true;
			final BeforeFilter[] controllerFilters = routeResult.target().controllerFilters;
			for (BeforeFilter filter : controllerFilters) {
				filterResult = filter.execute(instance, routeResult.target().method);
				if (!filterResult) {
					break;
				}
			}

			BeforeFilter[] actionFilters = routeResult.target().actionFilters;
			for (BeforeFilter filter : actionFilters) {
				filterResult = filter.execute(instance, routeResult.target().method);
				if (!filterResult) {
					break;
				}
			}

			if (filterResult) {
				routeResult.target().method.invoke(instance);
			}
		} catch (Exception ex) {
			LOGGER.error("{}", ex);
			writeHttpStatus(ctx, HttpResponseStatus.BAD_REQUEST);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) {
			//LOGGER.error("{}", cause.getMessage());
		} else {
			LOGGER.error("{}", cause);
		}
		ctx.close();
	}
}
