import { FastifyInstance } from "fastify";
import { register, login } from "../controllers/auth.controller";

export const authRoutes = async (fastify: FastifyInstance) => {
  fastify.post(
    "/register",
    {
      schema: {
        tags: ["Authentication"],
        body: {
          type: "object",
          required: ["email", "password", "name"],
          properties: {
            email: { type: "string", format: "email" },
            password: { type: "string", minLength: 6 },
            name: { type: "string" },
          },
        },
        response: {
          201: {
            type: "object",
            properties: {
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  email: { type: "string" },
                  name: { type: "string" },
                  role: { type: "string" },
                },
              },
              token: { type: "string" },
            },
          },
        },
      },
    },
    register
  );

  fastify.post(
    "/login",
    {
      schema: {
        tags: ["Authentication"],
        body: {
          type: "object",
          required: ["email", "password"],
          properties: {
            email: { type: "string", format: "email" },
            password: { type: "string" },
          },
        },
        response: {
          200: {
            type: "object",
            properties: {
              user: {
                type: "object",
                properties: {
                  id: { type: "string" },
                  email: { type: "string" },
                  name: { type: "string" },
                  role: { type: "string" },
                },
              },
              token: { type: "string" },
            },
          },
        },
      },
    },
    login
  );
};
