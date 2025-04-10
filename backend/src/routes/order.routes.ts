import { FastifyInstance } from "fastify";
import {
  createOrder,
  getAllOrders,
  getOrderById,
  updateOrderStatus,
  cancelOrder,
  getDashboardStats,
  getRevenueData,
} from "../controllers/order.controller";
import { authenticate, authorizeAdmin } from "../middleware/auth.middleware";

export const orderRoutes = async (fastify: FastifyInstance) => {
  // All order routes require authentication
  fastify.addHook("onRequest", authenticate);

  // Create order
  fastify.post(
    "/",
    {
      schema: {
        tags: ["Orders"],
        security: [{ bearerAuth: [] }],
        body: {
          type: "object",
          required: ["address", "phoneNumber"],
          properties: {
            address: { type: "string" },
            phoneNumber: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              userId: { type: "string" },
              status: { type: "string" },
              totalAmount: { type: "number" },
              address: { type: "string" },
              phoneNumber: { type: "string" },
              items: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    quantity: { type: "integer" },
                    price: { type: "number" },
                    product: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        name: { type: "string" },
                        description: { type: "string" },
                        price: { type: "number" },
                        imageUrl: { type: ["string", "null"] },
                      },
                    },
                  },
                },
              },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    createOrder
  );

  // Get all orders
  fastify.get(
    "/",
    {
      schema: {
        tags: ["Orders"],
        security: [{ bearerAuth: [] }],
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                userId: { type: "string" },
                status: { type: "string" },
                totalAmount: { type: "number" },
                address: { type: "string" },
                phoneNumber: { type: "string" },
                items: {
                  type: "array",
                  items: {
                    type: "object",
                    properties: {
                      id: { type: "string" },
                      quantity: { type: "integer" },
                      price: { type: "number" },
                      product: {
                        type: "object",
                        properties: {
                          id: { type: "string" },
                          name: { type: "string" },
                          description: { type: "string" },
                          price: { type: "number" },
                          imageUrl: { type: ["string", "null"] },
                        },
                      },
                    },
                  },
                },
                createdAt: { type: "string", format: "date-time" },
                updatedAt: { type: "string", format: "date-time" },
              },
            },
          },
        },
      },
    },
    getAllOrders
  );

  // Get order by ID
  fastify.get(
    "/:id",
    {
      schema: {
        tags: ["Orders"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["id"],
          properties: {
            id: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              userId: { type: "string" },
              status: { type: "string" },
              totalAmount: { type: "number" },
              address: { type: "string" },
              phoneNumber: { type: "string" },
              items: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    quantity: { type: "integer" },
                    price: { type: "number" },
                    product: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        name: { type: "string" },
                        description: { type: "string" },
                        price: { type: "number" },
                        imageUrl: { type: ["string", "null"] },
                      },
                    },
                    review: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        rating: { type: "number" },
                        comment: { type: "string" },
                      },
                    },
                  },
                },
              },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    getOrderById
  );

  // Update order status (admin only)
  fastify.put(
    "/:id/status",
    {
      // preHandler: authorizeAdmin,
      schema: {
        tags: ["Orders"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["id"],
          properties: {
            id: { type: "string" },
          },
        },
        body: {
          type: "object",
          required: ["status"],
          properties: {
            status: {
              type: "string",
              enum: [
                "PENDING",
                "PROCESSING",
                "SHIPPED",
                "DELIVERED",
                "CANCELLED",
                "COMPLETED",
              ],
            },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              status: { type: "string" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    updateOrderStatus
  );

  // Cancel order
  fastify.put(
    "/:id/cancel",
    {
      schema: {
        tags: ["Orders"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["id"],
          properties: {
            id: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              status: { type: "string" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    cancelOrder
  );

  // Get dashboard statistics (admin only)
  fastify.get(
    "/dashboard/stats",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Dashboard"],
        security: [{ bearerAuth: [] }],
        response: {
          200: {
            type: "object",
            properties: {
              totalRevenue: { type: "number" },
              totalOrders: { type: "integer" },
              todayRevenue: { type: "number" },
            },
          },
        },
      },
    },
    getDashboardStats
  );

  // Get revenue data (admin only)
  fastify.get(
    "/dashboard/revenue",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Dashboard"],
        security: [{ bearerAuth: [] }],
        querystring: {
          type: "object",
          properties: {
            days: { type: "integer", default: 7 },
          },
        },
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                date: { type: "string", format: "date" },
                amount: { type: "number" },
              },
            },
          },
        },
      },
    },
    getRevenueData
  );
};
