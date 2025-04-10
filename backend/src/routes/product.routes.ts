import { FastifyInstance } from "fastify";
import {
  getAllProducts,
  getProductById,
  createProduct,
  updateProduct,
  deleteProduct,
} from "../controllers/product.controller";
import { authenticate, authorizeAdmin } from "../middleware/auth.middleware";

export const productRoutes = async (fastify: FastifyInstance) => {
  // Get all products (public)
  fastify.get(
    "/",
    {
      schema: {
        tags: ["Products"],
        querystring: {
          type: "object",
          properties: {
            category: { type: "string" },
            search: { type: "string" },
          },
        },
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                name: { type: "string" },
                description: { type: "string" },
                price: { type: "number" },
                stock: { type: "integer" },
                imageUrl: { type: ["string", "null"] },
                categoryId: { type: "string" },
                category: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    name: { type: "string" },
                  },
                },
                avgRating: { type: "number" },
                createdAt: { type: "string", format: "date-time" },
                updatedAt: { type: "string", format: "date-time" },
              },
            },
          },
        },
      },
    },
    getAllProducts
  );

  // Get product by ID (public)
  fastify.get(
    "/:id",
    {
      schema: {
        tags: ["Products"],
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
              name: { type: "string" },
              description: { type: "string" },
              price: { type: "number" },
              stock: { type: "number" },
              imageUrl: { type: ["string", "null"] },
              categoryId: { type: "string" },
              category: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                },
              },
              reviews: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    rating: { type: "number" },
                    comment: { type: ["string", "null"] },
                    userId: { type: "string" },
                    user: {
                      type: "object",
                      properties: {
                        id: { type: "string" },
                        name: { type: "string" },
                      },
                    },
                    createdAt: { type: "string", format: "date-time" },
                  },
                },
              },
              avgRating: { type: "number" },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    getProductById
  );

  // Create product (admin only)
  fastify.post(
    "/",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Products"],
        security: [{ bearerAuth: [] }],
        body: {
          type: "object",
          required: ["name", "description", "price", "stock", "categoryId"],
          properties: {
            name: { type: "string" },
            description: { type: "string" },
            price: { type: "number", minimum: 0 },
            stock: { type: "number", minimum: 0 },
            categoryId: { type: "string" },
            imageUrl: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              name: { type: "string" },
              description: { type: "string" },
              price: { type: "number" },
              stock: { type: "number" },
              imageUrl: { type: ["string", "null"] },
              categoryId: { type: "string" },
              category: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                },
              },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    createProduct
  );

  // Update product (admin only)
  fastify.put(
    "/:id",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Products"],
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
          properties: {
            name: { type: "string" },
            description: { type: "string" },
            price: { type: "number", minimum: 0 },
            stock: { type: "number", minimum: 0 },
            categoryId: { type: "string" },
            imageUrl: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              name: { type: "string" },
              description: { type: "string" },
              price: { type: "number" },
              stock: { type: "number" },
              imageUrl: { type: ["string", "null"] },
              categoryId: { type: "string" },
              category: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                },
              },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    updateProduct
  );

  // Delete product (admin only)
  fastify.delete(
    "/:id",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Products"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["id"],
          properties: {
            id: { type: "string" },
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
    deleteProduct
  );
};
