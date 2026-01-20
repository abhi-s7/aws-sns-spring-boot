# AWS SNS Setup Guide

This document outlines the end-to-end process for setting up AWS SNS access for the **AWS-SNS-Spring-Boot** application.

---

## What is Amazon SNS?

**Amazon SNS (Simple Notification Service)** is AWS's fully managed pub/sub messaging service for application-to-application (A2A) and application-to-person (A2P) communication. Publishers send messages to topics, and all subscribers receive those messages. Common uses include fan-out scenarios, email/SMS notifications, mobile push notifications, and event-driven architectures.

---

## 1. Create an IAM User (Identity)

We create a dedicated IAM user with programmatic access instead of using root credentials for security best practices.

**Steps:**
1.  Go to **IAM** → **Users** → **Create user**.
2.  **User name**: `sns-spring-boot-user`
3.  **Access type**: Enable **Programmatic access** (this generates Access Keys).
4.  Click **Next: Permissions**.

---

## 2. Attach SNS Permissions Policy

The user needs permission to perform SNS operations (create topics, subscribe emails, publish messages, delete topics).

### Option A: Full SNS Access (Development/Testing)
1.  Select **Attach existing policies directly**.
2.  Search for `AmazonSNSFullAccess`.
3.  Check the box and click **Next: Tags** → **Next: Review** → **Create user**.

### Option B: Custom Minimal Policy (Production - Recommended)
If you want tighter security, create a custom policy with only required permissions:

**Steps:**
1.  Go to **IAM** → **Policies** → **Create policy**.
2.  Click **JSON** tab and paste the following policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sns:CreateTopic",
                "sns:DeleteTopic",
                "sns:ListTopics",
                "sns:Subscribe",
                "sns:Publish",
                "sns:GetTopicAttributes"
            ],
            "Resource": "*"
        }
    ]
}
```

3.  Click **Next: Tags** → **Next: Review**.
4.  **Policy name**: `SNS-Spring-Boot-Custom-Policy`
5.  Click **Create policy**.
6.  Go back to your user → **Add permissions** → **Attach policies directly** → Select your custom policy.

---

## 3. Generate Access Keys

After creating the user, you need to generate access keys for programmatic access. AWS will show you the **Access Key ID** and **Secret Access Key** ONCE. You must save these immediately.

**Steps:**
1.  Go to **IAM** → **Users** → Select your user (`sns-spring-boot-user`).
2.  Click **Security credentials** tab.
3.  Scroll to **Access keys** section → Click **Create access key**.
4.  Select **Use case**: Choose **Application running outside AWS**.
5.  Click **Next** → Add description (optional) → Click **Create access key**.
6.  AWS displays:
    *   **Access key ID**: `AKIAIOSFODNN7EXAMPLE`
    *   **Secret access key**: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`
7.  Click **Download .csv file** to save these credentials.
8.  **Important**: Keep these secure. Never commit them to version control.

**If you lose the keys:**
*   You cannot retrieve the secret key again.
*   You must create a new access key pair.
*   Delete old keys if no longer needed for security.

---

## 4. Understanding SNS Topics and Subscriptions

**Topic Types:**
*   **Standard Topic**: Maximum throughput, best-effort message ordering
*   **FIFO Topic**: First-In-First-Out ordering, exactly-once message delivery (topic name must end with `.fifo`)

**This application creates Standard topics by default.**

**Key Concepts:**

**Topics:**
*   Central communication channel for messages
*   Identified by unique ARN (Amazon Resource Name)
*   Format: `arn:aws:sns:region:account-id:topic-name`
*   Can have multiple subscribers

**Subscriptions:**
*   Email subscriptions require confirmation via email link
*   Subscription remains "Pending" until confirmed
*   Each email receives an AWS confirmation email with confirmation link
*   After confirmation, subscriber receives all published messages

**Publishing:**
*   SNS is push-based (unlike SQS which is pull-based)
*   Messages instantly delivered to all confirmed subscribers
*   Supports subject and message body
*   Maximum message size: 256 KB

**Message Delivery:**
*   Messages delivered to all confirmed subscriptions simultaneously
*   Failed deliveries can be retried (configurable)
*   Delivery status tracking available

---

## 5. Configure Application Environment Variables

Now that you have your AWS credentials, configure the application to use them.

**Steps:**

1.  **Navigate to project directory**:
    ```bash
    cd aws-sns-spring-boot
    ```

2.  **Copy the example file**:
    ```bash
    cp .env.example .env
    ```

3.  **Edit the `.env` file**:
    ```bash
    # On Mac/Linux
    nano .env
    
    # On Windows
    notepad .env
    ```

4.  **Fill in your AWS credentials**:
    ```env
    AWS_SNS_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
    AWS_SNS_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    AWS_SNS_REGION=us-west-1
    ```

    *Replace the example values with your actual credentials from Step 3.*

5.  **Save and close** the file.

**Security Note:**
*   The `.env` file is already added to `.gitignore` to prevent accidental commits.
*   Never share your Secret Access Key publicly or commit it to version control.
*   If you accidentally expose your keys, delete them immediately in IAM and create new ones.

---

## 6. Verify Configuration

