import { lazy, Suspense } from "react";
import { Navigate } from "react-router-dom";


const Loading = <div>Loading...</div>

const ItemList = lazy(() => import("../pages/plan/ItemListPage"));
const PlanList = lazy(() => import("../pages/plan/PlanListPage"));
const BomList = lazy(() => import("../pages/plan/BomListPage"));
const ApsPlanPage = lazy(() => import("../pages/plan/ApsPlanPage.jsx"));
const WorkOrderListPage = lazy(() => import("../pages/plan/WorkOrderListPage.jsx"));

const PlanRouter = () => ([
  {
    index: true,
    element: <Navigate to="itemlist" replace />
  },
  {
    path: "itemlist",
    element: <Suspense fallback={Loading}><ItemList/></Suspense>
  },
  {
    path: "planlist",
    element: <Suspense fallback={Loading}><PlanList/></Suspense>
  },
  {
    path: "bomlist",
    element: <Suspense fallback={Loading}><BomList/></Suspense>
  },
  {
    path: "aps",
    element: <Suspense fallback={Loading}><ApsPlanPage/></Suspense>
  },
  {
    path: "workorder",
    element: <Suspense fallback={Loading}><WorkOrderListPage/></Suspense>
  }
]);
  

export default PlanRouter;