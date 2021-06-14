package me.fuguanghua.net.http.server.router;

import io.netty.util.internal.ObjectUtil;

import java.util.Map;

final class PathPattern {
	public static String removeSlashesAtBothEnds(String path) {
		ObjectUtil.checkNotNull(path, "path");

		if (path.isEmpty()) {
			return path;
		}

		int beginIndex = 0;
		while (beginIndex < path.length() && path.charAt(beginIndex) == '/') {
			beginIndex++;
		}
		if (beginIndex == path.length()) {
			return "";
		}

		int endIndex = path.length() - 1;
		while (endIndex > beginIndex && path.charAt(endIndex) == '/') {
			endIndex--;
		}

		return path.substring(beginIndex, endIndex + 1);
	}

	// --------------------------------------------------------------------------

	private final String pattern;
	private final String[] tokens;

	/**
	 * The pattern must not contain query, example:
	 * {@code constant1/constant2?foo=bar}.
	 *
	 * <p>The pattern will be stored without slashes at both ends.
	 */
	public PathPattern(String pattern) {
		if (pattern.contains("?")) {
			throw new IllegalArgumentException("Path pattern must not contain query");
		}

		this.pattern = removeSlashesAtBothEnds(ObjectUtil.checkNotNull(pattern, "pattern"));
		this.tokens = this.pattern.split("/");
	}

	/**
	 * Returns the pattern given at the constructor, without slashes at both ends.
	 */
	public String pattern() {
		return pattern;
	}

	/**
	 * Returns the pattern given at the constructor, without slashes at both ends,
	 * and split by {@code '/'}.
	 */
	public String[] tokens() {
		return tokens;
	}

	// --------------------------------------------------------------------------
	// Instances of this class can be conveniently used as Map keys.

	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof PathPattern)) {
			return false;
		}

		PathPattern otherPathPattern = (PathPattern) o;
		return pattern.equals(otherPathPattern.pattern);
	}

	// --------------------------------------------------------------------------

	/**
	 * {@code params} will be updated with params embedded in the request path.
	 *
	 * <p>This method signature is designed so that {@code requestPathTokens} and {@code params}
	 * can be created only once then reused, to optimize for performance when a
	 * large number of path patterns need to be matched.
	 *
	 * @return {@code false} if not matched; in this case params should be reset
	 */
	public boolean match(String[] requestPathTokens, Map<String, String> params) {
		if (tokens.length == requestPathTokens.length) {
			for (int i = 0; i < tokens.length; i++) {
				String key = tokens[i];
				String value = requestPathTokens[i];

				if (key.length() > 0 && key.charAt(0) == ':') {
					// This is a placeholder
					params.put(key.substring(1), value);
				} else if (!key.equals(value)) {
					// This is a constant
					return false;
				}
			}

			return true;
		}

		if (tokens.length > 0 && tokens[tokens.length - 1].equals(":*") && tokens.length <= requestPathTokens.length) {
			// The first part
			for (int i = 0; i < tokens.length - 2; i++) {
				String key = tokens[i];
				String value = requestPathTokens[i];

				if (key.length() > 0 && key.charAt(0) == ':') {
					// This is a placeholder
					params.put(key.substring(1), value);
				} else if (!key.equals(value)) {
					// This is a constant
					return false;
				}
			}

			// The last :* part
			StringBuilder b = new StringBuilder(requestPathTokens[tokens.length - 1]);
			for (int i = tokens.length; i < requestPathTokens.length; i++) {
				b.append('/');
				b.append(requestPathTokens[i]);
			}
			params.put("*", b.toString());

			return true;
		}

		return false;
	}
}