**1. Start the application:**
```bash
./mvnw spring-boot:run
```

**2. Access the UI:**
```
http://localhost:8081
```

**3. Create a test topic:**
*   Enter a topic name (e.g., `test-topic-2026`)
*   Click "Create Topic"
*   You should see a success message with the topic ARN

**4. Verify in AWS Console:**
*   Go to **SNS** in AWS Console
*   You should see your newly created topic

---

## 7. Manual Topic and Subscription Setup (Optional)

You can also create topics and subscriptions manually from AWS Console before using the application.

### Creating a Topic Manually

1.  Go to **SNS** in AWS Console
2.  Click **Topics** → **Create topic**
3.  **Type**: Standard
4.  **Name**: `gift-card-notifications` (or any name)
5.  Leave other settings as default
6.  Click **Create topic**
7.  Copy the **Topic ARN** (e.g., `arn:aws:sns:us-west-1:123456789012:gift-card-notifications`)

### Creating a Subscription Manually

1.  Go to your topic in SNS Console
2.  Click **Create subscription**
3.  **Protocol**: Email
4.  **Endpoint**: Enter your email address
5.  Click **Create subscription**
6.  Check your email for confirmation message from AWS
7.  Click the confirmation link in the email
8.  Subscription status changes from "Pending confirmation" to "Confirmed"

**Note**: You can also create topics and subscriptions programmatically using the application UI.

---

## 8. Common SNS Operations

**Create Topic:**
*   Topic names must be unique within your AWS account and region
*   Can contain alphanumeric characters and hyphens
*   Maximum 256 characters
*   Application creates Standard topics by default

**Subscribe Email:**
*   Email must be valid and accessible
*   AWS sends confirmation email immediately
*   Subscription active only after confirmation
*   Confirmation link expires after 3 days

**Publish Notification:**
*   Requires topic ARN
*   Supports subject line (appears in email subject)
*   Message body supports plain text or JSON
*   Delivered to all confirmed subscriptions instantly

**Delete Topic:**
*   Permanently deletes topic and all subscriptions
*   Cannot be undone
*   Subscribers no longer receive notifications

---

## 9. Troubleshooting

### Error: "Access Denied"
*   **Cause**: Invalid credentials or insufficient IAM permissions.
*   **Fix**: 
    1.  Verify your Access Key and Secret Key in `.env`.
    2.  Check IAM user has SNS permissions attached.
    3.  Ensure no typos in credentials (no extra spaces).

### Error: "Topic does not exist"
*   **Cause**: Topic ARN is incorrect or doesn't exist in the specified region.
*   **Fix**: 
    1.  Verify topic ARN is correct (starts with `arn:aws:sns:`).
    2.  Check `AWS_SNS_REGION` matches topic region.
    3.  List topics to see available topics.

### Error: "The security token included in the request is invalid"
*   **Cause**: Access key has been deleted or deactivated in IAM.
*   **Fix**: Create new access keys in IAM console and update `.env`.

### Error: "AWS was not able to validate the provided access credentials"
*   **Cause**: Secret key is incorrect or contains extra characters.
*   **Fix**: Re-download credentials CSV and carefully copy the secret key (watch for trailing spaces).

### Email Not Receiving Notifications
*   **Cause**: Subscription not confirmed or email blocked.
*   **Fix**: 
    1.  Check email spam/junk folder for confirmation email.
    2.  Ensure subscription status is "Confirmed" in AWS Console.
    3.  Re-subscribe if confirmation link expired.

### Subscription Stuck in "Pending Confirmation"
*   **Cause**: Confirmation email not clicked or expired.
*   **Fix**: 
    1.  Check email inbox and spam folder.
    2.  Confirmation link valid for 3 days.
    3.  Delete subscription and create new one if expired.

### Region Mismatch Error
*   **Cause**: Topic exists in different region than specified.
*   **Fix**: Update `AWS_SNS_REGION` in `.env` to match topic region.

---

## 10. IAM Role for EC2 (For Deployment)

When deploying to EC2, create an IAM role instead of using access keys.

**1. Create IAM Role for EC2**

1.  Go to **IAM** → **Roles** → **Create role**.
2.  **Trusted entity type**: AWS service
3.  **Use case**: EC2
4.  Click **Next**.
5.  **Permissions**: Search and select `AmazonSNSFullAccess`
6.  Click **Next**.
7.  **Role name**: `ec2-sns-role`
8.  Click **Create role**.

**Note**: Attach this role to your EC2 instance when launching. See README.md for complete EC2 deployment steps.

---

## 11. Best Practices

*   **Never hardcode credentials**: Use environment variables or IAM roles.
*   **Use IAM roles on EC2**: Attach an IAM role instead of access keys when deploying to EC2.
*   **Confirm subscriptions promptly**: Email subscriptions expire after 3 days.
*   **Use meaningful topic names**: Descriptive names help identify purpose.
*   **Monitor topic metrics**: Track published messages and delivery success.
*   **Choose correct region**: Select closest to your application for lower latency.
*   **Enable encryption**: Use server-side encryption for sensitive data.
*   **Set delivery policies**: Configure retry attempts for failed deliveries.
*   **Use filter policies**: Allow subscribers to receive only relevant messages.
