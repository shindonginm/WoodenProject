// 좌측 카테고리칸 카테고리 배열 모음
export const OrderList = [
    { id: 1, name: "판매거래처",path:'order/sellercustomer'},
    { id: 2, name: "상품주문서",path:'order/orderlist'},
    { id: 3, name: "주문완료현황",path:"order/orderreceive"}
  ];

  export const BuyerList = [
    {id: 1, name:"구매거래처",path:'buyer/buyercustomer'},
    {id: 2, name: "부품", path:'buyer/partlist'},
    {id: 3, name:"부품발주",path:"buyer/partorder"},
  ]
  export const PlanList = [
    {id:1, name:"상품",path:"plan/itemlist"},
    {id:2, name:"생산",path:"plan/planlist"},
    {id:3, name:"BOM", path:"plan/bomlist"},
    { id: 4, name: "APS계획", path: "plan/aps" },
    { id: 5, name: "작업지시", path: "plan/workorder" }
  ]
  export const StockList = [
    {id :1, name:"생산완료재고",path:"stock/stocklist"},
    { id: 2, name: "입고완료재고",   path: "stock/partstock" },
    // {id :3, name:"실제판매수량",path:"stock/sellamount"},
  ]








  //----------------화면 상단 네비게이션(id,이름,경로) 배열-------------------------
  export const NavBar = [
    { 
      id: 1, name:"ORDER", path:'/order'
    },
    {
      id: 2, name:"BUYER",path:'/buyer'
    },
    {
      id: 3, name:"PLAN",path:'/plan'
    },
    {
      id: 4, name:"STOCK",path:'/stock'
    }
  ];
// 테스트 로그인 관리자 ID / PW 정보
  export const TestAdmin = {
    "ID":"admin",
    "Password":"1234"
  }