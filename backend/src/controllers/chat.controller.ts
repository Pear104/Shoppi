import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

export const getChatHistory = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { receiverId } = request.params as { receiverId: string };

    // Get chat messages between users (in both directions)
    const messages = await prisma.chatMessage.findMany({
      where: {
        OR: [
          { userId: user.id, receiverId: receiverId },
          { userId: receiverId, receiverId: user.id },
        ],
      },
      orderBy: { createdAt: "asc" },
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

    return reply.send(messages);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const sendMessage = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { receiverId } = request.params as { receiverId: string };
    const { content } = request.body as { content: string };

    // Verify receiver exists
    const receiver = await prisma.user.findUnique({
      where: { id: receiverId },
    });

    if (!receiver) {
      return reply.status(404).send({ error: "Receiver not found" });
    }

    // Create message
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

    // Emit message via socket.io (server instance will handle this)
    if (request.server.io) {
      // Emit to both sender and receiver room
      request.server.io.to(`user:${user.id}`).emit("new_message", message);
      request.server.io.to(`user:${receiverId}`).emit("new_message", message);
    }

    return reply.status(201).send(message);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getChatContacts = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;

    // Get unique users who have either sent messages to or received messages from the current user
    const chatContacts = await prisma.user.findMany({
      where: {
        OR: [
          // Users who received messages from current user
          {
            receivedChats: {
              some: {
                userId: user.id,
              },
            },
          },
          // Users who sent messages to current user
          {
            sentChats: {
              some: {
                receiverId: user.id,
              },
            },
          },
        ],
      },
      select: {
        id: true,
        name: true,
        email: true,
        role: true,
        // Get the latest message for each contact
        receivedChats: {
          where: {
            userId: user.id,
          },
          orderBy: {
            createdAt: "desc",
          },
          take: 1,
          select: {
            content: true,
            createdAt: true,
          },
        },
        sentChats: {
          where: {
            receiverId: user.id,
          },
          orderBy: {
            createdAt: "desc",
          },
          take: 1,
          select: {
            content: true,
            createdAt: true,
          },
        },
      },
    });

    // Format the response to include the latest message
    const formattedContacts = chatContacts.map((contact) => {
      const receivedMessage = contact.receivedChats[0];
      const sentMessage = contact.sentChats[0];

      // Get the most recent message
      let latestMessage = null;
      if (receivedMessage && sentMessage) {
        latestMessage =
          receivedMessage.createdAt > sentMessage.createdAt
            ? receivedMessage
            : sentMessage;
      } else {
        latestMessage = receivedMessage || sentMessage;
      }

      return {
        id: contact.id,
        name: contact.name,
        email: contact.email,
        role: contact.role,
        lastMessage: latestMessage
          ? {
              content: latestMessage.content,
              createdAt: latestMessage.createdAt,
            }
          : null,
      };
    });

    // Sort contacts by latest message date
    formattedContacts.sort((a, b) => {
      if (!a.lastMessage) return 1;
      if (!b.lastMessage) return -1;
      return (
        b.lastMessage.createdAt.getTime() - a.lastMessage.createdAt.getTime()
      );
    });

    return reply.send(formattedContacts);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
