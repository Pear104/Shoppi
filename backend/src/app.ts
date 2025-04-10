import Fastify, { FastifyInstance } from "fastify";
import jwt from "@fastify/jwt";
import cors from "@fastify/cors";
import multipart from "@fastify/multipart";
import fastifySwagger from "@fastify/swagger";
import fastifySwaggerUi from "@fastify/swagger-ui";
import socketPlugin from "./plugins/socket";

// import { userRoutes } from "./routes/user.routes";
import { authRoutes } from "./routes/auth.routes";
import { productRoutes } from "./routes/product.routes";
import { categoryRoutes } from "./routes/category.routes";
import { cartRoutes } from "./routes/cart.routes";
import { orderRoutes } from "./routes/order.routes";
import { chatRoutes } from "./routes/chat.routes";
import { reviewRoutes } from "./routes/review.routes";
// import { chatRoutes } from "./routes/chat.routes";

export const app: FastifyInstance = Fastify({
  logger: true,
});

// Register plugins
app.register(cors, {
  origin: "*",
});

app.register(jwt, {
  secret: process.env.JWT_SECRET || "supersecretkey",
});

app.register(multipart, {
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB
  },
});

// Swagger documentation
app.register(fastifySwagger, {
  swagger: {
    info: {
      title: "Shop API",
      description: "API documentation for the Shop backend",
      version: "1.0.0",
    },
    securityDefinitions: {
      bearerAuth: {
        type: "apiKey",
        name: "Authorization",
        in: "header",
      },
    },
  },
});

app.register(fastifySwaggerUi, {
  routePrefix: "/documentation",
});

// Register Socket.IO plugin
app.register(socketPlugin);

// Register routes
app.register(authRoutes, { prefix: "/api/auth" });
// app.register(userRoutes, { prefix: "/api/users" });
app.register(productRoutes, { prefix: "/api/products" });
app.register(categoryRoutes, { prefix: "/api/categories" });
app.register(cartRoutes, { prefix: "/api/cart" });
app.register(orderRoutes, { prefix: "/api/orders" });
app.register(chatRoutes, { prefix: "/api/chat" });
app.register(reviewRoutes, { prefix: "/api/reviews" });
// app.register(chatRoutes, { prefix: "/api/chat" });

// Health check route
app.get("/health", async () => {
  return { status: "ok" };
});

// Error handler
app.setErrorHandler((error, request, reply) => {
  app.log.error(error);

  reply.status(error.statusCode || 500).send({
    error: {
      message: error.message || "Internal Server Error",
      statusCode: error.statusCode || 500,
    },
  });
});
