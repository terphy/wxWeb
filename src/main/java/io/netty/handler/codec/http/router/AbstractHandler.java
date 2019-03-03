package io.netty.handler.codec.http.router;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * This class is designed so that it can also be used in other projects, like
 * Sinetja.
 */
@ChannelHandler.Sharable
public abstract class AbstractHandler<T, RouteLike extends MethodRouter<T, RouteLike>>
		extends SimpleChannelInboundHandler<HttpRequest> {
	private static final byte[] CONTENT_404 = "Not Found".getBytes();

	private final MethodRouter<T, RouteLike> router;

	public AbstractHandler(MethodRouter<T, RouteLike> router) {
		this.router = router;
	}

	public MethodRouter<T, RouteLike> router() {
		return router;
	}

	// ----------------------------------------------------------------------------

	/**
	 * @param routed
	 *            Will automatically be released. Please call routed.retain() if
	 *            you want.
	 */
	protected abstract void routed(ChannelHandlerContext ctx,
			MethodRouted<T> routed) throws Exception;

	protected void messageReceived(ChannelHandlerContext ctx, HttpRequest req)
			throws Exception {
		if (HttpHeaderUtil.is100ContinueExpected(req)) {
			ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.CONTINUE));
			return;
		}

		// Route
		HttpMethod method = req.method();
		QueryStringDecoder qsd = new QueryStringDecoder(req.uri());
		jauter.Routed<T> jrouted = router.route(method, qsd.path());

		if (jrouted == null) {
			respondNotFound(ctx, req);
			return;
		}

		MethodRouted<T> routed = new MethodRouted<T>(jrouted.target(),
				jrouted.notFound(), req, qsd.path(), jrouted.params(),
				qsd.parameters());
		routed(ctx, routed);
	}

	protected void respondNotFound(ChannelHandlerContext ctx, HttpRequest req) {
		HttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.NOT_FOUND,
				Unpooled.wrappedBuffer(CONTENT_404));

		HttpHeaders headers = res.headers();
		headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
		headers.setInt(HttpHeaderNames.CONTENT_LENGTH, CONTENT_404.length);

		KeepAliveWrite.flush(ctx, req, res);
	}
}
