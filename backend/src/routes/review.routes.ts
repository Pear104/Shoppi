import { FastifyInstance } from "fastify";
import {
  createReview,
  updateReview,
  deleteReview,
  getUserReviews,
  getProductReviews,
  getReviewByOrderItemId,
} from "../controllers/review.controller";
import { authenticate } from "../middleware/auth.middleware";

export const reviewRoutes = async (fastify: FastifyInstance) => {
  // Create a review
  fastify.post(
    "/",
    {
      onRequest: [authenticate],
      schema: {
        tags: ["Reviews"],
        security: [{ bearerAuth: [] }],
        body: {
          type: "object",
          required: ["productId", "orderItemId", "rating"],
          properties: {
            productId: { type: "string" },
            orderItemId: { type: "string" },
            rating: { type: "integer", minimum: 1, maximum: 5 },
            comment: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              userId: { type: "string" },
              productId: { type: "string" },
              orderItemId: { type: "string" },
              rating: { type: "integer" },
              comment: { type: ["string", "null"] },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  email: { type: "string" },
                },
              },
              product: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  imageUrl: { type: ["string", "null"] },
                },
              },
            },
          },
        },
      },
    },
    createReview
  );

  // Update a review
  fastify.put(
    "/:id",
    {
      onRequest: [authenticate],
      schema: {
        tags: ["Reviews"],
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
          required: ["rating"],
          properties: {
            rating: { type: "integer", minimum: 1, maximum: 5 },
            comment: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              userId: { type: "string" },
              productId: { type: "string" },
              orderItemId: { type: "string" },
              rating: { type: "integer" },
              comment: { type: ["string", "null"] },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  email: { type: "string" },
                },
              },
              product: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  imageUrl: { type: ["string", "null"] },
                },
              },
            },
          },
        },
      },
    },
    updateReview
  );

  // Delete a review
  fastify.delete(
    "/:id",
    {
      onRequest: [authenticate],
      schema: {
        tags: ["Reviews"],
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
              success: { type: "boolean" },
              message: { type: "string" },
            },
          },
        },
      },
    },
    deleteReview
  );

  // Get current user's reviews
  fastify.get(
    "/me",
    {
      onRequest: [authenticate],
      schema: {
        tags: ["Reviews"],
        security: [{ bearerAuth: [] }],
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                userId: { type: "string" },
                productId: { type: "string" },
                orderItemId: { type: "string" },
                rating: { type: "integer" },
                comment: { type: ["string", "null"] },
                createdAt: { type: "string", format: "date-time" },
                updatedAt: { type: "string", format: "date-time" },
                product: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    name: { type: "string" },
                    imageUrl: { type: ["string", "null"] },
                  },
                },
                orderItem: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    orderId: { type: "string" },
                  },
                },
              },
            },
          },
        },
      },
    },
    getUserReviews
  );

  // Get product reviews
  fastify.get(
    "/product/:productId",
    {
      schema: {
        tags: ["Reviews"],
        params: {
          type: "object",
          required: ["productId"],
          properties: {
            productId: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              reviews: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    userId: { type: "string" },
                    productId: { type: "string" },
                    orderItemId: { type: "string" },
                    rating: { type: "integer" },
                    comment: { type: ["string", "null"] },
                    createdAt: { type: "string", format: "date-time" },
                    updatedAt: { type: "string", format: "date-time" },
                    user: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        name: { type: "string" },
                      },
                    },
                  },
                },
              },
              averageRating: { type: "number" },
              totalReviews: { type: "integer" },
            },
          },
        },
      },
    },
    getProductReviews
  );

  // Get review by order item ID
  fastify.get(
    "/order-item/:orderItemId",
    {
      schema: {
        tags: ["Reviews"],
        params: {
          type: "object",
          required: ["orderItemId"],
          properties: {
            orderItemId: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              userId: { type: "string" },
              productId: { type: "string" },
              orderItemId: { type: "string" },
              rating: { type: "integer" },
              comment: { type: ["string", "null"] },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  email: { type: "string" },
                },
              },
              product: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  imageUrl: { type: ["string", "null"] },
                },
              },
            },
          },
        },
      },
    },
    getReviewByOrderItemId
  );
};
