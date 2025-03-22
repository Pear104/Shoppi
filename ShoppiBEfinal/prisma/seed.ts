import { PrismaClient, Role } from "@prisma/client";
import { faker } from "@faker-js/faker";
import * as bcrypt from "bcrypt";

const prisma = new PrismaClient();

async function main() {
  console.log("Starting seed...");

  // Create admin user
  const adminPassword = await bcrypt.hash("admin123", 10);
  const admin = await prisma.user.upsert({
    where: { email: "admin@example.com" },
    update: {},
    create: {
      email: "admin@example.com",
      password: adminPassword,
      name: "Admin User",
      role: Role.ADMIN,
      cart: {
        create: {},
      },
    },
  });
  console.log("Admin user created:", admin.email);

  // Create regular users
  const users = [];
  for (let i = 0; i < 10; i++) {
    const password = await bcrypt.hash("password123", 10);
    const user = await prisma.user.create({
      data: {
        email: faker.internet.email().toLowerCase(),
        password,
        name: faker.person.fullName(),
        role: Role.CUSTOMER,
        cart: {
          create: {},
        },
      },
    });
    users.push(user);
    console.log("User created:", user.email);
  }

  // Create categories
  const categories = [];
  const categoryNames = [
    "Electronics",
    "Clothing",
    "Books",
    "Home & Kitchen",
    "Sports",
  ];

  for (const name of categoryNames) {
    const category = await prisma.category.upsert({
      where: { name },
      update: {},
      create: { name },
    });
    categories.push(category);
    console.log("Category created or found:", category.name);
  }

  // Create products
  for (let i = 0; i < 50; i++) {
    const category = faker.helpers.arrayElement(categories);
    const product = await prisma.product.create({
      data: {
        name: faker.commerce.productName(),
        description: faker.commerce.productDescription(),
        price: parseFloat(faker.commerce.price({ min: 10, max: 1000 })),
        stock: faker.number.int({ min: 5, max: 100 }),
        imageUrl: faker.image.url(),
        categoryId: category.id,
      },
    });
    console.log("Product created:", product.name);

    // Create reviews for some products
    if (faker.number.int({ min: 0, max: 10 }) > 3) {
      // Use a set of users for reviews without duplicates
      const reviewCount = Math.min(
        faker.number.int({ min: 1, max: 5 }),
        users.length
      );
      const reviewers = faker.helpers.arrayElements(users, reviewCount);

      for (const user of reviewers) {
        // First create an order with this product for the user
        const order = await prisma.order.create({
          data: {
            userId: user.id,
            totalAmount: product.price,
            address: faker.location.streetAddress(),
            phoneNumber: faker.phone.number(),
            status: "COMPLETED",
          },
        });

        // Create the order item
        const orderItem = await prisma.orderItem.create({
          data: {
            orderId: order.id,
            productId: product.id,
            quantity: 1,
            price: product.price,
          },
        });

        // Now create the review with orderItemId
        await prisma.review.create({
          data: {
            userId: user.id,
            productId: product.id,
            orderItemId: orderItem.id,
            rating: faker.number.int({ min: 1, max: 5 }),
            comment: faker.helpers.maybe(() => faker.lorem.paragraph(), {
              probability: 0.7,
            }),
          },
        });
      }
      console.log(
        `Created ${reviewers.length} reviews for product ${product.name}`
      );
    }
  }

  // Create some orders
  for (let i = 0; i < 20; i++) {
    const user = faker.helpers.arrayElement(users);
    const orderItems = [];

    const itemCount = faker.number.int({ min: 1, max: 5 });
    const products = await prisma.product.findMany({
      take: itemCount,
      orderBy: { id: "asc" },
    });

    let totalAmount = 0;

    for (const product of products) {
      const quantity = faker.number.int({ min: 1, max: 3 });
      const price = product.price;
      totalAmount += price * quantity;

      orderItems.push({
        productId: product.id,
        quantity,
        price,
      });
    }

    await prisma.order.create({
      data: {
        userId: user.id,
        totalAmount,
        address: faker.location.streetAddress(),
        phoneNumber: faker.phone.number(),
        status: faker.helpers.arrayElement([
          "PENDING",
          "PROCESSING",
          "COMPLETED",
        ]),
        items: {
          create: orderItems,
        },
      },
    });
    console.log(`Created order for user ${user.email}`);
  }

  // Create some chat messages
  for (let i = 0; i < 30; i++) {
    const user = faker.helpers.arrayElement(users);
    await prisma.message.create({
      data: {
        content: faker.lorem.paragraph(),
        senderId: user.id,
        receiverId: admin.id,
        isRead: faker.datatype.boolean(),
      },
    });

    // Some replies from admin
    if (faker.datatype.boolean()) {
      await prisma.message.create({
        data: {
          content: faker.lorem.paragraph(),
          senderId: admin.id,
          receiverId: user.id,
          isRead: faker.datatype.boolean(),
        },
      });
    }
  }
  console.log("Created chat messages");

  console.log("Seeding completed!");
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
