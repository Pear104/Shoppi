import { FastifyInstance } from "fastify";
import {
  getCart,
  addToCart,
  updateCartItem,
  removeFromCart,
  clearCart,
} from "../controllers/cart.controller";
import { authenticate } from "../middleware/auth.middleware";

export const cartRoutes = async (fastify: FastifyInstance) => {
  // All cart routes require authentication
  fastify.addHook("onRequest", authenticate);

  // Get user's cart
  fastify.get(
    "/",
    {
      schema: {
        tags: ["Cart"],
        security: [{ bearerAuth: [] }],
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                quantity: { type: "number" },
                createdAt: { type: "string", format: "date-time" },
                updatedAt: { type: "string", format: "date-time" },
                product: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    name: { type: "string" },
                    description: { type: "string" },
                    price: { type: "number" },
                    stock: { type: "number" },
                    imageUrl: { type: ["string", "null"] },
                    category: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        name: { type: "string" },
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    },
    getCart
  );

  // Add item to cart
  fastify.post(
    "/items",
    {
      schema: {
        tags: ["Cart"],
        security: [{ bearerAuth: [] }],
        body: {
          type: "object",
          required: ["productId", "quantity"],
          properties: {
            productId: { type: "string" },
            quantity: { type: "number", minimum: 1 },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              quantity: { type: "number" },
              product: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  description: { type: "string" },
                  price: { type: "number" },
                  stock: { type: "number" },
                  imageUrl: { type: ["string", "null"] },
                  category: {
                    type: "object",
                    properties: {
                      id: { type: "string" },
                      name: { type: "string" },
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
    addToCart
  );

  // Update cart item quantity
  fastify.put(
    "/items/:productId",
    {
      schema: {
        tags: ["Cart"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["productId"],
          properties: {
            productId: { type: "string" },
          },
        },
        body: {
          type: "object",
          required: ["quantity"],
          properties: {
            quantity: { type: "number", minimum: 1 },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              quantity: { type: "number" },
              product: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  description: { type: "string" },
                  price: { type: "number" },
                  stock: { type: "number" },
                  imageUrl: { type: ["string", "null"] },
                  category: {
                    type: "object",
                    properties: {
                      id: { type: "string" },
                      name: { type: "string" },
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
    updateCartItem
  );

  // Remove item from cart
  fastify.delete(
    "/items/:productId",
    {
      schema: {
        tags: ["Cart"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["productId"],
          properties: {
            productId: { type: "string" },
          },
        },
        response: {
          204: {
            type: "null",
            description: "No content",
          },
        },
      },
    },
    removeFromCart
  );

  // Clear cart
  fastify.delete(
    "/",
    {
      schema: {
        tags: ["Cart"],
        security: [{ bearerAuth: [] }],
        response: {
          204: {
            type: "null",
            description: "No content",
          },
        },
      },
    },
    clearCart
  );
};
