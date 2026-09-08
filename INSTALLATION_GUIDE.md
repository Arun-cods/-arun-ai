# 📖 Arun AI — Commercial SaaS Setup & Deployment Manual
### *All-in-One Conversational AI Assistant & AI Photo Studio Platform*

---

## 🌟 1. System Overview & Key Features
- **⚡ Dual AI Architecture**:
  - **Zero-Key Standalone Engine**: Works out-of-the-box without requiring third-party API keys or paid subscriptions.
  - **Cloud Multi-Model Support**: High-performance streaming support for Google Gemini (Flash, Pro, Lightning).
- **🎨 AI Photo Studio (ChatGPT & Gemini Style)**:
  - Real face, beard, and hairstyle-preserving **Full Body Outpainting** (Half-to-Full portrait).
  - 1-Click **Attire Recoloring** (Executive Charcoal Black, Royal Navy Blue, Crimson Red, Studio White, Emerald Green).
  - High-Definition Studio Background replacements.
- **💳 Multi-Tier Payment & Monetization**:
  - **Direct PhonePe UPI QR Code**: Zero-commission direct bank payouts.
  - **Razorpay**: Domestic & international UPI, NetBanking, and credit/debit cards.
  - **Stripe**: Global multi-currency card processing.
- **📱 Universal PWA Support**:
  - Progressive Web App ready for 1-click home screen install on Android & iOS.
  - Ready for Google Play Store (TWA), Amazon Appstore, and Microsoft Store.

---

## 💻 2. System Requirements
- **Java**: Java 17 or Java 21+ (OpenJDK, Eclipse Temurin, or Oracle JDK).
- **RAM**: Minimum 1 GB (2 GB+ recommended for production).
- **Supported OS**: Windows 10/11, Ubuntu 20.04/22.04/24.04, Debian, macOS, or Docker.

---

## 🚀 3. Quick Start (Run Locally in 60 Seconds)

### On Windows:
1. Double-click start.bat.
2. Open your browser and navigate to: http://localhost:8080/.

### On Linux / macOS:
`ash
chmod +x start.sh
./start.sh
`
Open your browser at: http://localhost:8080/.

---

## 🐳 4. Deploy with Docker & Docker Compose
To run Arun AI in a clean isolated Docker container on any VPS:
`ash
# Start container in background
docker-compose up -d --build

# View real-time logs
docker-compose logs -f
`
The server will start listening on port 8080.

---

## 🌐 5. Production VPS Deployment (Ubuntu / Nginx / SSL)

### Step 1: Install Java 21 on Ubuntu
`ash
sudo apt update
sudo apt install -y openjdk-21-jre-headless nginx certbot python3-certbot-nginx
`

### Step 2: Set up Systemd Service for 24/7 Autostart
Create /etc/systemd/system/arunai.service:
`ini
[Unit]
Description=Arun AI SaaS Application
After=network.target

[Service]
User=root
WorkingDirectory=/var/www/arun-ai
ExecStart=/usr/bin/java -Xms256m -Xmx1024m -jar arun-ai-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
`
Enable and start the service:
`ash
sudo systemctl daemon-reload
sudo systemctl enable arunai
sudo systemctl start arunai
`

### Step 3: Nginx Reverse Proxy & Free SSL Certificate
Create /etc/nginx/sites-available/arunai:
`
ginx
server {
    server_name yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
        proxy_cache off;
    }
}
`
Enable site and get free SSL certificate:
`ash
sudo ln -s /etc/nginx/sites-available/arunai /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d yourdomain.com
`

---

## 💳 6. Configuring Your Own Payment Gateways

### 1. Update Direct PhonePe / UPI ID:
- Open src/main/resources/templates/index.html.
- Replace 9014218406@ybl with your own UPI ID.
- Replace src/main/resources/static/images/arun_phonepe_qr.jpg with your own UPI QR image.

### 2. Configure Razorpay:
- In src/main/resources/templates/index.html, set your key_id in the Razorpay options block:
`javascript
key: 'rzp_live_YOUR_KEY_HERE',
`

---

## 📱 7. Packaging for Amazon Appstore & Google Play
1. Deploy the app to your domain with HTTPS.
2. Go to **[PWABuilder.com](https://www.pwabuilder.com/)**.
3. Enter your live website URL and click **Start**.
4. Choose **Package for Stores** ➔ Select **Android**.
5. Download your signed APK/AAB bundle.
6. Submit to [Amazon Developer Console](https://developer.amazon.com) for 100% free distribution!

---
© 2026 Arun AI. All rights reserved. Commercial SaaS License.