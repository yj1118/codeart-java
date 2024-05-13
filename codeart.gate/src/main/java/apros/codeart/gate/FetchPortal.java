package apros.codeart.gate;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class FetchPortal {

	public static void open() {

		Vertx vertx = Vertx.vertx(); // 创建 Vert.x 实例
		HttpServer server = vertx.createHttpServer(); // 创建服务器

		Router router = Router.router(vertx); // 创建路由
		router.route().handler(BodyHandler.create()); // 处理请求体

		// 定义一个简单的路由
		router.get("/").handler(rc -> rc.response().end("Hello Vert.x!"));

		// 启动服务器并监听8080端口
		server.requestHandler(router).listen(8080, result -> {
			if (result.succeeded()) {
				System.out.println("Server is now listening on port 8080!");
			} else {
				System.out.println("Failed to bind!");
			}
		});
	}
}
