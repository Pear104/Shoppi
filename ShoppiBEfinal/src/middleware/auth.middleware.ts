import { FastifyRequest, FastifyReply } from "fastify";

// Define the JWT payload type
interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

export const authenticate = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    await request.jwtVerify();
    return;
  } catch (err) {
    return reply.status(401).send({ error: "Unauthorized" });
  }
};

export const authorizeAdmin = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    await request.jwtVerify();

    // Cast the user to our JwtPayload type
    const user = request.user as JwtPayload;

    if (user.role !== "ADMIN") {
      return reply
        .status(403)
        .send({ error: "Forbidden: Admin access required" });
    }
    return;
  } catch (err) {
    return reply.status(401).send({ error: "Unauthorized" });
  }
};
