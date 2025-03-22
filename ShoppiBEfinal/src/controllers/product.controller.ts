import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient, Product } from "@prisma/client";

const prisma = new PrismaClient();

export const getAllProducts = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { category, search } = request.query as {
      category?: string;
      search?: string;
    };

    const where = {
      ...(category ? { categoryId: category } : {}),
      ...(search ? { name: { contains: search } } : {}),
    };

    const [products] = await Promise.all([
      prisma.product.findMany({
        where,
        include: {
          category: true,
          reviews: {
            select: {
              rating: true,
            },
          },
        },
      }),
      prisma.product.count({ where }),
    ]);

    // Calculate average rating for each product
    const productsWithAvgRating = products.map(
      (product: Product & { reviews: { rating: number }[] }) => {
        const avgRating = product.reviews.length
          ? product.reviews.reduce((sum, review) => sum + review.rating, 0) /
            product.reviews.length
          : 0;

        return {
          ...product,
          avgRating,
          reviews: undefined,
        };
      }
    );

    return reply.send(productsWithAvgRating);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getProductById = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };

    const product = await prisma.product.findUnique({
      where: { id },
      include: {
        category: true,
        reviews: {
          include: {
            user: {
              select: {
                id: true,
                name: true,
              },
            },
          },
        },
      },
    });

    if (!product) {
      return reply.status(404).send({ error: "Product not found" });
    }

    // Calculate average rating
    const avgRating = product.reviews.length
      ? product.reviews.reduce((sum, review) => sum + review.rating, 0) /
        product.reviews.length
      : 0;

    return reply.send({
      ...product,
      avgRating,
    });
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const createProduct = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { name, description, price, stock, categoryId, imageUrl } =
      request.body as {
        name: string;
        description: string;
        price: number;
        stock: number;
        categoryId: string;
        imageUrl?: string;
      };

    // Check if category exists
    const category = await prisma.category.findUnique({
      where: { id: categoryId },
    });

    if (!category) {
      return reply.status(400).send({ error: "Category not found" });
    }

    const product = await prisma.product.create({
      data: {
        name,
        description,
        price,
        stock,
        categoryId,
        imageUrl,
      },
      include: {
        category: true,
      },
    });

    return reply.status(201).send(product);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const updateProduct = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };
    const { name, description, price, stock, categoryId, imageUrl } =
      request.body as {
        name?: string;
        description?: string;
        price?: number;
        stock?: number;
        categoryId?: string;
        imageUrl?: string;
      };

    // Check if product exists
    const existingProduct = await prisma.product.findUnique({
      where: { id },
    });

    if (!existingProduct) {
      return reply.status(404).send({ error: "Product not found" });
    }

    // Check if category exists if provided
    if (categoryId) {
      const category = await prisma.category.findUnique({
        where: { id: categoryId },
      });

      if (!category) {
        return reply.status(400).send({ error: "Category not found" });
      }
    }

    const product = await prisma.product.update({
      where: { id },
      data: {
        name,
        description,
        price,
        stock,
        categoryId,
        imageUrl,
      },
      include: {
        category: true,
      },
    });

    return reply.send(product);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const deleteProduct = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };

    // Check if product exists
    const existingProduct = await prisma.product.findUnique({
      where: { id },
    });

    if (!existingProduct) {
      return reply.status(404).send({ error: "Product not found" });
    }

    await prisma.product.delete({
      where: { id },
    });

    return reply.status(204).send();
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
