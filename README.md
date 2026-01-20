# AWS SNS Spring Boot Integration

A Spring Boot web application demonstrating Amazon SNS operations through a responsive UI. Supports topic creation, email subscriptions, notification publishing with gift card details, and topic management using AWS SDK v2.

---

## AWS SNS Setup Guide

Before running this application, set up AWS SNS access and credentials.

**Complete setup instructions: [AWS SNS Setup Guide](AWS-SNS-Guide.md)**

**The guide covers:**
*   What is Amazon SNS and its use cases
*   IAM User creation and permissions
*   Access key generation
*   Environment configuration
*   IAM role setup for EC2
*   Manual topic and subscription setup
*   Troubleshooting common issues

---

## Features

*   Create SNS topics dynamically
*   Subscribe email addresses to topics
*   Publish gift card notifications to topics
*   Delete topics
*   Email notifications with formatted gift card details

**Access**: `http://localhost:8081`

---

## Architecture

**Backend** (Spring Boot):
*   `AwsConfig.java` - Initializes SNS client with credentials or IAM role
*   `AwsSnsService.java` - Handles SNS operations (create topic, subscribe, publish, delete)
*   `AwsSnsController.java` - REST API endpoints at `/api/sns/*`
*   `AwsSnsIntegrationWithSpringBootApplication.java` - Main application with .env loader

**Models**:
*   `GiftCard.java` - Data model for gift card notifications
*   `SnsNotification.java` - Wrapper for SNS notification details

**Frontend** (HTML/CSS/JS):
*   `index.html` - Topic management and subscription page
*   `notify.html` - Notification publishing page
*   `style.css` - Responsive styling
*   `app.js` - Topic management operations
*   `notify.js` - Notification publishing operations with Axios

**API Endpoints:**
*   `POST /api/sns/create-topic?topicName={name}`
    *   Creates a new SNS topic and returns topic ARN
*   `GET /api/sns/list-topics`
    *   Lists all available topics in the region
*   `POST /api/sns/subscribe?topicArn={arn}&email={email}`
    *   Subscribes an email address to a topic (requires confirmation)
*   `POST /api/sns/publish`
    *   Publishes gift card notification to topic subscribers
*   `DELETE /api/sns/delete-topic?topicArn={arn}`
    *   Deletes the entire topic and all subscriptions

---

## Local Setup

**1. Clone the repository**
```bash
git clone <repository-url>
cd aws-sns-spring-boot
```

**2. Configure environment variables**

Follow the **[AWS SNS Setup Guide](AWS-SNS-Guide.md)** to get credentials, then:
```bash
cp .env.example .env
```

Edit `.env` with your AWS credentials:
```env
AWS_SNS_ACCESS_KEY=your-access-key
AWS_SNS_SECRET_KEY=your-secret-key
AWS_SNS_REGION=us-west-1
```

**3. Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Access at `http://localhost:8081`

---

## Usage

### Topic Management Page (`/`)

**Create Topic**: Enter a unique topic name and click Create Topic.

**List Topics**: Click "Refresh Topic List" to view all topics.

**Subscribe Email**: Enter topic ARN and email address to subscribe (email will receive confirmation link).

**Delete Topic**: Enter topic ARN and click Delete Topic.

### Send Notification Page (`/notify.html`)

**Select Topic**: Enter topic ARN or click "Load Available Topics" to select from list.

**Gift Card Form**:
*   Enter user name, select gift card type, enter amount and date
*   Enter email subject (default: "Gift Card Delivery Notification")
*   Click "Send Email Notification" to publish to all subscribers

---

## How Notifications Work

**Notification Format:**

The application publishes formatted email messages with gift card details:

```
Gift Card Notification

User: Abhishek Kumar
Gift Card Type: Amazon
Amount: $150.00
Date: 2026-01-20

This gift card has been processed and will be delivered soon.
```

**Publishing Flow:**
1. Fill form with gift card details
2. Select topic ARN
3. Click "Send Email Notification"
4. Backend formats data into email message
5. Message published to SNS topic
6. All subscribed emails receive the notification

**Subscription Flow:**
1. Enter topic ARN and email address
2. Click "Subscribe Email"
3. AWS sends confirmation email to the address
4. Click confirmation link in email to activate subscription
5. Future notifications will be delivered to this email

**Important Notes:**
*   Email subscriptions require confirmation via email link
*   All subscribers receive every published notification
*   Unsubscribe link included in every notification email

