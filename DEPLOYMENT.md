# Production Deployment Guide
## Team Expense Approval & Management System

This guide explains how to deploy the application as a **24/7 publicly accessible web application** so recruiters, interviewers, and team members can test it in their browser without downloading code.

---

## Deployment Architectures

You can choose between two deployment strategies:

| Strategy | Hosting | Cost | Setup Complexity | Best For |
| :--- | :--- | :--- | :--- | :--- |
| **Option 1: Unified Container (Recommended)** | **Render** | **100% Free** | ⭐ (1 Click) | Immediate portfolio presentation (single URL, zero CORS, zero configuration) |
| **Option 2: Distributed Micro-Service** | **Vercel** (Frontend) + **Render** (Backend) | **100% Free** | ⭐⭐⭐ (Two services) | Professional multi-tier production simulation |

---

## Option 1: Unified Full-Stack Deployment on Render (Recommended)

This approach uses the root `Dockerfile` to compile the React frontend, package it inside the Spring Boot JAR, and serve both from **a single free URL** (e.g., `https://team-expense-system.onrender.com`).

### Step-by-Step Instructions:

1. **Sign in to Render**:
   - Go to [render.com](https://render.com) and click **Sign in with GitHub**.

2. **Create New Web Service**:
   - Click the **New +** button in the dashboard and select **Web Service**.
   - Select your repository: **`praptidhote/team-expense-management-system`**.

3. **Configure the Service**:
   - **Name**: `team-expense-management-system` (or any name you prefer)
   - **Region**: Choose the closest region (e.g., *Singapore*, *Frankfurt*, or *Ohio*)
   - **Branch**: `main`
   - **Root Directory**: Leave blank (uses repo root)
   - **Runtime**: Select **`Docker`** (Render automatically detects the root `Dockerfile`)
   - **Instance Type**: Select **`Free`**

4. **Add Optional Environment Variables** (under *Advanced*):
   - `JWT_SECRET`: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`
   - `UPLOAD_DIR`: `uploads/receipts`

5. **Deploy**:
   - Click **Create Web Service**.
   - Render will build the multi-stage Docker container (React build &rarr; Spring Boot packaging &rarr; Runtime container).
   - In approximately 3–4 minutes, your service will display **`Live`**.
   - Click your custom URL (e.g. `https://team-expense-management-system.onrender.com`) to open your live application!

> [!NOTE]
> Render free-tier web services spin down after 15 minutes of inactivity. When a recruiter visits the link for the first time, it may take ~40 seconds for the container to wake up.

---

## Option 2: Distributed Deployment (Frontend on Vercel + Backend on Render)

If you prefer to showcase a decoupled cloud architecture with the React SPA hosted on an edge CDN and the Spring Boot API hosted as a dedicated backend:

### Part A: Deploy the Backend on Render
1. Go to [render.com](https://render.com) &rarr; **New +** &rarr; **Web Service**.
2. Select repository: `praptidhote/team-expense-management-system`.
3. Configure:
   - **Root Directory**: Leave blank (or use root Dockerfile)
   - **Runtime**: `Docker`
   - **Instance Type**: `Free`
4. Copy your backend URL once live: e.g., `https://expense-api.onrender.com`.

---

### Part B: Deploy the Frontend on Vercel
1. Go to [vercel.com](https://vercel.com) and sign in with GitHub.
2. Click **Add New...** &rarr; **Project**.
3. Import **`praptidhote/team-expense-management-system`**.
4. Configure Project Settings:
   - **Framework Preset**: `Vite`
   - **Root Directory**: Click *Edit* and select **`frontend`**
5. **Add Environment Variable**:
   - Key: **`VITE_API_URL`**
   - Value: `https://expense-api.onrender.com` *(your backend URL from Part A, without trailing slash)*
6. Click **Deploy**.
7. In ~45 seconds, Vercel will assign a production URL (e.g., `https://team-expense-system.vercel.app`).
   - The included `frontend/vercel.json` automatically manages client-side SPA route rewrites for React Router.

---

## Connecting an External Cloud MySQL Database (Optional)

By default, the application runs on a persistent, zero-configuration embedded database. If you wish to connect an external managed MySQL instance (such as from **Aiven.io**, **Railway**, or **AWS RDS**):

1. Provision a free MySQL database instance on [Aiven.io](https://aiven.io) or [Railway.app](https://railway.app).
2. In your Render or Railway dashboard, add the following environment variables:

```properties
DB_URL=jdbc:mysql://<your-db-host>:<port>/<db_name>?createDatabaseIfNotExist=true&useSSL=true&allowPublicKeyRetrieval=true
DB_USERNAME=<your-db-username>
DB_PASSWORD=<your-db-password>
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_PLATFORM=org.hibernate.dialect.MySQLDialect
```

3. Restart the service. Spring Boot Data JPA will automatically connect and update schema tables on startup.
