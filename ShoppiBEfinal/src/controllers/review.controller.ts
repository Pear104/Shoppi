import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

interface JwtPayload {
  id: string;
  email: string;
  role: string;
}

export const createReview = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { productId, orderItemId, rating, comment } = request.body as {
      productId: string;
      orderItemId: string;
      rating: number;
      comment?: string;
    };

    // Verify the order item belongs to the user and is from a delivered order
    const orderItem = await prisma.orderItem.findUnique({
      where: { id: orderItemId },
      include: {
        order: true,
        product: true,
      },
    });

    if (!orderItem) {
      return reply.status(404).send({ error: "Order item not found" });
    }

    if (orderItem.order.userId !== user.id) {
      return reply
        .status(403)
        .send({ error: "You can only review products from your own orders" });
    }

    // if (orderItem.order.status !== "DELIVERED") {
    //   return reply
    //     .status(400)
    //     .send({ error: "You can only review products from delivered orders" });
    // }

    if (orderItem.productId !== productId) {
      return reply
        .status(400)
        .send({ error: "Product ID does not match the order item" });
    }

    // Check if a review already exists
    const existingReview = await prisma.review.findUnique({
      where: {
        orderItemId,
      },
    });

    if (existingReview) {
      return reply
        .status(400)
        .send({ error: "You have already reviewed this order item" });
    }

    // Create the review
    const review = await prisma.review.create({
      data: {
        userId: user.id,
        productId,
        orderItemId,
        rating,
        comment,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        product: {
          select: {
            id: true,
            name: true,
            imageUrl: true,
          },
        },
      },
    });

    return reply.status(201).send(review);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const updateReview = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { id } = request.params as { id: string };
    const { rating, comment } = request.body as {
      rating: number;
      comment?: string;
    };

    // Verify the review belongs to the user
    const review = await prisma.review.findUnique({
      where: { id },
    });

    if (!review) {
      return reply.status(404).send({ error: "Review not found" });
    }

    if (review.userId !== user.id && user.role !== "ADMIN") {
      return reply
        .status(403)
        .send({ error: "You can only update your own reviews" });
    }

    // Update the review
    const updatedReview = await prisma.review.update({
      where: { id },
      data: {
        rating,
        comment,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        product: {
          select: {
            id: true,
            name: true,
            imageUrl: true,
          },
        },
      },
    });

    return reply.send(updatedReview);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const deleteReview = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;
    const { id } = request.params as { id: string };

    // Verify the review belongs to the user
    const review = await prisma.review.findUnique({
      where: { id },
    });

    if (!review) {
      return reply.status(404).send({ error: "Review not found" });
    }

    if (review.userId !== user.id && user.role !== "ADMIN") {
      return reply
        .status(403)
        .send({ error: "You can only delete your own reviews" });
    }

    // Delete the review
    await prisma.review.delete({
      where: { id },
    });

    return reply.send({
      success: true,
      message: "Review deleted successfully",
    });
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getUserReviews = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const user = request.user as JwtPayload;

    const reviews = await prisma.review.findMany({
      where: {
        userId: user.id,
      },
      include: {
        product: {
          select: {
            id: true,
            name: true,
            imageUrl: true,
          },
        },
        orderItem: {
          select: {
            id: true,
            orderId: true,
          },
        },
      },
      orderBy: {
        createdAt: "desc",
      },
    });

    return reply.send(reviews);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getProductReviews = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { productId } = request.params as { productId: string };

    const reviews = await prisma.review.findMany({
      where: {
        productId,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: {
        createdAt: "desc",
      },
    });

    // Calculate average rating
    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
    const averageRating = reviews.length > 0 ? totalRating / reviews.length : 0;

    return reply.send({
      reviews,
      averageRating,
      totalReviews: reviews.length,
    });
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getReviewByOrderItemId = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { orderItemId } = request.params as { orderItemId: string };

    const review = await prisma.review.findUnique({
      where: { orderItemId },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
          },
        },
        product: {
          select: {
            id: true,
            name: true,
            imageUrl: true,
          },
        },
      },
    });

    if (!review) {
      return reply.status(404).send({ error: "Review not found" });
    }

    return reply.send(review);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
