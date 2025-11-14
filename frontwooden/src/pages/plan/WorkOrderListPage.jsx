import React, { useEffect, useState } from "react";
import {
  listWorkOrders,
  releaseWorkOrder,
  startWorkOrder,
  completeWorkOrder,
  cancelWorkOrder,
} from "../../api/workOrderAPI";

// 작업지시 목록 페이지
// - 상태별 필터
// - 상태 전이 버튼 (release/start/complete/cancel)
const WorkOrderListPage = () => {
  const [statusFilter, setStatusFilter] = useState(""); // "", "PLANNED", ...
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchList();
  }, [statusFilter]);

  const fetchList = async () => {
    setLoading(true);
    try {
      const data = await listWorkOrders(statusFilter || undefined);
      setList(data);
    } catch (err) {
      console.error(err);
      alert("작업지시 목록 조회 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleChangeStatus = async (type, woNo) => {
    try {
      if (type === "release") {
        await releaseWorkOrder(woNo);
      } else if (type === "start") {
        await startWorkOrder(woNo);
      } else if (type === "complete") {
        await completeWorkOrder(woNo);
      } else if (type === "cancel") {
        await cancelWorkOrder(woNo);
      }
      await fetchList();
    } catch (err) {
      console.error(err);
      alert("상태 변경 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="page-wrapper">
      <h2 style={{ textAlign: "center" }}>작업지시 관리</h2>

      {/* 필터 영역 */}
      <div
        style={{
          margin: "12px 0",
          padding: "8px 12px",
          border: "1px solid #ccc",
          borderRadius: 8,
        }}
      >
        <span>상태 필터: </span>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">전체</option>
          <option value="PLANNED">PLANNED</option>
          <option value="RELEASED">RELEASED</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="DONE">DONE</option>
          <option value="CANCELED">CANCELED</option>
        </select>
        <button onClick={fetchList} style={{ marginLeft: 8 }} disabled={loading}>
          새로고침
        </button>
      </div>

      {/* 리스트 */}
      {loading ? (
        <p>불러오는 중...</p>
      ) : list.length === 0 ? (
        <p>작업지시가 없습니다.</p>
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
              <th style={thStyle}>WO No</th>
              <th style={thStyle}>Plan Id</th>
              <th style={thStyle}>품목</th>
              <th style={thStyle}>수량</th>
              <th style={thStyle}>납기일</th>
              <th style={thStyle}>상태</th>
              <th style={thStyle}>비고</th>
              <th style={thStyle}>액션</th>
            </tr>
          </thead>
          <tbody>
            {list.map((wo) => (
              <tr key={wo.woNo}>
                <td style={tdStyle}>{wo.woNo}</td>
                <td style={tdStyle}>{wo.plannedOrderId ?? "-"}</td>
                <td style={tdStyle}>
                  {wo.itemNo} / {wo.itemName}
                </td>
                <td style={tdStyle}>{wo.qty}</td>
                <td style={tdStyle}>{wo.deliDate}</td>
                <td style={tdStyle}>{wo.status}</td>
                <td style={tdStyle}>{wo.remark ?? ""}</td>
                <td style={tdStyle}>
                  {wo.status === "PLANNED" && (
                    <button
                      onClick={() => handleChangeStatus("release", wo.woNo)}
                    >
                      RELEASE
                    </button>
                  )}
                  {wo.status === "RELEASED" && (
                    <button
                      onClick={() => handleChangeStatus("start", wo.woNo)}
                    >
                      시작(IN_PROGRESS)
                    </button>
                  )}
                  {wo.status === "IN_PROGRESS" && (
                    <button
                      onClick={() => handleChangeStatus("complete", wo.woNo)}
                    >
                      완료(DONE)
                    </button>
                  )}
                  {wo.status !== "DONE" && wo.status !== "CANCELED" && (
                    <button
                      onClick={() => handleChangeStatus("cancel", wo.woNo)}
                      style={{ marginLeft: 4 }}
                    >
                      취소(CANCEL)
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
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

export default WorkOrderListPage;
