package me.fuguanghua.net.http.server.router;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

final class PatternRouter<T> {
	private static final InternalLogger log = InternalLoggerFactory.getInstance(PatternRouter.class);

	// A path pattern can only point to one target
	private final Map<PathPattern, T> routes = new HashMap<>();

	// Reverse index to create reverse routes fast (a target can have multiple
	// path patterns)
	private final Map<T, Set<PathPattern>> reverseRoutes = new HashMap<>();

	// --------------------------------------------------------------------------

	/**
	 * Returns all routes in this router, an unmodifiable map of {@code PathPattern -> Target}.
	 */
	public Map<PathPattern, T> routes() {
		return Collections.unmodifiableMap(routes);
	}

	/**
	 * This method does nothing if the path pattern has already been added.
	 * A path pattern can only point to one target.
	 */
	public PatternRouter<T> addRoute(String pathPattern, T target) {
		PathPattern p = new PathPattern(pathPattern);
		if (routes.containsKey(p)) {
			return this;
		}

		routes.put(p, target);
		addReverseRoute(target, p);
		return this;
	}

	private void addReverseRoute(T target, PathPattern pathPattern) {
		Set<PathPattern> patterns = reverseRoutes.get(target);
		if (patterns == null) {
			patterns = new HashSet<>();
			patterns.add(pathPattern);
			reverseRoutes.put(target, patterns);
		} else {
			patterns.add(pathPattern);
		}
	}

	/**
	 * @return {@code null} if no match
	 */
	public RouteResult<T> route(String uri, String decodedPath, String[] pathTokens) {
		// Optimize: reuse requestPathTokens and pathParams in the loop
		Map<String, String> pathParams = new HashMap<>();
		for (Map.Entry<PathPattern, T> entry : routes.entrySet()) {
			PathPattern pattern = entry.getKey();
			if (pattern.match(pathTokens, pathParams)) {
				T target = entry.getValue();
				return new RouteResult<>(uri, decodedPath, pathParams, Collections.emptyMap(), target);
			}

			// Reset for the next try
			pathParams.clear();
		}

		return null;
	}

	/**
	 * Checks if there's any matching route.
	 */
	public boolean anyMatched(String[] requestPathTokens) {
		Map<String, String> pathParams = new HashMap<>();
		for (PathPattern pattern : routes.keySet()) {
			if (pattern.match(requestPathTokens, pathParams)) {
				return true;
			}

			// Reset for the next loop
			pathParams.clear();
		}

		return false;
	}

	/**
	 * Given a target and params, this method tries to do the reverse routing
	 * and returns the URI.
	 *
	 * <p>Placeholders in the path pattern will be filled with the params.
	 * The params can be a map of {@code placeholder name -> value}
	 * or ordered values.
	 *
	 * <p>If a param doesn't have a corresponding placeholder, it will be put
	 * to the query part of the result URI.
	 *
	 * @return {@code null} if there's no match
	 */
	@SuppressWarnings("unchecked")
	public String uri(T target, Object... params) {
		if (params.length == 0) {
			return uri(target, Collections.emptyMap());
		}

		if (params.length == 1 && params[0] instanceof Map<?, ?>) {
			return pathMap(target, (Map<Object, Object>) params[0]);
		}

		if (params.length % 2 == 1) {
			throw new IllegalArgumentException("Missing value for param: " + params[params.length - 1]);
		}

		Map<Object, Object> map = new HashMap<Object, Object>(params.length / 2);
		for (int i = 0; i < params.length; i += 2) {
			String key = params[i].toString();
			String value = params[i + 1].toString();
			map.put(key, value);
		}
		return pathMap(target, map);
	}

	/**
	 * @return {@code null} if there's no match, or the params can't be UTF-8 encoded
	 */
	private String pathMap(T target, Map<Object, Object> params) {
		Set<PathPattern> patterns = reverseRoutes.get(target);
		if (patterns == null) {
			return null;
		}

		try {
			// The best one is the one with minimum number of params in the
			// query
			String bestCandidate = null;
			int minQueryParams = Integer.MAX_VALUE;

			boolean matched = true;
			Set<String> usedKeys = new HashSet<String>();

			for (PathPattern pattern : patterns) {
				matched = true;
				usedKeys.clear();

				// "+ 16": Just in case the part befor that is 0
				int initialCapacity = pattern.pattern().length() + 20 * params.size() + 16;
				StringBuilder b = new StringBuilder(initialCapacity);

				for (String token : pattern.tokens()) {
					b.append('/');

					if (token.length() > 0 && token.charAt(0) == ':') {
						String key = token.substring(1);
						Object value = params.get(key);
						if (value == null) {
							matched = false;
							break;
						}

						usedKeys.add(key);
						b.append(value.toString());
					} else {
						b.append(token);
					}
				}

				if (matched) {
					int numQueryParams = params.size() - usedKeys.size();
					if (numQueryParams < minQueryParams) {
						if (numQueryParams > 0) {
							boolean firstQueryParam = true;

							for (Map.Entry<Object, Object> entry : params.entrySet()) {
								String key = entry.getKey().toString();
								if (!usedKeys.contains(key)) {
									if (firstQueryParam) {
										b.append('?');
										firstQueryParam = false;
									} else {
										b.append('&');
									}

									String value = entry.getValue().toString();

									// May throw UnsupportedEncodingException
									b.append(URLEncoder.encode(key, "UTF-8"));

									b.append('=');

									// May throw UnsupportedEncodingException
									b.append(URLEncoder.encode(value, "UTF-8"));
								}
							}
						}

						bestCandidate = b.toString();
						minQueryParams = numQueryParams;
					}
				}
			}

			return bestCandidate;
		} catch (UnsupportedEncodingException e) {
			log.warn("Params can't be UTF-8 encoded: " + params);
			return null;
		}
	}
}
