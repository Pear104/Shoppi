import { FastifyRequest, FastifyReply } from "fastify";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

export const getAllCategories = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const categories = await prisma.category.findMany();

    return reply.send(
      categories.map((category) => ({
        ...category,
      }))
    );
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const getCategoryById = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };

    const category = await prisma.category.findUnique({
      where: { id },
      include: {
        products: true,
        _count: {
          select: {
            products: true,
          },
        },
      },
    });

    if (!category) {
      return reply.status(404).send({ error: "Category not found" });
    }

    return reply.send({
      ...category,
      productCount: category._count.products,
      _count: undefined,
    });
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const createCategory = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { name } = request.body as { name: string };

    // Check if category with the same name already exists
    const existingCategory = await prisma.category.findUnique({
      where: { name },
    });

    if (existingCategory) {
      return reply
        .status(400)
        .send({ error: "Category with this name already exists" });
    }

    const category = await prisma.category.create({
      data: { name },
    });

    return reply.status(201).send(category);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const updateCategory = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };
    const { name } = request.body as { name: string };

    // Check if category exists
    const existingCategory = await prisma.category.findUnique({
      where: { id },
    });

    if (!existingCategory) {
      return reply.status(404).send({ error: "Category not found" });
    }

    // Check if another category with the same name already exists
    const duplicateCategory = await prisma.category.findUnique({
      where: { name },
    });

    if (duplicateCategory && duplicateCategory.id !== id) {
      return reply
        .status(400)
        .send({ error: "Category with this name already exists" });
    }

    const category = await prisma.category.update({
      where: { id },
      data: { name },
    });

    return reply.send(category);
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};

export const deleteCategory = async (
  request: FastifyRequest,
  reply: FastifyReply
) => {
  try {
    const { id } = request.params as { id: string };

    // Check if category exists
    const existingCategory = await prisma.category.findUnique({
      where: { id },
    });

    if (!existingCategory) {
      return reply.status(404).send({ error: "Category not found" });
    }

    // Check if category has products
    const productCount = await prisma.product.count({
      where: { categoryId: id },
    });

    if (productCount > 0) {
      return reply.status(400).send({
        error: "Cannot delete category with associated products",
        productCount,
      });
    }

    await prisma.category.delete({
      where: { id },
    });

    return reply.status(204).send();
  } catch (error) {
    request.log.error(error);
    return reply.status(500).send({ error: "Internal Server Error" });
  }
};
