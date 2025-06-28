<template>
  <div class="p-4 space-y-4">
    <button
      @click="startPayment"
      class="bg-blue-500 text-white px-4 py-2 rounded"
    >
      Generate QR
    </button>

    <div v-if="qr">
      <p>Scan this QR:</p>
      <img
        :src="`https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(
          qr
        )}&size=200x200`"
        alt="QR Code"
        class="border rounded"
      />
    </div>

    <p>Status: {{ paymentStatus }}</p>

    <div v-if="statusMessage">
      <p class="text-green-600 font-bold">{{ statusMessage }}</p>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from "vue";
import useKhqrWebSocket from "~/composables/useWebSocket";

const qr = ref("");
const md5 = ref("");
const paymentStatus = ref("WAITING");
const statusMessage = ref("");
let fallbackTimer: NodeJS.Timeout | null = null;
let disconnectWebSocket: (() => void) | null = null;

const startPayment = async () => {
  const txId = "TX" + Date.now();

  const { data, error }: any = await useFetch(
    "http://localhost:8080/api/payment/generate-khqr",
    {
      method: "POST",
      body: {
        amount: 100,
        transactionId: txId,
      },
    }
  );

  if (error.value) {
    console.error("❌ Failed to generate QR:", error.value);
    return;
  }

  qr.value = data.value.qr;
  md5.value = data.value.md5;
};

watch(md5, (newMd5) => {
  if (!newMd5) return;

  // Clean up previous connections
  if (disconnectWebSocket) disconnectWebSocket();
  if (fallbackTimer) clearInterval(fallbackTimer);

  const { connect, disconnect } = useKhqrWebSocket(newMd5, (status) => {
    paymentStatus.value = status;
    statusMessage.value = "✅ Payment received via WebSocket";
    cleanup();
  });

  disconnectWebSocket = disconnect;
  connect();

  // Fallback polling
  fallbackTimer = setInterval(async () => {
    try {
      // FIX: Remove destructuring and access data directly
      const response = await $fetch(
        "http://localhost:8080/api/payment/check-payment",
        {
          method: "POST",
          body: { md5: newMd5 },
        }
      );

      // Access response directly without .value
      if (response?.responseCode === 0 && response?.responseMessage === "Success") {
        paymentStatus.value = "PAID";
        statusMessage.value = "✅ Payment confirmed via polling";
        cleanup();
      } else {
        console.log("⌛ Waiting for payment...");
      }
    } catch (error) {
      console.error("Payment check error:", error);
    }
  }, 5000);
});

function cleanup() {
  if (fallbackTimer) {
    clearInterval(fallbackTimer);
    fallbackTimer = null;
  }
  if (disconnectWebSocket) {
    disconnectWebSocket();
    disconnectWebSocket = null;
  }
}

onBeforeUnmount(() => {
  cleanup();
});
</script>
