import { FastifyPluginAsync } from "fastify";
import fp from "fastify-plugin";
import { Server } from "socket.io";
import { verify } from "jsonwebtoken";
import { PrismaClient } from "@prisma/client";
// import { verify } from "crypto";

// Create a new instance to use inside the plugin
const prisma = new PrismaClient();

interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

// Add type declarations to Fastify
declare module "fastify" {
  interface FastifyInstance {
    io: Server;
  }
}

const socketPlugin: FastifyPluginAsync = async (fastify) => {
  // Create Socket.io server
  const io = new Server(fastify.server, {
    cors: {
      origin: "*", // Allow all origins in development
      methods: ["GET", "POST"],
      credentials: true,
    },
    transports: ["websocket", "polling"], // Move to root level
  });

  // Middleware for authentication
  io.use((socket, next) => {
    const token = socket.handshake.auth.token;
    console.log(socket.handshake);

    if (!token) {
      return next(new Error("Authentication error"));
    }

    try {
      const decoded = verify(token, process.env.JWT_SECRET!) as JwtPayload;
      socket.data.user = decoded;
      next();
    } catch (err) {
      next(new Error("Authentication error"));
    }
  });

  // Handle connections
  io.on("connection", (socket) => {
    const user = socket.data.user;

    fastify.log.info(`User connected: ${user.id}`);

    // Automatically join user's personal room
    socket.join(`user:${user.id}`);

    // Join a specific chat room
    socket.on("join", async (data) => {
      try {
        const roomId = data.roomId || "support";

        // Join the room
        socket.join(`room:${roomId}`);

        // Notify other users in the room
        socket.to(`room:${roomId}`).emit("user_joined", {
          userId: user.id,
          name: user.email,
        });

        socket.emit("joined", roomId);
        fastify.log.info(`User ${user.id} joined room ${roomId}`);
      } catch (error) {
        fastify.log.error(error);
        socket.emit("error", "Error joining room");
      }
    });

    // Handle direct chat messages
    socket.on("message", async (data) => {
      try {
        // Extract message data
        const content = data.content;
        const roomId = data.roomId;
        const timestamp = data.timestamp || Date.now();

        if (!content || content.trim() === "") {
          return;
        }

        // If this is a room message
        if (roomId) {
          // Create formatted message for broadcast
          const messageData = {
            senderId: user.id,
            content: content,
            timestamp: timestamp,
            senderName: user.email,
            roomId: roomId,
          };

          // Broadcast to all users in the room
          io.to(`room:${roomId}`).emit("message", messageData);

          fastify.log.info(`User ${user.id} sent message to room ${roomId}11`);
          return;
        }

        // Handle direct user-to-user message
        const receiverId = data.receiverId;
        if (!receiverId) {
          socket.emit("error", "Missing receiver ID");
          return;
        }

        // Save message to database if needed
        const message = await prisma.chatMessage.create({
          data: {
            content,
            userId: user.id,
            receiverId,
          },
          include: {
            user: {
              select: {
                id: true,
                name: true,
                email: true,
                role: true,
              },
            },
            receiver: {
              select: {
                id: true,
                name: true,
                email: true,
                role: true,
              },
            },
          },
        });

        // Send to both the sender and receiver
        socket.emit("new_message", message);
        socket.to(`user:${receiverId}`).emit("new_message", message);

        fastify.log.info(`User ${user.id} sent message to user ${receiverId}`);
      } catch (error) {
        fastify.log.error(error);
        socket.emit("error", "Error sending message");
      }
    });

    // Start chat with specific user
    socket.on("start_chat", async (receiverId) => {
      try {
        // Verify user exists
        const receiver = await prisma.user.findUnique({
          where: { id: receiverId },
        });

        if (!receiver) {
          socket.emit("error", "User not found");
          return;
        }

        // No need to join a separate room as messages are emitted to user rooms
        socket.emit("chat_started", receiverId);
        fastify.log.info(
          `User ${user.id} started chat with user ${receiverId}`
        );
      } catch (error) {
        fastify.log.error(error);
        socket.emit("error", "Server error");
      }
    });

    // Handle typing indicator
    socket.on("typing", (receiverId) => {
      // Notify the receiver that this user is typing
      socket.to(`user:${receiverId}`).emit("user_typing", user.id);
    });

    // Leave room handler
    socket.on("leave", (roomId) => {
      socket.leave(`room:${roomId}`);
      socket.to(`room:${roomId}`).emit("user_left", user.id);
      fastify.log.info(`User ${user.id} left room ${roomId}`);
    });

    // Handle disconnect
    socket.on("disconnect", () => {
      fastify.log.info(`User disconnected: ${user.id}`);
    });
  });

  // Make io available through the fastify instance
  fastify.decorate("io", io);
};

export default fp(socketPlugin);
