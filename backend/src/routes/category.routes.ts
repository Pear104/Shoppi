import { FastifyInstance } from "fastify";
import {
  getAllCategories,
  getCategoryById,
  createCategory,
  updateCategory,
  deleteCategory,
} from "../controllers/category.controller";
import { authenticate, authorizeAdmin } from "../middleware/auth.middleware";

export const categoryRoutes = async (fastify: FastifyInstance) => {
  // Get all categories (public)
  fastify.get(
    "/",
    {
      schema: {
        tags: ["Categories"],
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                name: { type: "string" },
                createdAt: { type: "string", format: "date-time" },
                updatedAt: { type: "string", format: "date-time" },
              },
            },
          },
        },
      },
    },
    getAllCategories
  );

  // Get category by ID (public)
  fastify.get(
    "/:id",
    {
      schema: {
        tags: ["Categories"],
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
              products: {
                type: "array",
                items: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    name: { type: "string" },
                    description: { type: "string" },
                    price: { type: "number" },
                    stock: { type: "number" },
                    imageUrl: { type: ["string", "null"] },
                    createdAt: { type: "string", format: "date-time" },
                    updatedAt: { type: "string", format: "date-time" },
                  },
                },
              },
              productCount: { type: "number" },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    getCategoryById
  );

  // Create category (admin only)
  fastify.post(
    "/",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Categories"],
        security: [{ bearerAuth: [] }],
        body: {
          type: "object",
          required: ["name"],
          properties: {
            name: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              name: { type: "string" },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    createCategory
  );

  // Update category (admin only)
  fastify.put(
    "/:id",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Categories"],
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
          required: ["name"],
          properties: {
            name: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              id: { type: "string" },
              name: { type: "string" },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    updateCategory
  );

  // Delete category (admin only)
  fastify.delete(
    "/:id",
    {
      preHandler: authorizeAdmin,
      schema: {
        tags: ["Categories"],
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
    deleteCategory
  );
};
