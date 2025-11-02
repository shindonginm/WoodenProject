import axios_api from "./axios";
import { BASE_URL } from "./config";

const host = `${BASE_URL}/stock/item`;

// 전체 재고 목록
export const getItemStockList = async () => {
  const res = await axios_api.get(host);
  return res.data;
};

// 단건 조회
export const getItemStockOne = async (itemNo) => {
  const res = await axios_api.get(`${host}/${itemNo}`);
  return res.data;
};

// 생산/판매 반영
export const adjustItemStock = async ({ itemNo, delta }) => {
  const {data} = await axios_api.patch(`${host}/adjust`, {
    itemNo: Number(itemNo),
    delta: Number(delta),
  });
  return data;
};
