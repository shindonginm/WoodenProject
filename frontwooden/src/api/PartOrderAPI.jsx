import axios_api from "./axios";

const host = `/buyer/partorder`;

// 전체 발주 조회
export const getPartOrders = async () => {
  const res = await axios_api.get(host);
  return res.data;
};

// 완료된 발주 리스트
export const getCompletedPartOrders = async () => {
  const { data } = await axios_api.get(`${host}/completed`);
  return data;
}; 

// 등록
export const createPartOrder = async (formData) => {
  const res = await axios_api.post(host, formData);
  return res.data;
};

// 수정
export const updatePartOrder = async (poNo, formData) => {
  const res = await axios_api.put(`${host}/${poNo}`, formData);
  return res.data;
};

// 삭제
export const deletePartOrder = async (poNo) => {
  const res = await axios_api.delete(`${host}/${poNo}`);
  return res.data;
};

