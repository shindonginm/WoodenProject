import React, { useEffect, useState } from "react";
import { runAps, getPlannedOrders, releasePlannedOrder } from "../../api/apsAPI";

// APS 계획 페이지
// - APS 실행 (N주)
// - 미확정 계획 목록 조회
// - 계획 확정 (PLAN -> WorkOrder, BUYER -> PartOrder)
const ApsPlanPage = () => {
  const [horizonWeeks, setHorizonWeeks] = useState(12); // 몇 주치 계산할지
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [running, setRunning] = useState(false);

  // 최초 진입 시 PLANNED 계획 목록 조회
  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    setLoading(true);
    try {
      const data = await getPlannedOrders();
      setPlans(data);
    } catch (err) {
      console.error(err);
      alert("계획 목록을 가져오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // APS 실행 버튼
  const handleRunAps = async () => {
    if (!window.confirm(`${horizonWeeks}주 기준으로 APS를 실행할까요?`)) return;
    setRunning(true);
    try {
      const count = await runAps(horizonWeeks);
      alert(`APS 실행 완료: 생성된 계획 건수 = ${count}건`);
      await fetchPlans();
    } catch (err) {
      console.error(err);
      alert("APS 실행 중 오류가 발생했습니다.");
    } finally {
      setRunning(false);
    }
  };

  // 개별 계획 확정 버튼
  const handleRelease = async (planId) => {
    if (!window.confirm(`계획 ${planId} 를 확정하시겠습니까?`)) return;
    try {
      await releasePlannedOrder(planId);
      alert("계획이 확정되었습니다. (PLAN: 작업지시 / BUYER: 실발주 생성)");
      await fetchPlans();
    } catch (err) {
      console.error(err);
      alert("계획 확정 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="page-wrapper">
      <h2 style={{ textAlign: "center" }}>APS 계획 관리</h2>

      {/* 상단 컨트롤 바: APS 실행 */}
      <div
        style={{
          margin: "12px 0",
          padding: "12px",
          border: "1px solid #ccc",
          borderRadius: 8,
        }}
      >
        <label>
          계획 기간(주):
          <input
            type="number"
            min={1}
            max={52}
            value={horizonWeeks}
            onChange={(e) => setHorizonWeeks(Number(e.target.value))}
            style={{ width: 60, marginLeft: 8, marginRight: 16 }}
          />
        </label>
        <button onClick={handleRunAps} disabled={running}>
          {running ? "APS 실행중..." : "APS 실행"}
        </button>
        <button
          onClick={fetchPlans}
          style={{ marginLeft: 8 }}
          disabled={loading}
        >
          새로고침
        </button>
      </div>

      {/* 계획 목록 테이블 */}
      <div>
        <h3>미확정 계획 목록 (status = PLANNED)</h3>
        {loading ? (
          <p>불러오는 중...</p>
        ) : plans.length === 0 ? (
          <p>미확정 계획이 없습니다.</p>
        ) : (
          <table
            style={{
              width: "100%",
              borderCollapse: "collapse",
              marginTop: 8,
            }}
          >
            <thead>
              <tr>
                <th style={thStyle}>ID</th>
                <th style={thStyle}>유형</th>
                <th style={thStyle}>품목/부품</th>
                <th style={thStyle}>납기일</th>
                <th style={thStyle}>수량</th>
                <th style={thStyle}>수요ID</th>
                <th style={thStyle}>비고</th>
                <th style={thStyle}>액션</th>
              </tr>
            </thead>
            <tbody>
              {plans.map((p) => (
                <tr key={p.planId}>
                  <td style={tdStyle}>{p.planId}</td>
                  <td style={tdStyle}>{p.planType}</td>
                  <td style={tdStyle}>
                    {p.planType === "PLAN"
                      ? `${p.itemNo ?? "-"} / ${p.itemName ?? "-"}`
                      : `${p.partNo ?? "-"} / ${p.partName ?? "-"}`}
                  </td>
                  <td style={tdStyle}>{p.deliDate}</td>
                  <td style={tdStyle}>{p.qty}</td>
                  <td style={tdStyle}>{p.demandId ?? "-"}</td>
                  <td style={tdStyle}>{p.remark ?? ""}</td>
                  <td style={tdStyle}>
                    <button onClick={() => handleRelease(p.planId)}>
                      확정(RELEASE)
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

const thStyle = {
  border: "1px solid #ddd",
  padding: "4px 8px",
  backgroundColor: "#f5f5f5",
};

const tdStyle = {
  border: "1px solid #ddd",
  padding: "4px 8px",
  textAlign: "center",
};

export default ApsPlanPage;
