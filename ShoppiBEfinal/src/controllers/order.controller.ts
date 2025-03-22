import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient, OrderStatus } from "@prisma/client";

const prisma = new PrismaClient();

interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

export const createOrder = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { address, phoneNumber } = request.body as {
      address: string;
      phoneNumber: string;
    };

    // Get user's cart
    const cart = await prisma.cart.findUnique({
      where: { userId: user.id },
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
    });

    if (!cart || cart.items.length === 0) {
      return reply.status(400).send({ error: "Cart is empty" });
    }

    // Check stock availability and calculate total
    let totalAmount = 0;
    const orderItems = [];

    for (const item of cart.items) {
      if (item.product.stock < item.quantity) {
        return reply.status(400).send({
          error: `Not enough stock for product: ${item.product.name}`,
        });
      }

      totalAmount += item.product.price * item.quantity;
      orderItems.push({
        productId: item.productId,
        quantity: item.quantity,
        price: item.product.price,
      });

      // Update product stock
      await prisma.product.update({
        where: { id: item.productId },
        data: {
          stock: item.product.stock - item.quantity,
        },
      });
    }

    // Create order
    const order = await prisma.order.create({
      data: {
        userId: user.id,
        totalAmount,
        address,
        phoneNumber,
        items: {
          create: orderItems,
        },
      },
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
    });

    // Clear cart
    await prisma.cartItem.deleteMany({
      where: { cartId: cart.id },
    });

    return reply.status(201).send(order);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getAllOrders = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;

    const where = user.role === "ADMIN" ? {} : { userId: user.id };

    const orders = await prisma.order.findMany({
      where,
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
      orderBy: {
        createdAt: "desc",
      },
    });

    return reply.send(orders);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getOrderById = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { id } = request.params as { id: string };

    const order = await prisma.order.findUnique({
      where: { id },
      include: {
        items: {
          include: {
            product: true,
            review: true,
          },
        },
      },
    });

    if (!order) {
      return reply.status(404).send({ error: "Order not found" });
    }

    // Only allow admin or order owner to view the order
    if (user.role !== "ADMIN" && order.userId !== user.id) {
      return reply.status(403).send({ error: "Forbidden" });
    }

    return reply.send(order);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const updateOrderStatus = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };
    const { status } = request.body as { status: OrderStatus };

    const order = await prisma.order.update({
      where: { id },
      data: { status },
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
    });

    return reply.send(order);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const cancelOrder = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { id } = request.params as { id: string };

    const order = await prisma.order.findUnique({
      where: { id },
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
    });

    if (!order) {
      return reply.status(404).send({ error: "Order not found" });
    }

    // Only allow admin or order owner to cancel the order
    if (user.role !== "ADMIN" && order.userId !== user.id) {
      return reply.status(403).send({ error: "Forbidden" });
    }

    // Only allow cancellation of pending orders
    if (order.status !== "PENDING") {
      return reply
        .status(400)
        .send({ error: "Can only cancel pending orders" });
    }

    // Restore product stock
    for (const item of order.items) {
      await prisma.product.update({
        where: { id: item.productId },
        data: {
          stock: item.product.stock + item.quantity,
        },
      });
    }

    // Update order status
    const updatedOrder = await prisma.order.update({
      where: { id },
      data: { status: "CANCELLED" },
      include: {
        items: {
          include: {
            product: true,
          },
        },
      },
    });

    return reply.send(updatedOrder);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
