import axios_api from "./axios";
import { BASE_URL } from "./config";

const host = `${BASE_URL}/workorders`;

export const listWorkOrders = async (status) => {
  const res = await axios_api.get(host, { params: status ? { status } : {} });
  return res.data;
};

export const createWorkOrderFromPlan = async (planId) => {
  const res = await axios_api.post(`${host}/from-plan/${planId}`);
  return res.data;
};

export const releaseWorkOrder = async (woNo) => {
  const res = await axios_api.patch(`${host}/${woNo}/release`);
  return res.data;
};

export const startWorkOrder = async (woNo) => {
  const res = await axios_api.patch(`${host}/${woNo}/start`);
  return res.data;
};

export const completeWorkOrder = async (woNo) => {
  const res = await axios_api.patch(`${host}/${woNo}/complete`);
  return res.data;
};

export const cancelWorkOrder = async (woNo) => {
  const res = await axios_api.patch(`${host}/${woNo}/cancel`);
  return res.data;
};
