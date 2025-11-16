# User Management & Authentication Module
### Enterprise-Grade Security · JWT + Refresh Tokens · OTP Login · Device Sessions · RBAC Permissions

A fully featured, production-ready authentication and user management module built with **Spring Boot**, designed for enterprise systems requiring strong security, scalability and modularity.  
This module can be easily plugged into any microservice-based or monolithic application.

---

## 🔥 Features

### 1) Authentication & Authorization
- Email/username + password login
- OTP login (passwordless option available)
- Refresh Token rotation & generation
- HTTP-only Secure cookies for access/refresh tokens
- Role-Based Access Control (RBAC)
- Permission-level authorization (fine-grained)
- Permission caching layer for performance

---

## 🔐 Security Highlights

### 2) Advanced Rate Limiting
Built-in 2-layer protection:
- **OTP resend limit** (username/IP-based)
- **Login attempt limiter** to block brute-force attacks
- Centralized rate-limit infrastructure

### 3) Account Lockout Policies
- Auto-lock user after excessive login failures
- Automatic unlock after configurable duration
- Admin can unlock manually

---

## 📱 Device & Session Management

### 4) Device Session Tracking
Each login registers:
- userId
- deviceId
- IP
- user agent
- createdAt
- active/inactive status

Admin endpoints available to:
- List user sessions
- Revoke a specific device session
- Enforce session concurrency policies

Supports:
- **DENY_NEW** (prevent new logins when limit reached)
- **KICK_OLDEST** (logout oldest device)

---

## 🔒 Token Blacklisting (Logout Security)
- Access tokens stored in distributed cache
- Blacklisted JWTs become unusable immediately
- Called automatically from `/logout` API
- No race conditions on multi-device logout

---

## 🛡 Password Security

### 5) Password Policies
- Enforces minimum strength
- Checks password history
- Prevents reusing previous passwords

### 6) Force Password Change
When reset by admin, user must change password at next login.

---

## 📜 Audit Logging
All critical events are logged:
- Login success
- Login failure
- OTP send attempts
- Password changes
- Session revocations

Stored with:
- timestamp
- userId
- username
- IP address
- user-agent
- event type

Ideal for compliance-ready systems.

---

## 🧩 Multi-Tenant Ready
Although the module is single-tenant by default, it includes:
- tenant-aware user model extension points
- tenant context resolver
- multi-tenant permission model reserved

---

## ⚡ Performance Features

### Permission Cache
- Cached per user
- Cache invalidation on role/permission update
- Avoids heavy join queries on each request
- Endpoint: `/auth/permissions`

---

## 🌐 Full JWT Authentication Flow

### Access Token
- short-lived (configurable)
- stored in HTTP-only Secure Cookie

### Refresh Token
- long-lived
- stored server-side in DB
- must not be leaked
- rotated per login

---

## 📦 Installation

### Prerequisites
- Java 17+
- Spring Boot 3.2+
- PostgreSQL / MySQL / Oracle (supported)
- Redis (optional but recommended for token blacklist)

### Clone the project
```bash
git clone https://github.com/yourrepo/auth-module.git
cd auth-module
```

### Build
```bash
mvn clean install
```

---

## ⚙ Environment Variables

```
application.security.jwt.secret-key=<base64 hmac key>
application.security.jwt.expiration=900000
application.security.jwt.refresh-token.expiration=1296000000
application.security.jwt.cookie-name=ACCESS_TOKEN
application.security.jwt.refresh-token.cookie-name=REFRESH_TOKEN

app.otp.resend.max=3
app.otp.resend.window.seconds=600

app.login.max.attempts=5
app.login.window.seconds=300

app.security.max.sessions.per.user=3
app.security.session.policy=KICK_OLDEST
```

---

## 🧪 API Endpoints (Summary)

### Authentication
```
POST /auth/login
POST /auth/login-otp
POST /auth/send-otp
POST /auth/refresh-token
POST /auth/logout
```

### Device Sessions
```
GET  /auth/device-sessions
DELETE /auth/device-sessions/{id}
```

### Permissions
```
GET /auth/permissions
```

### Password
```
POST /auth/change-password
POST /auth/reset-password
```

---

## 🏗 Architecture Overview

```
┌────────────────────┐
│ Authentication API │
└──────────┬─────────┘
           │
           ▼
┌──────────────────────────┐
│ Authentication Service   │
├──────────────────────────┤
│ - login with password    │
│ - passwordless OTP login │
│ - refresh token flow     │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ Security Layer (Filters) │
├──────────────────────────┤
│ - JWT Auth Filter        │
│ - Captcha Filter         │
│ - Rate Limit Filters     │
└──────────┬───────────────┘
           │
           ▼
┌────────────────────────┐
│ Domain Services        │
├────────────────────────┤
│ DeviceSessionService   │
│ AttemptService         │
│ OtpRateLimitService    │
│ TokenBlacklistService  │
│ PasswordPolicyService  │
│ PermissionCacheService │
└────────────────────────┘
```

---

## 🚀 Production Ready
This module has:
- Clear extension points
- High configurability
- Distributed-cache support
- Full auditability
- Strong security posture
- Modern enterprise-grade design

You can easily embed it into:
- Banking platforms
- Telecom systems
- CRM/ERP
- E-commerce
- Government systems
- SaaS platforms

---

## 📄 License
MIT — free for commercial use.

---

# 💬 Support
For feature requests or issues, open a GitHub Issue.