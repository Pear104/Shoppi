import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

export const getCart = async (request: FastifyRequest, reply: FastifyReply) => {
  try {
    const user = request.user as JwtPayload;

    const cart = await prisma.cart.findUnique({
      where: { userId: user.id },
      include: {
        items: {
          include: {
            product: {
              include: {
                category: true,
              },
            },
          },
        },
      },
    });

    if (!cart) {
      // Create a cart if it doesn't exist
      const newCart = await prisma.cart.create({
        data: {
          userId: user.id,
        },
        include: {
          items: {
            include: {
              product: {
                include: {
                  category: true,
                },
              },
            },
          },
        },
      });
      return reply.send(newCart);
    }

    return reply.send([...cart.items]);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const addToCart = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { productId, quantity } = request.body as {
      productId: string;
      quantity: number;
    };

    // Check if product exists and has enough stock
    const product = await prisma.product.findUnique({
      where: { id: productId },
    });

    if (!product) {
      return reply.status(404).send({ error: "Product not found" });
    }

    if (product.stock < quantity) {
      return reply.status(400).send({ error: "Not enough stock available" });
    }

    // Get or create cart
    let cart = await prisma.cart.findUnique({
      where: { userId: user.id },
    });

    if (!cart) {
      cart = await prisma.cart.create({
        data: {
          userId: user.id,
        },
      });
    }

    // Check if item already exists in cart
    const existingCartItem = await prisma.cartItem.findUnique({
      where: {
        cartId_productId: {
          cartId: cart.id,
          productId,
        },
      },
    });

    if (existingCartItem) {
      // Update quantity if item exists
      const updatedCartItem = await prisma.cartItem.update({
        where: {
          cartId_productId: {
            cartId: cart.id,
            productId,
          },
        },
        data: {
          quantity: existingCartItem.quantity + quantity,
        },
        include: {
          product: {
            include: {
              category: true,
            },
          },
        },
      });

      return reply.send(updatedCartItem);
    }

    // Create new cart item
    const cartItem = await prisma.cartItem.create({
      data: {
        cartId: cart.id,
        productId,
        quantity,
      },
      include: {
        product: {
          include: {
            category: true,
          },
        },
      },
    });

    return reply.status(201).send(cartItem);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const updateCartItem = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { productId } = request.params as { productId: string };
    const { quantity } = request.body as { quantity: number };

    // Check if product exists and has enough stock
    const product = await prisma.product.findUnique({
      where: { id: productId },
    });

    if (!product) {
      return reply.status(404).send({ error: "Product not found" });
    }

    if (product.stock < quantity) {
      return reply.status(400).send({ error: "Not enough stock available" });
    }

    // Get cart
    const cart = await prisma.cart.findUnique({
      where: { userId: user.id },
    });

    if (!cart) {
      return reply.status(404).send({ error: "Cart not found" });
    }

    // Update cart item
    const cartItem = await prisma.cartItem.update({
      where: {
        cartId_productId: {
          cartId: cart.id,
          productId,
        },
      },
      data: {
        quantity,
      },
      include: {
        product: {
          include: {
            category: true,
          },
        },
      },
    });

    return reply.send(cartItem);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const removeFromCart = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { productId } = request.params as { productId: string };

    // Get cart
    const cart = await prisma.cart.findUnique({
      where: { userId: user.id },
    });

    if (!cart) {
      return reply.status(404).send({ error: "Cart not found" });
    }

    // Delete cart item
    await prisma.cartItem.delete({
      where: {
        cartId_productId: {
          cartId: cart.id,
          productId,
        },
      },
    });

    return reply.status(204).send();
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const clearCart = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;

    // Get cart
    const cart = await prisma.cart.findUnique({
      where: { userId: user.id },
    });

    if (!cart) {
      return reply.status(404).send({ error: "Cart not found" });
    }

    // Delete all cart items
    await prisma.cartItem.deleteMany({
      where: {
        cartId: cart.id,
      },
    });

    return reply.status(204).send();
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
