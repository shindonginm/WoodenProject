import axios_api from "./axios";

// 드롭다운 아이템 목록
export const getItems = async () => {
  const { data } = await axios_api.get("/plan/itemlist/main");
  return data; // [{itemNo, itemName}]
};

// 예측 그래프용 시계열 조회
export const getForecastSeries = async (itemNo, h = 12) => {
  const { data } = await axios_api.get("/forecast/series", {
    params: { itemNo, h },
  });
  return data;
};

export const applyForecastToDemand = async (itemNo, h = 12) => {
  const { data } = await axios_api.post("/forecast/apply", null, {
    params: { itemNo, h },
  });
  // data = 생성된 DemandPlan 건수(int)
  return data;
};
