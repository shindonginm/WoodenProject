import axios from "axios";
import { useEffect, useState, useMemo } from "react";
import { getOrderList, createOrderList, updateOrderList, deleteOrderList } from "../../api/OrderListAPI";
import { useCRUD } from "../../hook/useCRUD";
import { initForms } from "../../arrays/TableArrays";
import { useNavigate } from "react-router-dom";
import { OrderListArr } from "../../arrays/OrderListArrays";
import { BASE_URL } from "../../api/config";
import { getCustomer } from "../../api/CustomerAPI";
import { getItemList } from "../../api/ItemListAPI";
import { updateOrderStatus } from "../../api/OrderListAPI";
import ModalComponent from "../../components/ModalComponent";
import OrderListForm from "../../form/order/OrderListForm";
import ButtonComponent from "../../components/ButtonComponent";
import BackButtonComponent from "../../components/BackButtonComponent";
import InlineSelectCell from "../../components/InlineSelectCell";
import SearchComponent from "../../components/SearchComponent";

const OrderListPage = () => {
  const navigate = useNavigate();

  // 완료 여부 유틸
  const isCompleted = o =>
    o?.orderState === "승인완료" && o?.orderDeliState === "납품완료";
  
  const normalizeOrder = (o) => {
    const qty = Number(o.orderQty ?? 0);
    const price = Number(o.orderPrice ?? 0);
    const serverTotal = Number(o.totalPrice ?? 0);
    return {
      ...o,
      orderQty: qty,
      orderPrice: price,
      totalPrice: serverTotal > 0 ? serverTotal : qty * price,
    };
  };

// api.update는 DTO 스펙에 맞춰 body 정리해서 전송
const api = {
  getAll: async () => {
    const data = await getOrderList();
    return data.map(normalizeOrder).filter(o => !isCompleted(o));
  },
  create: createOrderList,
  update: (fd) => {
    const payload = {
      customerId: Number(fd.customerId),
      itemId: Number(fd.itemId),
      orderQty: Number(fd.orderQty),
      orderPrice: Number(fd.orderPrice),
      orderState: fd.orderState,
      orderDeliState: fd.orderDeliState,
      orderDate: fd.orderDate,        // YYYY-MM-DD
      deliveryDate: fd.deliveryDate,  // YYYY-MM-DD
      cusAddr: fd.cusAddr,
    };
    return updateOrderList(fd.orderNo, payload);
  },
  delete: (orderNo) => deleteOrderList(orderNo),
};

const {
  items: orderList,
  setItems: setOrderList,
  formData, setFormData,

  handleChange, handleCreate, handleUpdate, handleDelete,
  openEdit, openCreate, closeCreate, closeEdit,
  isCreateOpen, isEditOpen, selectedItem,

  // 드롭다운 데이터
  customer, itemList, setCustomer, setItemList,
  setCustomerId, setItemListId,
  } = useCRUD({
    initFormData: () => initForms.orderList,
    api,
    keyField: "orderNo",
  });

// 검색 상태
const [q, setQ] = useState("");
const [term, setTerm] = useState("");

const computeCompletion = (prevRow, patch) => {
    const nextOrderState = patch.orderState ?? prevRow.orderState;
    const nextDeliState = patch.orderDeliState ?? prevRow.orderDeliState;
    return nextOrderState === "승인완료" && nextDeliState === "납품완료";
};

// 업데이트 후 실패 시 롤백 유틸
const replaceRow = (list, id, patch) => 
  list.map(r => (r.orderNo === id ? { ...r, ...patch } : r));

// 드롭다운 소스 로드
useEffect(() => {
  (async () => {
    const [orders, customers, items] = await Promise.all([
      getOrderList(),
      getCustomer(),
      getItemList(),
    ]);

    const normalized = orders.map(o => {
      const qty = Number(o.orderQty ?? 0);
      const price = Number(o.orderPrice ?? 0);
      const serverTotal = Number(o.totalPrice ?? 0);
      return {
        ...o,
        orderQty: qty,
        orderPrice: price,
        totalPrice: serverTotal > 0 ? serverTotal : qty * price, // 총 금액 표시 핵심
      };
    });

    setOrderList(normalized.filter(o => !isCompleted(o)));  // 완료 리스트는 제외
    setCustomer(customers.map(c => ({ value: c.cusNo, label: c.cusComp })));
    setItemList(items.map(i => ({ value: i.itemNo, label: i.itemName, price: i.itemPrice })));
    })();
}, [setOrderList, setCustomer, setItemList]);

useEffect(() => {
  // 고객 목록 전체 조회
  axios.get(`${BASE_URL}/order/sellercustomer`).then((res) => {
    const options = res.data.map((temp) => ({
      label: temp.cusComp, // 판매거래처명
      value: temp.cusNo, // PK
    }));
    setCustomer(options);
  });

  // 상품 목록 전체 조회
  axios.get(`${BASE_URL}/plan/itemlist`).then((res) => {
    const options = res.data.map((temp) => ({
      label: temp.itemName, // 상품명
      value: temp.itemNo, // PK
      price: temp.itemPrice // 단가
    }));
    setItemList(options);
  });
}, [setCustomer, setItemList]);

  // 이름(label) 과 ID(value) 매핑 도우미
  const labelToId = (list, label) => {
    const f = list.find(x => x.label === label);
    return f ? f.value : "";
  };

  // 컬럼명으로 클릭 가능하게 함
  const handleRowClick = (row) => {
    openEdit(row); // 기존 데이터 세팅
    // 이름만 있던 필드를 ID 로 채워줌
    const cid = labelToId(customer, row.cusComp);
    const iid = labelToId(itemList, row.itemName);

    setFormData(prev => ({
      ...prev,
      customerId: cid,
      itemId: iid,
      // 숫자형 필드는 안전하게 숫자로
      orderQty: Number(row.orderQty || 0),
      orderPrice: Number(row.orderPrice || 0),
      // 총 금액 표시
      totalPrice: Number(row.orderQty || 0) * Number(row.orderPrice || 0),
    }));
  };

  // 오더리스트 페이지 리로드
  const reloadOrders = async () => {
  const orders = await getOrderList();
  const normalized = orders.map(normalizeOrder);
  setOrderList(normalized.filter(o => !isCompleted(o)));
  };

  // CRUD 후 새로고침
  const handleCreateAndRefresh = async () => {
    const ok = await handleCreate();
    if (ok) {
      await reloadOrders();
      closeCreate();
      alert("등록 완료");
    } else {
      alert("등록 실패");
    }
  };
  const handleUpdateAndRefresh = async () => {
    const ok = await handleUpdate(); // useCRUD가 true/false 반환
    if (ok) {
      await reloadOrders();
      closeEdit();
      alert("수정 완료");
    } else {
      alert("수정 실패");
    }
  };

  // state 값 onPatch 호출

// 주문 상태 변경
const onPatchOrderState = async (id, next) => {
  const prevRow = orderList.find(r => r.orderNo === id);
  const prev = prevRow?.orderState;

  // 낙관적 반영
  setOrderList(list => replaceRow(list, id, { orderState: next }));

  try {
    const updated = await updateOrderStatus(id, { orderState: next });
    const patch = { orderState: updated?.orderState ?? next };

    // 완료 판단
    if (computeCompletion(prevRow, patch)) {
      // 완료면 목록에서 제거하고 이동
      setOrderList(list => list.filter(r => r.orderNo !== id));
    } else {
      // 미완료면 서버값으로만 확정. 여기서 refetch 금지!
      setOrderList(list => replaceRow(list, id, patch));
    }
  } catch (err) {
    alert(err?.response?.data?.error || "상태 변경 실패");
    setOrderList(list => replaceRow(list, id, { orderState: prev }));
  }
};

  // 납품상태 변경 및 이동
  const onPatchDeliState = async (id, next) => {
    const prevRow = orderList.find(r => r.orderNo === id);
    const prev = prevRow?.orderDeliState;

    // UI 업데이트
    setOrderList(list => replaceRow(list, id, { orderDeliState: next }));

      try {
        // 1 ) 납품완료 때만 선제 재고 체크 (서버 409 를 줄여 UX를 보호)
        if (next === "납품완료") {
          // 화면의 itemList 에서 label -> id 로 변환
          const itemId = labelToId(itemList, prevRow.itemName);
          if (itemId) {
            const {data : stock} = await axios.get(`${BASE_URL}/stock/item/${itemId}`);
            const cur = Number(stock?.isQty ?? 0);
            const need = Number(prevRow?.orderQty ?? 0);
            if (cur < need) {
              // 서버와 같은 톤의 메시지로 즉시 차단
              throw new Error(`재고 부족 : 현재 상품${stock.itemName}이 ${cur} 개. 필요 ${need} 개 상품 재고가 필요합니다`);
            }
          }
        }
        // 서버 PATCH
        const updated = await updateOrderStatus(id, { orderDeliState: next });

        // 3 ) 서버가 일부만 돌려줘도 안전하게 패치만 반영
        const patch = { orderDeliState: updated?.orderDeliState ?? next };

        if (computeCompletion(prevRow, patch)) {
          // 완료 : 리스트에서 제거하고 이동
          setOrderList(list => list.filter(r => r.orderNo !== id));
          await reloadOrders();
          navigate("/order/orderreceive", {replace: true});
        } else {
          // 미완료 : 서버 값으로 확정 & 리로드
          setOrderList(list => replaceRow(list, id, patch));
          await reloadOrders();
        }
      } catch (err) {
        // 409 / 400 본문 포멧 : { code, message }
        const msg =
          err?.response?.data?.error || 
          err?.message ||
          "상태 변경 실패";
        alert(msg); // ex: "재고가 부족합니다"
        // 실패 롤백
        setOrderList(list => replaceRow(list, id, { orderDeliState: prev }));
      }
    };

    // 판매처명 기준 검색
    const getSellerName = (row) =>
      row?.cusComp ?? row?.customerName ?? row?.company ?? "";
    
    const filtered = useMemo(() => {
      const t = term.trim().toLowerCase();
      const base = Array.isArray(orderList) ? orderList : [];
      if (!t) return base;
      return base.filter(r => getSellerName(r).toLowerCase().includes(t));
    }, [orderList, term]);

  return (
    <div className="page-wrapper">
      <BackButtonComponent text="< &nbsp;이전페이지" onClick={() => navigate(-1)} />
      <h2 style={{ textAlign: "center" }}>상품주문서</h2>

      {/* 판매처명 검색 + 새로고침 */}
      <div className="top-searchbar">
        <SearchComponent
          value={q}
          onChange={setQ}
          onDebounced={setTerm}
          delay={300}
          minLength={0}
          placeholder="판매처명"
          className="border rounded px-3 py-2"
        />
        <ButtonComponent onClick={reloadOrders} text="새로고침" cln="refresh" />
      </div>

      {/* 테이블 */}
      <div className="table-wrapper">
          <table>
        <thead>
          <tr>
            {OrderListArr.map(col => (
              <th key={col.id}>{col.content}</th>
            ))}
          </tr>
        </thead>

        <tbody>
          {filtered?.length ? (
            filtered.map(row => (
              <tr key={row.orderNo} className="row">
                {OrderListArr.map(col => {
                  // 총금액 계산 표기
                  if (col.clmn === "totalPrice") {
                    const qty   = Number(row.orderQty ?? 0);
                    const price = Number(row.orderPrice ?? 0);
                    const total = Number(row.totalPrice ?? 0) || qty * price;
                    return <td key={col.id}>{total.toLocaleString()}</td>;
                  }
                  // 2) 주문상태 드롭다운: 버블링 차단
                  if (col.clmn === "orderState") {
                    return (
                      <td key={col.id} onClick={(e) => e.stopPropagation()}>
                        <InlineSelectCell
                          rowKey={row.orderNo}
                          value={row.orderState}
                          options={["승인대기", "승인완료"]}
                          onPatch={onPatchOrderState}
                        />
                      </td>
                    );
                  }
                  // 2) 납품상태 드롭다운: 버블링 차단
                  if (col.clmn === "orderDeliState") {
                    return (
                      <td key={col.id} onClick={(e) => e.stopPropagation()}>
                        <InlineSelectCell
                          rowKey={row.orderNo}
                          value={row.orderDeliState}
                          options={["납품대기", "납품완료"]}
                          onPatch={onPatchDeliState}
                        />
                      </td>
                    );
                  }
                  // 3) 판매처명만 클릭으로 수정폼 오픈
                  if (col.clmn === "cusComp") {
                    return (
                      <td
                        key={col.id}
                        style={{ color: "blue", textDecoration: "underline", cursor: "pointer" }}
                        onClick={() => handleRowClick(row)}
                      >
                        {row[col.clmn]}
                      </td>
                    );
                  }

                  // 일반 셀
                  return <td key={col.id}>{row[col.clmn]}</td>;
                })}
              </tr>
            ))
          ) : (
              <tr>
                <td colSpan={OrderListArr.length} style={{ textAlign: "center" }}>
                  데이터가 없습니다.
                </td>
              </tr>
          )}
        </tbody>



      </table>
      </div>
      

      {/* 등록 버튼 */}
      <br />
      <ButtonComponent onClick={openCreate} text={"주문 등록"} cln="submit" />

      {/* 등록 모달 */}
      <ModalComponent
        isOpen={isCreateOpen}
        onClose={closeCreate}
        title="상품주문서 등록"
        onConfirm={handleCreate}
      >
        <OrderListForm
          formData={formData}
          onChange={handleChange}
          customer={customer}
          itemList={itemList}
          setCustomerId={(v) => setFormData(p => ({ ...p, customerId: v }))}
          setItemListId={(v) => setFormData(p => ({ ...p, itemId: v }))} >
          <ButtonComponent text={"등록"} onClick={handleCreateAndRefresh} cln="submit" />
        </OrderListForm>
      </ModalComponent>

      {/* 수정/삭제 모달 */}
      <ModalComponent
        isOpen={isEditOpen}
        onClose={closeEdit}
        title="상품주문서 수정/삭제"
        onConfirm={handleUpdate}
      >
        {selectedItem && (
          <OrderListForm
            formData={formData}
            onChange={handleChange}
            customer={customer}
            itemList={itemList}
            setCustomerId={setCustomerId}
            setItemListId={setItemListId}
          >
            <div className="btn-wrapper">
              <ButtonComponent text="수정" onClick={handleUpdateAndRefresh} cln="fixbtn" />
              <ButtonComponent text="삭제" onClick={handleDelete} cln="delbtn" />
            </div>
          </OrderListForm>
        )}
      </ModalComponent>
    </div>
  );
};

export default OrderListPage;
