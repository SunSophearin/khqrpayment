
# 🇰🇭 KHQR Payment
A modern KHQR (Cambodia QR) payment solution with real-time updates, seamless integration, and full-stack architecture using Spring Boot and Nuxt 3.

# 🚀 Overview
KHQR Payment is a full-stack project designed to simplify digital payments in Cambodia using the KHQR standard — with real-time notifications and a modern tech stack.

This system connects banks (like ABA, ACLEDA, Wing, etc.) via QR code, tracks the status in real-time, and gives instant feedback to users.

## 🧩 Key Features

- 🏦 KHQR Payment Integration — based on the Cambodian banking QR standard.
- 📡 Real-Time Notifications — via WebSocket (STOMP) for instant updates.
- 🔗 Full-Stack Architecture — built with Spring Boot (Java) & Nuxt 3 (Vue).
- 💳 Seamless User Flow — from QR generation to payment confirmation.


## Tech Stack

**Client:** Nuxtjs, TailwindCSS

**Server:** Spring boot


## Installation

Install For FrontEnd

```bash
  bun create nuxt <project-name>
  cd <project-name>
  bun i @stomp/stompjs
  bun i sockjs-client
```
Install For BackEnd

```bash
   You can access Spring Initializr at https://start.spring.io
```
Add dependencies KHQR

```bash
 <dependency>
    <groupId>kh.org.nbc.bakong_khqr</groupId>
    <artifactId>sdk-java</artifactId>
    <version>1.0.0.9</version>
 </dependency>
```
Add dependencies Websocket

```bash
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
 </dependency>
```
## Application Flow

1.User Click payment
![Screenshot (36)](https://github.com/user-attachments/assets/c47dac4c-8167-4cce-9968-c7a4a46045b8)


2.Payment Seccess
![Screenshot (34)](https://github.com/user-attachments/assets/35d9f0fa-c520-4993-a260-afee705b8541)


## 🔗 Links

- Nuxt 3 Documentation: https://nuxt.com/
- KHQR Documentation: https://www.npmjs.com/package/ts-khqr
