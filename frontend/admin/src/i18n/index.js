import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    admin: {
      title: 'Admin Panel',
      menu: { dashboard: 'Dashboard', products: 'Products', orders: 'Orders', users: 'Users', sellers: 'Sellers', tickets: 'Tickets' },
      menuGroup: { products: 'Product Mgmt', orders: 'Order Mgmt', users: 'User Mgmt', content: 'Content Mgmt', finance: 'Finance', tools: 'Tools' },
      menuSub: { productList: 'Products', inventory: 'Inventory Entry', printLabels: 'Print Labels', importImages: 'Import Images', history: 'History', orderList: 'Orders', buyerMgmt: 'Buyers', sellerMgmt: 'Sellers', ads: 'Ads', news: 'News', financeDashboard: 'Dashboard', salesRevenue: 'Sales Revenue', purchaseReport: 'Purchase Report', profitReport: 'Profit Report', barcodeCodes: 'Barcode Codes', auditLogs: 'Audit Logs' }
    },
    tickets: {
      title: 'Ticket Management',
      subject: 'Subject',
      type: 'Type',
      status: 'Status',
      date: 'Date',
      reply: 'Reply',
      replyPlaceholder: 'Type your reply...',
      sendReply: 'Send Reply'
    },
    dashboard: {
      title: 'Dashboard',
      totalProducts: 'Total Products',
      totalOrders: 'Total Orders',
      totalUsers: 'Total Users',
      pendingSellers: 'Pending Sellers'
    },
    products: {
      title: 'Product Management',
      addProduct: 'Add Product',
      id: 'ID',
      name: 'Name',
      price: 'Price',
      status: 'Status',
      actions: 'Actions',
      activate: 'Activate',
      deactivate: 'Deactivate'
    },
    orders: {
      title: 'Order Management',
      id: 'ID',
      orderNo: 'Order No',
      status: 'Status',
      amount: 'Amount',
      buyer: 'Buyer',
      seller: 'Seller',
      forceComplete: 'Force Complete',
      forceCompleteTip: 'Force complete this order'
    },
    users: {
      title: 'User Management',
      id: 'ID',
      username: 'Username',
      email: 'Email',
      enabled: 'Enabled',
      roles: 'Roles',
      toggleStatus: 'Toggle Status'
    },
    sellers: {
      title: 'Seller Management',
      applications: 'Applications',
      profiles: 'Profiles',
      shopName: 'Shop Name',
      status: 'Status',
      approve: 'Approve',
      reject: 'Reject',
      reason: 'Reject Reason'
    },
    ads: {
      title: 'Ad Management', addAd: 'Add Ad', editAd: 'Edit Ad', title_: 'Title', imageUrl: 'Image URL', linkUrl: 'Link URL', sortOrder: 'Sort Order', isActive: 'Active', startDate: 'Start Date', endDate: 'End Date', deleteConfirm: 'Are you sure you want to delete this ad?'
    },
    news: {
      title: 'News Management', addNews: 'Add News', editNews: 'Edit News', title_: 'Title', summary: 'Summary', content: 'Content', imageUrl: 'Thumbnail URL', isPublished: 'Published', publishedAt: 'Published At', publish: 'Publish', unpublish: 'Unpublish', deleteConfirm: 'Are you sure you want to delete this news?'
    },
    financeReports: {
      salesRevenue: { title: 'Sales Revenue', totalRevenue: 'Total Revenue', orderCount: 'Order Count', avgOrderValue: 'Avg Order Value', month: 'Month', revenue: 'Revenue' },
      purchaseReport: { title: 'Purchase Report', totalCost: 'Total Cost', batchCount: 'Batch Count', avgCostPerBatch: 'Avg Cost / Batch', month: 'Month', cost: 'Cost' },
      profitReport: { title: 'Profit Report', totalRevenue: 'Total Revenue', totalCost: 'Total Cost', netProfit: 'Net Profit', profitMargin: 'Profit Margin', month: 'Month', revenue: 'Revenue', cost: 'Cost', profit: 'Profit', margin: 'Margin (%)' }
    },
    common: { logout: 'Logout', submit: 'Submit', cancel: 'Cancel', search: 'Search', yes: 'Yes', no: 'No', noData: 'No Data', startDate: 'Start Date', endDate: 'End Date' }
  },
  'zh-CN': {
    admin: {
      title: '管理后台',
      menu: { dashboard: '控制台', products: '商品管理', orders: '订单管理', users: '用户管理', sellers: '卖家管理', tickets: '工单管理' },
      menuGroup: { products: '商品管理', orders: '订单管理', users: '用户管理', content: '内容管理', finance: '财务管理', tools: '工具' },
      menuSub: { productList: '商品列表', inventory: '入库', printLabels: '打印标签', importImages: '导入图片', history: '历史查询', orderList: '订单列表', buyerMgmt: '买家管理', sellerMgmt: '卖家管理', ads: '广告管理', news: '新闻管理', financeDashboard: '财务概览', salesRevenue: '销售收入', purchaseReport: '采购入库', profitReport: '利润报表', barcodeCodes: '编码维护', auditLogs: '操作日志' }
    },
    tickets: {
      title: '工单管理',
      subject: '主题',
      type: '类型',
      status: '状态',
      date: '日期',
      reply: '回复',
      replyPlaceholder: '请输入回复内容...',
      sendReply: '发送回复'
    },
    dashboard: {
      title: '控制台',
      totalProducts: '商品总数',
      totalOrders: '订单总数',
      totalUsers: '用户总数',
      pendingSellers: '待审核卖家'
    },
    products: {
      title: '商品管理',
      addProduct: '添加商品',
      id: 'ID',
      name: '名称',
      price: '价格',
      status: '状态',
      actions: '操作',
      activate: '上架',
      deactivate: '下架'
    },
    orders: {
      title: '订单管理',
      id: 'ID',
      orderNo: '订单号',
      status: '状态',
      amount: '金额',
      buyer: '买家',
      seller: '卖家',
      forceComplete: '强制完成',
      forceCompleteTip: '强制完成此订单'
    },
    users: {
      title: '用户管理',
      id: 'ID',
      username: '用户名',
      email: '邮箱',
      enabled: '状态',
      roles: '角色',
      toggleStatus: '切换状态'
    },
    sellers: {
      title: '卖家管理',
      applications: '申请列表',
      profiles: '卖家列表',
      shopName: '店铺名称',
      status: '状态',
      approve: '通过',
      reject: '拒绝',
      reason: '拒绝原因'
    },
    ads: {
      title: '广告管理', addAd: '添加广告', editAd: '编辑广告', title_: '标题', imageUrl: '图片地址', linkUrl: '链接地址', sortOrder: '排序', isActive: '启用', startDate: '开始日期', endDate: '结束日期', deleteConfirm: '确定要删除这个广告吗？'
    },
    news: {
      title: '新闻管理', addNews: '添加新闻', editNews: '编辑新闻', title_: '标题', summary: '摘要', content: '内容', imageUrl: '缩略图地址', isPublished: '已发布', publishedAt: '发布时间', publish: '发布', unpublish: '撤回', deleteConfirm: '确定要删除这个新闻吗？'
    },
    financeReports: {
      salesRevenue: { title: '销售收入', totalRevenue: '总收入', orderCount: '订单数', avgOrderValue: '平均订单金额', month: '月份', revenue: '收入' },
      purchaseReport: { title: '采购入库', totalCost: '总成本', batchCount: '批次数量', avgCostPerBatch: '平均成本/批', month: '月份', cost: '成本' },
      profitReport: { title: '利润报表', totalRevenue: '总收入', totalCost: '总成本', netProfit: '净利润', profitMargin: '利润率', month: '月份', revenue: '收入', cost: '成本', profit: '利润', margin: '利润率(%)' }
    },
    common: { logout: '退出登录', submit: '提交', cancel: '取消', search: '搜索', yes: '是', no: '否', noData: '暂无数据', startDate: '开始日期', endDate: '结束日期' }
  },
  'zh-TW': {
    admin: {
      title: '管理後台',
      menu: { dashboard: '控制台', products: '商品管理', orders: '訂單管理', users: '用戶管理', sellers: '賣家管理', tickets: '工單管理' },
      menuGroup: { products: '商品管理', orders: '訂單管理', users: '用戶管理', content: '內容管理', finance: '財務管理', tools: '工具' },
      menuSub: { productList: '商品列表', inventory: '入庫', printLabels: '打印標籤', importImages: '導入圖片', history: '歷史查詢', orderList: '訂單列表', buyerMgmt: '買家管理', sellerMgmt: '賣家管理', ads: '廣告管理', news: '新聞管理', financeDashboard: '財務概覽', salesRevenue: '銷售收入', purchaseReport: '採購入庫', profitReport: '利潤報表', barcodeCodes: '編碼維護', auditLogs: '操作日誌' }
    },
    tickets: {
      title: '工單管理',
      subject: '主題',
      type: '類型',
      status: '狀態',
      date: '日期',
      reply: '回覆',
      replyPlaceholder: '請輸入回覆內容...',
      sendReply: '發送回覆'
    },
    dashboard: {
      title: '控制台',
      totalProducts: '商品總數',
      totalOrders: '訂單總數',
      totalUsers: '用戶總數',
      pendingSellers: '待審核賣家'
    },
    products: { title: '商品管理', addProduct: '添加商品', id: 'ID', name: '名稱', price: '價格', status: '狀態', actions: '操作', activate: '上架', deactivate: '下架' },
    orders: { title: '訂單管理', id: 'ID', orderNo: '訂單號', status: '狀態', amount: '金額', buyer: '買家', seller: '賣家', forceComplete: '強制完成', forceCompleteTip: '強制完成此訂單' },
    users: { title: '用戶管理', id: 'ID', username: '用戶名', email: '郵箱', enabled: '狀態', roles: '角色', toggleStatus: '切換狀態' },
    sellers: { title: '賣家管理', applications: '申請列表', profiles: '賣家列表', shopName: '店鋪名稱', status: '狀態', approve: '通過', reject: '拒絕', reason: '拒絕原因' },
    ads: {
      title: '廣告管理', addAd: '添加廣告', editAd: '編輯廣告', title_: '標題', imageUrl: '圖片地址', linkUrl: '連結地址', sortOrder: '排序', isActive: '啟用', startDate: '開始日期', endDate: '結束日期', deleteConfirm: '確定要刪除這個廣告嗎？'
    },
    news: {
      title: '新聞管理', addNews: '添加新聞', editNews: '編輯新聞', title_: '標題', summary: '摘要', content: '內容', imageUrl: '縮略圖地址', isPublished: '已發布', publishedAt: '發布時間', publish: '發布', unpublish: '撤回', deleteConfirm: '確定要刪除這個新聞嗎？'
    },
    financeReports: {
      salesRevenue: { title: '銷售收入', totalRevenue: '總收入', orderCount: '訂單數', avgOrderValue: '平均訂單金額', month: '月份', revenue: '收入' },
      purchaseReport: { title: '採購入庫', totalCost: '總成本', batchCount: '批次數量', avgCostPerBatch: '平均成本/批', month: '月份', cost: '成本' },
      profitReport: { title: '利潤報表', totalRevenue: '總收入', totalCost: '總成本', netProfit: '淨利潤', profitMargin: '利潤率', month: '月份', revenue: '收入', cost: '成本', profit: '利潤', margin: '利潤率(%)' }
    },
    common: { logout: '退出登錄', submit: '提交', cancel: '取消', search: '搜索', yes: '是', no: '否', noData: '暫無數據', startDate: '開始日期', endDate: '結束日期' }
  },
  ja: {
    admin: {
      title: '管理パネル',
      menu: { dashboard: 'ダッシュボード', products: '商品管理', orders: '注文管理', users: 'ユーザー管理', sellers: '売り手管理', tickets: 'チケット管理' },
      menuGroup: { products: '商品管理', orders: '注文管理', users: 'ユーザー管理', content: 'コンテンツ管理', finance: '財務管理', tools: 'ツール' },
      menuSub: { productList: '商品一覧', inventory: '入庫', printLabels: 'ラベル印刷', importImages: '画像取込', history: '履歴検索', orderList: '注文一覧', buyerMgmt: '買い手管理', sellerMgmt: '売り手管理', ads: '広告管理', news: 'ニュース管理', financeDashboard: '財務概要', salesRevenue: '売上高', purchaseReport: '仕入報告', profitReport: '利益報告', barcodeCodes: 'コード管理', auditLogs: '操作ログ' }
    },
    tickets: {
      title: 'チケット管理',
      subject: '件名',
      type: '種別',
      status: 'ステータス',
      date: '日時',
      reply: '返信',
      replyPlaceholder: '返信內容を入力...',
      sendReply: '返信を送信'
    },
    dashboard: {
      title: 'ダッシュボード',
      totalProducts: '商品数',
      totalOrders: '注文数',
      totalUsers: 'ユーザー数',
      pendingSellers: '審査待ち'
    },
    products: { title: '商品管理', addProduct: '商品追加', id: 'ID', name: '名称', price: '価格', status: 'ステータス', actions: '操作', activate: '公開', deactivate: '非公開' },
    orders: { title: '注文管理', id: 'ID', orderNo: '注文番号', status: 'ステータス', amount: '金額', buyer: '買い手', seller: '売り手', forceComplete: '強制完了', forceCompleteTip: 'この注文を強制完了する' },
    users: { title: 'ユーザー管理', id: 'ID', username: 'ユーザー名', email: 'メール', enabled: '有効', roles: '役割', toggleStatus: '状態切替' },
    sellers: { title: '売り手管理', applications: '申請一覧', profiles: '売り手一覧', shopName: 'ショップ名', status: 'ステータス', approve: '承認', reject: '却下', reason: '却下理由' },
    ads: {
      title: '広告管理', addAd: '広告追加', editAd: '広告編集', title_: 'タイトル', imageUrl: '画像URL', linkUrl: 'リンクURL', sortOrder: '並び順', isActive: '有効', startDate: '開始日', endDate: '終了日', deleteConfirm: 'この広告を削除してもよろしいですか？'
    },
    news: {
      title: 'ニュース管理', addNews: 'ニュース追加', editNews: 'ニュース編集', title_: 'タイトル', summary: '概要', content: '本文', imageUrl: 'サムネイルURL', isPublished: '公開済み', publishedAt: '公開日時', publish: '公開', unpublish: '非公開', deleteConfirm: 'このニュースを削除してもよろしいですか？'
    },
    financeReports: {
      salesRevenue: { title: '売上高', totalRevenue: '総売上', orderCount: '注文数', avgOrderValue: '平均注文額', month: '月', revenue: '売上' },
      purchaseReport: { title: '仕入報告', totalCost: '総仕入額', batchCount: 'バッチ数', avgCostPerBatch: '平均仕入/バッチ', month: '月', cost: '仕入額' },
      profitReport: { title: '利益報告', totalRevenue: '総売上', totalCost: '総仕入', netProfit: '純利益', profitMargin: '利益率', month: '月', revenue: '売上', cost: '仕入', profit: '利益', margin: '利益率(%)' }
    },
    common: { logout: 'ログアウト', submit: '送信', cancel: 'キャンセル', search: '検索', yes: 'はい', no: 'いいえ', noData: 'データなし', startDate: '開始日', endDate: '終了日' }
  },
  ko: {
    admin: {
      title: '관리 패널',
      menu: { dashboard: '대시보드', products: '상품 관리', orders: '주문 관리', users: '사용자 관리', sellers: '판매자 관리', tickets: '티켓 관리' },
      menuGroup: { products: '상품 관리', orders: '주문 관리', users: '사용자 관리', content: '콘텐츠 관리', finance: '재무 관리', tools: '도구' },
      menuSub: { productList: '상품 목록', inventory: '입고', printLabels: '라벨 인쇄', importImages: '이미지 가져오기', history: '이력 조회', orderList: '주문 목록', buyerMgmt: '구매자 관리', sellerMgmt: '판매자 관리', ads: '광고 관리', news: '뉴스 관리', financeDashboard: '재무 개요', salesRevenue: '매출 수익', purchaseReport: '구매 입고', profitReport: '이익 보고서', barcodeCodes: '바코드 관리', auditLogs: '작업 로그' }
    },
    tickets: {
      title: '티켓 관리',
      subject: '제목',
      type: '유형',
      status: '상태',
      date: '날짜',
      reply: '답변',
      replyPlaceholder: '답변을 입력하세요...',
      sendReply: '답변 보내기'
    },
    dashboard: {
      title: '대시보드',
      totalProducts: '총 상품',
      totalOrders: '총 주문',
      totalUsers: '총 사용자',
      pendingSellers: '승인 대기 판매자'
    },
    products: { title: '상품 관리', addProduct: '상품 추가', id: 'ID', name: '이름', price: '가격', status: '상태', actions: '작업', activate: '활성화', deactivate: '비활성화' },
    orders: { title: '주문 관리', id: 'ID', orderNo: '주문 번호', status: '상태', amount: '금액', buyer: '구매자', seller: '판매자', forceComplete: '강제 완료', forceCompleteTip: '이 주문을 강제 완료합니다' },
    users: { title: '사용자 관리', id: 'ID', username: '사용자명', email: '이메일', enabled: '활성', roles: '역할', toggleStatus: '상태 전환' },
    sellers: { title: '판매자 관리', applications: '신청 목록', profiles: '판매자 목록', shopName: '상점명', status: '상태', approve: '승인', reject: '거절', reason: '거절 사유' },
    ads: {
      title: '광고 관리', addAd: '광고 추가', editAd: '광고 수정', title_: '제목', imageUrl: '이미지 URL', linkUrl: '링크 URL', sortOrder: '정렬 순서', isActive: '활성', startDate: '시작 날짜', endDate: '종료 날짜', deleteConfirm: '이 광고를 삭제하시겠습니까?'
    },
    news: {
      title: '뉴스 관리', addNews: '뉴스 추가', editNews: '뉴스 수정', title_: '제목', summary: '요약', content: '내용', imageUrl: '썸네일 URL', isPublished: '게시됨', publishedAt: '게시 시간', publish: '게시', unpublish: '게시 취소', deleteConfirm: '이 뉴스를 삭제하시겠습니까?'
    },
    financeReports: {
      salesRevenue: { title: '매출 수익', totalRevenue: '총 매출', orderCount: '주문 수', avgOrderValue: '평균 주문 금액', month: '월', revenue: '매출' },
      purchaseReport: { title: '구매 입고', totalCost: '총 비용', batchCount: '배치 수', avgCostPerBatch: '평균 비용/배치', month: '월', cost: '비용' },
      profitReport: { title: '이익 보고서', totalRevenue: '총 매출', totalCost: '총 비용', netProfit: '순이익', profitMargin: '이익률', month: '월', revenue: '매출', cost: '비용', profit: '이익', margin: '이익률(%)' }
    },
    common: { logout: '로그아웃', submit: '제출', cancel: '취소', search: '검색', yes: '예', no: '아니오', noData: '데이터 없음', startDate: '시작 날짜', endDate: '종료 날짜' }
  }
}

const i18n = createI18n({
  locale: localStorage.getItem('language') || 'zh-CN',
  fallbackLocale: 'en',
  messages,
  legacy: false,
  globalInjection: true
})

export default i18n
