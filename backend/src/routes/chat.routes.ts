import { FastifyInstance } from "fastify";
import {
  getChatHistory,
  sendMessage,
  getChatContacts,
} from "../controllers/chat.controller";
import { authenticate } from "../middleware/auth.middleware";

export const chatRoutes = async (fastify: FastifyInstance) => {
  // All chat routes require authentication
  fastify.addHook("onRequest", authenticate);

  // Get all chat contacts
  fastify.get(
    "/contacts",
    {
      schema: {
        tags: ["Chat"],
        security: [{ bearerAuth: [] }],
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                name: { type: "string" },
                email: { type: "string" },
                role: { type: "string" },
                lastMessage: {
                  type: ["object", "null"],
                  properties: {
                    content: { type: "string" },
                    createdAt: { type: "string", format: "date-time" },
                  },
                },
              },
            },
          },
        },
      },
    },
    getChatContacts
  );

  // Get chat history with a user
  fastify.get(
    "/user/:receiverId",
    {
      schema: {
        tags: ["Chat"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["receiverId"],
          properties: {
            receiverId: { type: "string" },
          },
        },
        response: {
          200: {
            type: "array",
            items: {
              type: "object",
              properties: {
                id: { type: "string" },
                content: { type: "string" },
                userId: { type: "string" },
                receiverId: { type: "string" },
                user: {
                  type: "object",
                  properties: {
                    id: { type: "string" },
                    name: { type: "string" },
                    email: { type: "string" },
                    role: { type: "string" },
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
    getChatHistory
  );

  // Send a message to a user
  fastify.post(
    "/user/:receiverId",
    {
      schema: {
        tags: ["Chat"],
        security: [{ bearerAuth: [] }],
        params: {
          type: "object",
          required: ["receiverId"],
          properties: {
            receiverId: { type: "string" },
          },
        },
        body: {
          type: "object",
          required: ["content"],
          properties: {
            content: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              id: { type: "string" },
              content: { type: "string" },
              userId: { type: "string" },
              receiverId: { type: "string" },
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  name: { type: "string" },
                  email: { type: "string" },
                  role: { type: "string" },
                },
              },
              createdAt: { type: "string", format: "date-time" },
              updatedAt: { type: "string", format: "date-time" },
            },
          },
        },
      },
    },
    sendMessage
  );
};
