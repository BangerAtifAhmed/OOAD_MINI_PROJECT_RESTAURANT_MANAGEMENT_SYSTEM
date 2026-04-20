# Restaurant Management System

A desktop-based Restaurant Management System built as a Mini Project for the **CS352B Object-Oriented Analysis and Design** course. Developed in pure Java using Swing for the GUI and MySQL as the backend database via JDBC.

---

## Features

- **Table Management** — Track restaurant tables with real-time availability (Available / Occupied)
- **Order Management** — Place orders, assign waiters, add menu items with custom toppings
- **Kitchen Panel** — Kitchen staff can view and update order status in real time
- **Billing & Payment** — Process payments via Cash, Stripe, or PayPal; auto-generate PDF receipts
- **Admin Panel** — Manage menu items, waiters, and view order history

---

## Design Patterns

| Pattern | Implementation |
|---|---|
| **Decorator** | Add toppings (Cheese, Paneer, Mushroom, Chilli) to food items dynamically |
| **State** | Order lifecycle: Received → Cooking → Ready → Served / Cancelled |
| **Adapter** | Unified payment interface bridging StripeAPI and PayPalAPI |
| **Singleton** | FloorManager for thread-safe table availability management |

---

## Tech Stack

- **Language:** Java (no frameworks)
- **GUI:** Java Swing
- **Database:** MySQL (JDBC)
- **Architecture:** MVC (Model-View-Controller)

---

## Project Structure

```
MiniProject/
├── src/
│   ├── Main.java
│   ├── controller/        # MVC Controllers
│   ├── dao/               # Database Access Objects
│   ├── model/
│   │   ├── decorator/     # Decorator pattern (toppings)
│   │   ├── payment/       # Adapter pattern (Stripe, PayPal)
│   │   └── state/         # State pattern (order lifecycle)
│   ├── singleton/         # Singleton pattern (FloorManager)
│   └── view/              # Swing UI panels
├── lib/
│   └── mysql-connector-j-9.6.0.jar
├── schema.sql
└── compile_and_run.bat
```

---

## Prerequisites

1. **Java JDK** 8 or higher
2. **MySQL** running on `localhost:3306`
3. MySQL Connector JAR already included in `lib/`

---

## Setup

### 1. Create the database

Run `schema.sql` in your MySQL client:

```sql
source path/to/schema.sql
```

### 2. Configure database credentials

Open [src/dao/DatabaseManager.java](src/dao/DatabaseManager.java) and update:

```java
private static final String PASSWORD = "your_mysql_password";
```

### 3. Build and run

**Windows:**
```bash
compile_and_run.bat
```

**Manual (Windows):**
```bash
javac -cp "lib\*" -sourcepath src -d out src\Main.java src\model\*.java src\model\decorator\*.java src\model\state\*.java src\model\payment\*.java src\dao\*.java src\singleton\*.java src\controller\*.java src\view\*.java
java -cp "out;lib\*" Main
```

**Manual (Linux/Mac):**
```bash
javac -cp "lib/*" -sourcepath src -d out src/Main.java src/model/*.java src/model/decorator/*.java src/model/state/*.java src/model/payment/*.java src/dao/*.java src/singleton/*.java src/controller/*.java src/view/*.java
java -cp "out:lib/*" Main
```

---

## Database

- **Database name:** `restaurant_management`
- Seed data includes 8 tables and 10 menu items (auto-inserted on first run)
- Payment methods supported: `CASH`, `STRIPE`, `PAYPAL`

---

## Diagrams

| Diagram | File |
|---|---|
| Use Case Diagram | `Use_Case_Diagram.png` |
| Class Diagram (Domain) | `class_diagram_domain.png` |
| Class Diagram (Patterns) | `class_diagram_patterns.png` |
| Sequence Diagram (Normal Flow) | `Sequence_Diagram_Normal_flow.png` |
| Sequence Diagram (Alternate + Exception) | `Sequence_Diagram_ Alternate + Exception Flow.png` |
| State Diagram | `State_Diagram.png` |
| Activity Diagram | `activity_diagram.png` |
| Component Diagram | `component_diagram.png` |
| Deployment Diagram | `deployment_diagram.png` |
