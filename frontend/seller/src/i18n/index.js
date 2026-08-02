import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    errors: {
      'Invalid credentials': 'Invalid username or password',
      'Account is disabled': 'Account is disabled',
      'Username already exists': 'Username already exists',
      'Email already exists': 'Email already exists',
      'User not found': 'User not found'
    },
    seller: {
      title: 'Seller Center',
      menu: { dashboard: 'Dashboard', products: 'Products', orders: 'Orders', profile: 'Profile' },
      application: { title: 'Apply to Become a Seller', shopName: 'Shop Name', shopDescription: 'Shop Description', idDocument: 'ID Document' },
      validation: { shopNameRequired: 'Shop name is required', idRequired: 'ID document is required' }
    },
    nav: {
      dashboard: 'Dashboard',
      products: 'Products',
      orders: 'Orders',
      profile: 'Profile',
      quickInventory: 'Quick Inventory',
      salesReport: 'Sales Report',
      printLabels: 'Print Labels',
      logout: 'Logout'
    },
    dashboard: {
      title: 'Dashboard',
      weeklySales: 'Weekly Sales (7 days)',
      recentOrders: 'Recent Orders',
      salesSummary: 'Monthly Sales Summary',
      recentCompleted: 'Recent Completed Orders',
      revenueOverview: 'Revenue Overview',
      completedOrders: 'Completed Orders',
      totalRevenue: 'Total Revenue',
      avgOrderValue: 'Avg Order Value',
      viewReport: 'View Report'
    },
    orders: {
      title: 'Order Management',
      orderNo: 'Order No',
      amount: 'Amount',
      status: 'Status',
      buyer: 'Buyer',
      detail: 'Detail',
      ship: 'Ship',
      cancel: 'Cancel',
      confirmShip: 'Confirm Shipment',
      noOrders: 'No orders yet',
      paymentMethod: 'Payment Method',
      shippingMethod: 'Shipping Method',
      address: 'Address',
      paidAt: 'Paid At',
      tracking: 'Tracking',
      statuses: {
        PENDING_PAYMENT: 'Pending Payment',
        PAID: 'Paid',
        SHIPPED: 'Shipped',
        COMPLETED: 'Completed',
        CANCELLED: 'Cancelled'
      }
    },
    profile: {
      title: 'Shop Settings',
      shopInfo: 'Shop Information',
      shopName: 'Shop Name',
      shopDescription: 'Shop Description',
      lockedHint: 'Profile locked. Please contact admin to modify.',
      saveChanges: 'Save Changes'
    },
    salesReport: {
      title: 'Sales Report',
      totalRevenue: 'Monthly Revenue',
      completedOrders: 'Completed Orders',
      pendingOrders: 'Pending Orders',
      avgOrderValue: 'Avg Order Value',
      statusBreakdown: 'Order Status Breakdown',
      monthlyOrders: 'Completed Orders This Month',
      total: 'Total'
    },
    products: {
      title: 'Product Management',
      publish: 'Publish Product',
      batchActivate: 'Batch Activate',
      batchDeactivate: 'Batch Deactivate',
      quickInventory: 'Quick Inventory',
      printLabels: 'Print Labels',
      selected: 'Selected',
      totalInvestment: 'Total Investment',
      totalSalePrice: 'Total Sale Price',
      expectedProfit: 'Expected Profit',
      edit: 'Edit',
      image: 'Image',
      copy: 'Copy',
      deactivate: 'Deactivate',
      cancel: 'Cancel',
      update: 'Update',
      publishBtn: 'Publish',
      uploadImageTitle: 'Upload Images',
      barcode: 'Barcode',
      uploadHint: 'Drag or click to upload images',
      close: 'Close',
      noData: 'No products yet'
    },
    printLabels: {
      title: 'Print Labels',
      refresh: 'Refresh',
      selectAll: 'Select All',
      showUnprinted: 'Show unprinted only',
      noProducts: 'No products found',
      stock: 'Stock',
      printed: 'Printed',
      unprinted: 'Unprinted'
    },
    quickInventory: {
      title: 'Quick Inventory',
      scanBarcode: 'Scan Barcode',
      scanHint: 'Use a barcode scanner or type the barcode manually',
      search: 'Search',
      productInfo: 'Product Info',
      barcode: 'Barcode',
      confirmEntry: 'Confirm Inventory Entry',
      notFound: 'Product Not Found — Quick Create',
      createProduct: 'Create Product',
      createAndEntry: 'Create & Add to Inventory',
      entryRecord: 'Inventory Records'
    },
    common: { logout: 'Logout', submit: 'Submit', upload: 'Upload', search: 'Search', save: 'Save', cancel: 'Cancel', close: 'Close', confirm: 'Confirm', loading: 'Loading...', all: 'All', detail: 'Detail', refresh: 'Refresh', actions: 'Actions', status: 'Status', back: 'Back' }
  },
  'zh-CN': {
    errors: {
      'Invalid credentials': '用户名或密码错误',
      'Account is disabled': '账号已被禁用',
      'Username already exists': '用户名已存在',
      'Email already exists': '邮箱已被注册',
      'User not found': '用户不存在'
    },
    seller: {
      title: '卖家中心',
      menu: { dashboard: '控制台', products: '商品管理', orders: '订单管理', profile: '资料设置' },
      application: { title: '申请成为卖家', shopName: '店铺名称', shopDescription: '店铺描述', idDocument: '身份证明' },
      validation: { shopNameRequired: '请输入店铺名称', idRequired: '请上传身份证明' }
    },
    nav: {
      dashboard: '控制台',
      products: '商品管理',
      orders: '订单管理',
      profile: '资料设置',
      quickInventory: '快捷入库',
      salesReport: '销售报表',
      printLabels: '打印条码',
      logout: '退出登录'
    },
    dashboard: {
      title: '控制台',
      weeklySales: '近7日销售额',
      recentOrders: '近期订单',
      salesSummary: '本月销售概况',
      recentCompleted: '最近完成订单',
      revenueOverview: '收入概览',
      completedOrders: '已完成订单',
      totalRevenue: '总收入',
      avgOrderValue: '平均订单金额',
      viewReport: '查看报表'
    },
    orders: {
      title: '订单管理',
      orderNo: '订单号',
      amount: '金额',
      status: '状态',
      buyer: '买家',
      detail: '详情',
      ship: '发货',
      cancel: '取消',
      confirmShip: '确认发货',
      noOrders: '暂无订单',
      paymentMethod: '支付方式',
      shippingMethod: '配送方式',
      address: '地址',
      paidAt: '支付时间',
      tracking: '快递',
      statuses: {
        PENDING_PAYMENT: '待付款',
        PAID: '已支付',
        SHIPPED: '已发货',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      }
    },
    profile: {
      title: '店铺设置',
      shopInfo: '店铺信息',
      shopName: '店铺名称',
      shopDescription: '店铺描述',
      lockedHint: '资料已锁定，如需修改请联系管理员',
      saveChanges: '保存修改'
    },
    salesReport: {
      title: '销售报表',
      totalRevenue: '本月总收入',
      completedOrders: '已完成订单',
      pendingOrders: '待处理订单',
      avgOrderValue: '平均订单金额',
      statusBreakdown: '订单状态分布',
      monthlyOrders: '本月已完成订单',
      total: '总计'
    },
    products: {
      title: '商品管理',
      publish: '发布商品',
      batchActivate: '批量上架',
      batchDeactivate: '批量下架',
      quickInventory: '快捷入库',
      printLabels: '打印条码',
      selected: '已选',
      totalInvestment: '总投资',
      totalSalePrice: '总售价',
      expectedProfit: '预期利润',
      edit: '编辑',
      image: '图片',
      copy: '复制',
      deactivate: '下架',
      cancel: '取消',
      update: '更新',
      publishBtn: '发布',
      uploadImageTitle: '上传图片',
      barcode: '条码',
      uploadHint: '拖拽或点击上传图片',
      close: '关闭',
      noData: '暂无商品'
    },
    printLabels: {
      title: '打印条码',
      refresh: '刷新',
      selectAll: '全选',
      showUnprinted: '仅显示未打印商品',
      noProducts: '暂无符合条件的商品',
      stock: '库存',
      printed: '已打印',
      unprinted: '未打印'
    },
    quickInventory: {
      title: '快捷入库',
      scanBarcode: '扫描条码',
      scanHint: '使用条码扫描枪对准条码，或手动输入条码号',
      search: '查询',
      productInfo: '商品信息',
      barcode: '条码',
      confirmEntry: '确认入库',
      notFound: '未找到该商品 — 快速创建',
      createProduct: '创建商品',
      createAndEntry: '创建并入库',
      entryRecord: '本次入库记录'
    },
    common: { logout: '退出登录', submit: '提交', upload: '上传', search: '搜索', save: '保存', cancel: '取消', close: '关闭', confirm: '确认', loading: '加载中...', all: '全部', detail: '详情', refresh: '刷新', actions: '操作', status: '状态', back: '返回' }
  },
  'zh-TW': {
    errors: {
      'Invalid credentials': '用戶名或密碼錯誤',
      'Account is disabled': '帳號已被禁用',
      'Username already exists': '用戶名已存在',
      'Email already exists': '郵箱已被註冊',
      'User not found': '用戶不存在'
    },
    seller: {
      title: '賣家中心',
      menu: { dashboard: '控制台', products: '商品管理', orders: '訂單管理', profile: '資料設置' },
      application: { title: '申請成為賣家', shopName: '店鋪名稱', shopDescription: '店鋪描述', idDocument: '身份證明' },
      validation: { shopNameRequired: '請輸入店鋪名稱', idRequired: '請上傳身份證明' }
    },
    nav: {
      dashboard: '控制台',
      products: '商品管理',
      orders: '訂單管理',
      profile: '資料設置',
      quickInventory: '快捷入庫',
      salesReport: '銷售報表',
      printLabels: '打印條碼',
      logout: '退出登錄'
    },
    dashboard: {
      title: '控制台',
      weeklySales: '近7日銷售額',
      recentOrders: '近期訂單',
      salesSummary: '本月銷售概況',
      recentCompleted: '最近完成訂單',
      revenueOverview: '收入概覽',
      completedOrders: '已完成訂單',
      totalRevenue: '總收入',
      avgOrderValue: '平均訂單金額',
      viewReport: '查看報表'
    },
    orders: {
      title: '訂單管理',
      orderNo: '訂單號',
      amount: '金額',
      status: '狀態',
      buyer: '買家',
      detail: '詳情',
      ship: '發貨',
      cancel: '取消',
      confirmShip: '確認發貨',
      noOrders: '暫無訂單',
      paymentMethod: '支付方式',
      shippingMethod: '配送方式',
      address: '地址',
      paidAt: '支付時間',
      tracking: '快遞',
      statuses: {
        PENDING_PAYMENT: '待付款',
        PAID: '已支付',
        SHIPPED: '已發貨',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      }
    },
    profile: {
      title: '店鋪設置',
      shopInfo: '店鋪信息',
      shopName: '店鋪名稱',
      shopDescription: '店鋪描述',
      lockedHint: '資料已鎖定，如需修改請聯繫管理員',
      saveChanges: '保存修改'
    },
    salesReport: {
      title: '銷售報表',
      totalRevenue: '本月總收入',
      completedOrders: '已完成訂單',
      pendingOrders: '待處理訂單',
      avgOrderValue: '平均訂單金額',
      statusBreakdown: '訂單狀態分佈',
      monthlyOrders: '本月已完成訂單',
      total: '總計'
    },
    products: {
      title: '商品管理',
      publish: '發佈商品',
      batchActivate: '批量上架',
      batchDeactivate: '批量下架',
      quickInventory: '快捷入庫',
      printLabels: '打印條碼',
      selected: '已選',
      totalInvestment: '總投資',
      totalSalePrice: '總售價',
      expectedProfit: '預期利潤',
      edit: '編輯',
      image: '圖片',
      copy: '複製',
      deactivate: '下架',
      cancel: '取消',
      update: '更新',
      publishBtn: '發佈',
      uploadImageTitle: '上傳圖片',
      barcode: '條碼',
      uploadHint: '拖拽或點擊上傳圖片',
      close: '關閉',
      noData: '暫無商品'
    },
    printLabels: {
      title: '打印條碼',
      refresh: '刷新',
      selectAll: '全選',
      showUnprinted: '僅顯示未打印商品',
      noProducts: '暫無符合條件的商品',
      stock: '庫存',
      printed: '已打印',
      unprinted: '未打印'
    },
    quickInventory: {
      title: '快捷入庫',
      scanBarcode: '掃描條碼',
      scanHint: '使用條碼掃描槍對準條碼，或手動輸入條碼號',
      search: '查詢',
      productInfo: '商品信息',
      barcode: '條碼',
      confirmEntry: '確認入庫',
      notFound: '未找到該商品 — 快速創建',
      createProduct: '創建商品',
      createAndEntry: '創建并入庫',
      entryRecord: '本次入庫記錄'
    },
    common: { logout: '退出登錄', submit: '提交', upload: '上傳', search: '搜索', save: '保存', cancel: '取消', close: '關閉', confirm: '確認', loading: '加載中...', all: '全部', detail: '詳情', refresh: '刷新', actions: '操作', status: '狀態', back: '返回' }
  },
  ja: {
    errors: {
      'Invalid credentials': 'ユーザー名またはパスワードが正しくありません',
      'Account is disabled': 'アカウントが無効です',
      'Username already exists': 'ユーザー名は既に存在します',
      'Email already exists': 'メールアドレスは既に登録されています',
      'User not found': 'ユーザーが見つかりません'
    },
    seller: {
      title: '売りセンタ',
      menu: { dashboard: 'ダッシュボード', products: '商品管理', orders: '注文管理', profile: 'プロフィール' },
      application: { title: '売り手になる', shopName: 'ショップ名', shopDescription: 'ショップ説明', idDocument: '身分証明書' },
      validation: { shopNameRequired: 'ショップ名は必須です', idRequired: '身分証明書が必要です' }
    },
    nav: {
      dashboard: 'ダッシュボード',
      products: '商品管理',
      orders: '注文管理',
      profile: 'プロフィール',
      quickInventory: 'クイック入庫',
      salesReport: '販売レポート',
      printLabels: 'ラベル印刷',
      logout: 'ログアウト'
    },
    dashboard: {
      title: 'ダッシュボード',
      weeklySales: '週間売上（7日間）',
      recentOrders: '最近の注文',
      salesSummary: '月間販売概要',
      recentCompleted: '最近完了した注文',
      revenueOverview: '収入概要',
      completedOrders: '完了した注文',
      totalRevenue: '総収入',
      avgOrderValue: '平均注文金額',
      viewReport: 'レポートを見る'
    },
    orders: {
      title: '注文管理',
      orderNo: '注文番号',
      amount: '金額',
      status: 'ステータス',
      buyer: '買い手',
      detail: '詳細',
      ship: '発送',
      cancel: 'キャンセル',
      confirmShip: '発送を確認',
      noOrders: '注文がありません',
      paymentMethod: '支払い方法',
      shippingMethod: '配送方法',
      address: '住所',
      paidAt: '支払日時',
      tracking: '追跡',
      statuses: {
        PENDING_PAYMENT: '支払い待ち',
        PAID: '支払い済み',
        SHIPPED: '発送済み',
        COMPLETED: '完了',
        CANCELLED: 'キャンセル済み'
      }
    },
    profile: {
      title: 'ショップ設定',
      shopInfo: 'ショップ情報',
      shopName: 'ショップ名',
      shopDescription: 'ショップ説明',
      lockedHint: 'プロフィールはロックされています。変更するには管理者にお問い合わせください。',
      saveChanges: '変更を保存'
    },
    salesReport: {
      title: '販売レポート',
      totalRevenue: '月間総収入',
      completedOrders: '完了した注文',
      pendingOrders: '保留中の注文',
      avgOrderValue: '平均注文金額',
      statusBreakdown: '注文ステータス分布',
      monthlyOrders: '今月完了した注文',
      total: '合計'
    },
    products: {
      title: '商品管理',
      publish: '商品を公開',
      batchActivate: '一括有効化',
      batchDeactivate: '一括無効化',
      quickInventory: 'クイック入庫',
      printLabels: 'ラベル印刷',
      selected: '選択済み',
      totalInvestment: '总投资額',
      totalSalePrice: '総販売価格',
      expectedProfit: '予想利益',
      edit: '編集',
      image: '画像',
      copy: '複製',
      deactivate: '非公開',
      cancel: 'キャンセル',
      update: '更新',
      publishBtn: '公開',
      uploadImageTitle: '画像アップロード',
      barcode: 'バーコード',
      uploadHint: 'ドラッグまたはクリックして画像をアップロード',
      close: '閉じる',
      noData: '商品がありません'
    },
    printLabels: {
      title: 'ラベル印刷',
      refresh: '更新',
      selectAll: 'すべて選択',
      showUnprinted: '未印刷のみ表示',
      noProducts: '該当する商品がありません',
      stock: '在庫',
      printed: '印刷済み',
      unprinted: '未印刷'
    },
    quickInventory: {
      title: 'クイック入庫',
      scanBarcode: 'バーコードスキャン',
      scanHint: 'バーコードスキャナを使用するか、手動でバーコードを入力してください',
      search: '検索',
      productInfo: '商品情報',
      barcode: 'バーコード',
      confirmEntry: '入庫を確認',
      notFound: '商品が見つかりません — クイック作成',
      createProduct: '商品を作成',
      createAndEntry: '作成して入庫',
      entryRecord: '入庫記録'
    },
    common: { logout: 'ログアウト', submit: '送信', upload: 'アップロード', search: '検索', save: '保存', cancel: 'キャンセル', close: '閉じる', confirm: '確認', loading: '読み込み中...', all: 'すべて', detail: '詳細', refresh: '更新', actions: '操作', status: 'ステータス', back: '戻る' }
  },
  ko: {
    errors: {
      'Invalid credentials': '사용자 이름 또는 비밀번호가 잘못되었습니다',
      'Account is disabled': '계정이 비활성화되었습니다',
      'Username already exists': '사용자 이름이 이미 존재합니다',
      'Email already exists': '이메일이 이미 등록되었습니다',
      'User not found': '사용자를 찾을 수 없습니다'
    },
    seller: {
      title: '판매자 센터',
      menu: { dashboard: '대시보드', products: '상품 관리', orders: '주문 관리', profile: '프로필 설정' },
      application: { title: '판매자 신청', shopName: '상점명', shopDescription: '상점 설명', idDocument: '신분 증명서' },
      validation: { shopNameRequired: '상점명을 입력해주세요', idRequired: '신분 증명서를 업로드해주세요' }
    },
    nav: {
      dashboard: '대시보드',
      products: '상품 관리',
      orders: '주문 관리',
      profile: '프로필 설정',
      quickInventory: '빠른 입고',
      salesReport: '판매 보고서',
      printLabels: '라벨 인쇄',
      logout: '로그아웃'
    },
    dashboard: {
      title: '대시보드',
      weeklySales: '주간 매출 (7일)',
      recentOrders: '최근 주문',
      salesSummary: '월간 판매 요약',
      recentCompleted: '최근 완료 주문',
      revenueOverview: '수익 개요',
      completedOrders: '완료된 주문',
      totalRevenue: '총 수익',
      avgOrderValue: '평균 주문 금액',
      viewReport: '보고서 보기'
    },
    orders: {
      title: '주문 관리',
      orderNo: '주문 번호',
      amount: '금액',
      status: '상태',
      buyer: '구매자',
      detail: '상세',
      ship: '배송',
      cancel: '취소',
      confirmShip: '배송 확인',
      noOrders: '주문이 없습니다',
      paymentMethod: '결제 수단',
      shippingMethod: '배송 방법',
      address: '주소',
      paidAt: '결제 시간',
      tracking: '배송 추적',
      statuses: {
        PENDING_PAYMENT: '결제 대기',
        PAID: '결제 완료',
        SHIPPED: '배송 중',
        COMPLETED: '완료',
        CANCELLED: '취소됨'
      }
    },
    profile: {
      title: '상점 설정',
      shopInfo: '상점 정보',
      shopName: '상점명',
      shopDescription: '상점 설명',
      lockedHint: '프로필이 잠겨 있습니다. 수정하려면 관리자에게 문의하세요.',
      saveChanges: '변경사항 저장'
    },
    salesReport: {
      title: '판매 보고서',
      totalRevenue: '월간 총 수익',
      completedOrders: '완료된 주문',
      pendingOrders: '처리 대기 주문',
      avgOrderValue: '평균 주문 금액',
      statusBreakdown: '주문 상태 분포',
      monthlyOrders: '이번 달 완료 주문',
      total: '합계'
    },
    products: {
      title: '상품 관리',
      publish: '상품 등록',
      batchActivate: '일괄 활성화',
      batchDeactivate: '일괄 비활성화',
      quickInventory: '빠른 입고',
      printLabels: '라벨 인쇄',
      selected: '선택됨',
      totalInvestment: '총 투자액',
      totalSalePrice: '총 판매 가격',
      expectedProfit: '예상 수익',
      edit: '수정',
      image: '이미지',
      copy: '복사',
      deactivate: '비활성화',
      cancel: '취소',
      update: '업데이트',
      publishBtn: '등록',
      uploadImageTitle: '이미지 업로드',
      barcode: '바코드',
      uploadHint: '드래그 또는 클릭하여 이미지 업로드',
      close: '닫기',
      noData: '상품이 없습니다'
    },
    printLabels: {
      title: '라벨 인쇄',
      refresh: '새로고침',
      selectAll: '전체 선택',
      showUnprinted: '미인쇄 상품만 표시',
      noProducts: '조건에 맞는 상품이 없습니다',
      stock: '재고',
      printed: '인쇄됨',
      unprinted: '미인쇄'
    },
    quickInventory: {
      title: '빠른 입고',
      scanBarcode: '바코드 스캔',
      scanHint: '바코드 스캐너를 사용하거나 바코드를 직접 입력하세요',
      search: '검색',
      productInfo: '상품 정보',
      barcode: '바코드',
      confirmEntry: '입고 확인',
      notFound: '상품을 찾을 수 없음 — 빠른 생성',
      createProduct: '상품 생성',
      createAndEntry: '생성 및 입고',
      entryRecord: '입고 기록'
    },
    common: { logout: '로그아웃', submit: '제출', upload: '업로드', search: '검색', save: '저장', cancel: '취소', close: '닫기', confirm: '확인', loading: '로딩 중...', all: '전체', detail: '상세', refresh: '새로고침', actions: '작업', status: '상태', back: '뒤로' }
  }
}

const i18n = createI18n({
  locale: localStorage.getItem('language') || 'en',
  fallbackLocale: 'en',
  messages,
  legacy: false,
  globalInjection: true
})

export default i18n
