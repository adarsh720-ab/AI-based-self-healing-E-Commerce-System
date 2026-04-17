# 🔧 Developer Configuration Required

This document lists all API keys, credentials, and configurations that **MUST** be provided by the developer before running the system in production or for full functionality.

---

## ✅ What Needs to Be Configured

### 1. **Payment Service - Stripe API Key**
**File:** `payment-service/src/main/resources/application.yml`  
**Line:** 37  
**Current Value:** `${STRIPE_API_KEY:sk_test_dummy}`

```yaml
stripe:
  api:
    key: ${STRIPE_API_KEY:sk_test_dummy}  # ❌ REPLACE WITH YOUR KEY
```

**Action Required:**
- Get your Stripe API key from [https://dashboard.stripe.com/apikeys](https://dashboard.stripe.com/apikeys)
- Set environment variable: `STRIPE_API_KEY=sk_test_your_actual_key_here`
- Or update the line to: `key: sk_test_your_actual_key_here`

**Why:** Without this, payment processing will fail.

---

### 2. **Notification Service - SendGrid API Key**
**File:** `notification-service/src/main/resources/application.yml`  
**Line:** 17  
**Current Value:** `${SENDGRID_API_KEY:SG.dummy}`

```yaml
sendgrid:
  api:
    key: ${SENDGRID_API_KEY:SG.dummy}  # ❌ REPLACE WITH YOUR KEY
```

**Action Required:**
- Get your SendGrid API key from [https://app.sendgrid.com/settings/api_keys](https://app.sendgrid.com/settings/api_keys)
- Set environment variable: `SENDGRID_API_KEY=SG.your_actual_key_here`
- Or update the line to: `key: SG.your_actual_key_here`

**Why:** Without this, email notifications won't be sent.

---

### 3. **Notification Service - Twilio Account SID**
**File:** `notification-service/src/main/resources/application.yml`  
**Line:** 20  
**Current Value:** `${TWILIO_ACCOUNT_SID:ACdummy}`

```yaml
twilio:
  account:
    sid: ${TWILIO_ACCOUNT_SID:ACdummy}  # ❌ REPLACE WITH YOUR SID
```

**Action Required:**
- Get your Twilio Account SID from [https://console.twilio.com](https://console.twilio.com)
- Set environment variable: `TWILIO_ACCOUNT_SID=ACyour_actual_sid_here`
- Or update the line to: `sid: ACyour_actual_sid_here`

**Why:** Without this, SMS notifications won't be sent.

---

### 4. **Notification Service - Twilio Auth Token**
**File:** `notification-service/src/main/resources/application.yml`  
**Line:** 22  
**Current Value:** `${TWILIO_AUTH_TOKEN:dummy}`

```yaml
twilio:
  auth:
    token: ${TWILIO_AUTH_TOKEN:dummy}  # ❌ REPLACE WITH YOUR TOKEN
```

**Action Required:**
- Get your Twilio Auth Token from [https://console.twilio.com](https://console.twilio.com)
- Set environment variable: `TWILIO_AUTH_TOKEN=your_actual_token_here`
- Or update the line to: `token: your_actual_token_here`

**Why:** Without this, Twilio authentication will fail.

---

### 5. **Notification Service - Twilio Phone Number**
**File:** `notification-service/src/main/resources/application.yml`  
**Line:** 24  
**Current Value:** `${TWILIO_PHONE_NUMBER:+1234567890}`

```yaml
twilio:
  phone:
    number: ${TWILIO_PHONE_NUMBER:+1234567890}  # ❌ REPLACE WITH YOUR PHONE
```

**Action Required:**
- Get your Twilio phone number from your Twilio account
- Set environment variable: `TWILIO_PHONE_NUMBER=+1234567890` (your actual number)
- Or update the line to: `number: +1234567890`

**Why:** This is the number from which SMS notifications will be sent.

---

## ⚙️ Optional: Environment Variables Setup

Create a `.env` file in the project root for easy configuration:

```bash
# Database
DB_HOST=localhost
DB_USER=enter your username
DB_PASS=enter your password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASS=

# Kafka
KAFKA_BOOTSTRAP=localhost:9092

# JWT (must be same across all services)
JWT_SECRET=mySecretKey123456789012345678901234567890AbcDef

# Stripe
STRIPE_API_KEY=sk_test_your_actual_key_here

# SendGrid
SENDGRID_API_KEY=SG.your_actual_key_here

# Twilio
TWILIO_ACCOUNT_SID=ACyour_actual_sid_here
TWILIO_AUTH_TOKEN=your_actual_token_here
TWILIO_PHONE_NUMBER=+1234567890

# Elasticsearch
ELASTICSEARCH_URI=http://localhost:9200

# User Service URL (for Feign)
USER_SERVICE_URL=http://localhost:8081
```

---

## 📋 Summary Table

| Service | Configuration | File | Line | Current Value | Status |
|---------|---------------|------|------|---------------|--------|
| Payment | Stripe API Key | `payment-service/src/main/resources/application.yml` | 37 | `sk_test_dummy` | ❌ Required |
| Notification | SendGrid API Key | `notification-service/src/main/resources/application.yml` | 17 | `SG.dummy` | ❌ Required |
| Notification | Twilio Account SID | `notification-service/src/main/resources/application.yml` | 20 | `ACdummy` | ❌ Required |
| Notification | Twilio Auth Token | `notification-service/src/main/resources/application.yml` | 22 | `dummy` | ❌ Required |
| Notification | Twilio Phone Number | `notification-service/src/main/resources/application.yml` | 24 | `+1234567890` | ❌ Required |

---

## 🚀 How to Apply These Configurations

### **Option 1: Environment Variables (Recommended)**
```bash
export STRIPE_API_KEY=sk_test_your_key
export SENDGRID_API_KEY=SG.your_key
export TWILIO_ACCOUNT_SID=ACyour_sid
export TWILIO_AUTH_TOKEN=your_token
export TWILIO_PHONE_NUMBER=+1234567890

# Then run services
mvn spring-boot:run -pl payment-service
mvn spring-boot:run -pl notification-service
```

### **Option 2: Docker Compose .env File**
```bash
# Create .env file in project root with all values
# Docker Compose will automatically load it
docker compose up -d
```

### **Option 3: Direct File Update**
Edit each configuration file and replace the dummy values directly (not recommended for production).

---

## ✨ What's NOT Required (Already Configured)

- ✅ PostgreSQL connection (defaults to localhost:5432)
- ✅ Redis connection (defaults to localhost:6379)
- ✅ Kafka bootstrap servers (defaults to localhost:9092)
- ✅ Elasticsearch URI (defaults to http://localhost:9200)
- ✅ JWT Secret (has a default development value)
- ✅ Service URLs (all default to localhost with correct ports)

---

## 🔒 Security Notes

⚠️ **Never commit API keys or credentials to Git!**

1. Add `.env` to `.gitignore`
2. Use GitHub Secrets for CI/CD pipelines
3. Store secrets in secure vaults (DigitalOcean, HashiCorp Vault, etc.)
4. Rotate keys regularly
5. Use different keys for dev, staging, and production

---

## 📞 Getting the Keys

| Service | How to Get Key | Website |
|---------|----------------|---------|
| **Stripe** | Create account → Settings → API Keys | [stripe.com](https://stripe.com) |
| **SendGrid** | Create account → Settings → API Keys | [sendgrid.com](https://sendgrid.com) |
| **Twilio** | Create account → Console → Settings | [twilio.com](https://twilio.com) |

---

**Last Updated:** March 23, 2026  
**Status:** 5 configurations required before production use

