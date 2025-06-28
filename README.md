🇰🇭 KHQR Payment — Real-Time Cambodian QR Payment System

A modern KHQR (Cambodia QR) payment solution with real-time updates, seamless integration, and full-stack architecture using Spring Boot and Nuxt 3.


🚀 Overview

KHQR Payment is a full-stack project designed to simplify digital payments in Cambodia using the KHQR standard — with real-time notifications and a modern tech stack.

This system connects banks (like ABA, ACLEDA, Wing, etc.) via QR code, tracks the status in real-time, and gives instant feedback to users.

🧩 Key Features

🏦 KHQR Payment Integration — based on the Cambodian banking QR standard.

📡 Real-Time Notifications — via WebSocket (STOMP) for instant updates.

🔗 Full-Stack Architecture — built with Spring Boot (Java) & Nuxt 3 (Vue).

💳 Seamless User Flow — from QR generation to payment confirmation.

🔐 Optional Security — via Spring Security and token protection.

🧪 Workflow

1. User initiates payment via Nuxt frontend.

2. Backend generates a KHQR QR code and returns it.

3. Frontend displays QR to the user.

4. User scans QR using a banking app (ABA, ACLEDA, Wing, etc.).

5. Backend detects payment and sends real-time update via WebSocket.

6. Frontend updates UI instantly to reflect payment success.

📦 Tech Stack

🔙 Backend (Spring Boot)

· spring-boot-starter-websocket

· spring-boot-starter-security (optional)

· KHQR logic and payment detection (https://bakong.nbc.gov.kh/en/download/KHQR/integration/KHQR%20SDK%20Document.pdf)

· Real-time communication via STOMP/WebSocket

🔜 Frontend (Nuxt.js 3)

· @stomp/stompjs

· sockjs-client

· WebSocket connection for real-time payment updates

· QR code generation and display

⚡ Real-Time

· WebSocket + STOMP protocol for bi-directional communication

🖼️ QR Code Generator

· Use any standard QR library or a custom QR microservice

📸 UI Preview (optional)

![Screenshot (34)](https://github.com/user-attachments/assets/22709d09-0c49-40c4-a151-6e67880ccabb)