---

## Tech Stack

*   Spring Boot 3.5.9, Java 21, Maven
*   AWS SDK v2.29.3 (SNS, Auth, Regions)
*   dotenv-java 3.0.0
*   Frontend: HTML5, CSS3, Axios

---

## EC2 Deployment

The application supports deployment on EC2 instances using IAM roles instead of access keys.

**Local Development**: Uses IAM User with Access Keys (stored in `.env`)

**EC2 Production**: Uses IAM Role (no keys needed, more secure)

**Benefit**: Same JAR file works in both environments automatically.

---

### Automated Deployment (Recommended)

**First Time:**
```bash
cp deploy-first-time.sh.example deploy-first-time.sh
nano deploy-first-time.sh  # Update EC2_HOST and KEY_FILE
chmod +x deploy-first-time.sh
./deploy-first-time.sh
```

**Updates:**
```bash
cp deploy.sh.example deploy.sh
nano deploy.sh  # Update EC2_HOST and KEY_FILE
chmod +x deploy.sh
./deploy.sh
```

---

### Manual Deployment Steps

**1. Create IAM Role for EC2**

See **[AWS SNS Setup Guide - Section 10](AWS-SNS-Guide.md#10-iam-role-for-ec2-for-deployment)** for IAM role creation.

**2. Launch EC2 Instance**

1.  Go to **EC2** → **Launch instance**.
2.  **Name**: `sns-spring-boot-app`
3.  **AMI**: Amazon Linux 2023 or Ubuntu 22.04
4.  **Instance type**: t3.micro (free tier eligible)
5.  **Key pair**: Create new or select existing
6.  **Network settings** - Configure Security Group:
    *   Allow SSH (port 22) from your IP
    *   Allow Custom TCP (port 8081) from anywhere (0.0.0.0/0)
7.  **Advanced details** → **IAM instance profile**: Select `ec2-sns-role`
8.  Click **Launch instance**.

**3. Install Java on EC2**

SSH into your instance:
```bash
ssh -i your-key.pem ec2-user@your-ec2-public-ip
```

Install Java 21:
```bash
# Amazon Linux 2023
sudo yum install java-21-amazon-corretto -y

# Ubuntu
sudo apt update
sudo apt install openjdk-21-jdk -y

# Verify
java -version
```

**4. Build and Upload JAR**

On your local machine:
```bash
# Build JAR
./mvnw clean package

# Upload to EC2
scp -i your-key.pem target/aws-sns-spring-boot-1.0.0-SNAPSHOT.jar ec2-user@your-ec2-ip:/home/ec2-user/
```

**5. Run Application on EC2**

SSH into EC2 and run:
```bash
java -jar aws-sns-spring-boot-1.0.0-SNAPSHOT.jar
```

**Note**: No `.env` file needed on EC2. The application automatically detects the IAM role and uses it for authentication.

**Access your application:**
```
http://your-ec2-public-ip:8081
```

**6. Run as Background Service (Optional)**

Create systemd service file:
```bash
sudo nano /etc/systemd/system/sns-app.service
```

Add:
```ini
[Unit]
Description=AWS SNS Spring Boot Application
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user
ExecStart=/usr/bin/java -jar aws-sns-spring-boot-1.0.0-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable sns-app
sudo systemctl start sns-app
sudo systemctl status sns-app
```

View logs:
```bash
sudo journalctl -u sns-app -f
```

**7. Managing the Service**

```bash
# Stop the service
sudo systemctl stop sns-app

# Start the service
sudo systemctl start sns-app

# Restart the service
sudo systemctl restart sns-app

# Check status
sudo systemctl status sns-app

# View logs (follow)
sudo journalctl -u sns-app -f

# Disable auto-start on boot
sudo systemctl disable sns-app
```

---

## Troubleshooting

See **[AWS SNS Setup Guide - Troubleshooting](AWS-SNS-Guide.md#9-troubleshooting)** for detailed solutions.

*   **Access Denied**: Check credentials and IAM permissions
*   **Topic not found**: Ensure topic ARN is correct and matches region
*   **Region mismatch**: Match `.env` region with topic region
*   **Subscription not working**: Check email for confirmation link
*   **Deleted topic still showing**: AWS takes up to 60 seconds to propagate deletion across all servers
*   **Build fails**: Run `./mvnw clean install`
