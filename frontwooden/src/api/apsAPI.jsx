import axios_api from "./axios";
import { BASE_URL } from "./config";

const host = `${BASE_URL}/aps`;

export const runAps = async (horizonWeeks = 12) => {
  const res = await axios_api.post(`${host}/run`, null, { params: { horizonWeeks } });
  return res.data;
};

export const getPlannedOrders = async () => {
  const res = await axios_api.get(`${host}/planned-orders`);
  return res.data;
};

export const releasePlannedOrder = async (planId) => {
  const res = await axios_api.post(`${host}/planned-orders/${planId}/release`);
  return res.data;
};
