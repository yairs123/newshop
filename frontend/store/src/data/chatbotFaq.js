// 智能客服 FAQ 知识库
// 每个规则: match 为关键词数组, answers 为各语言的回复（支持 {placeholder}）

export const chatbotRules = [
  {
    id: 'shipping',
    match: ['发货', '配送', '物流', '快递', '运费', 'shipping', 'delivery', 'track', '运单'],
    answers: {
      'zh-CN': '📦 关于配送：\n· 标准配送 5-10 个工作日\n· 快递配送 2-3 个工作日\n· 订单满 $500 全球免运费\n· 所有订单都会提供物流跟踪单号\n\n您可以在「我的订单」中查看物流状态。',
      'zh-TW': '📦 關於配送：\n· 標準配送 5-10 個工作日\n· 快遞配送 2-3 個工作日\n· 訂單滿 $500 全球免運費\n· 所有訂單都會提供物流追蹤單號\n\n您可以在「我的訂單」中查看物流狀態。',
      'ja': '📦 配送について：\n· 標準配送 5〜10営業日\n· 速達配送 2〜3営業日\n· $500以上で送料無料\n· すべての注文に追跡番号を提供\n\n「注文履歴」で配送状況を確認できます。',
      'en': '📦 Shipping:\n· Standard 5-10 business days\n· Express 2-3 business days\n· Free worldwide shipping over $500\n· All orders include tracking\n\nYou can track in "My Orders".'
    }
  },
  {
    id: 'refund',
    match: ['退款', '退货', '退钱', 'return', 'refund', '退回'],
    answers: {
      'zh-CN': '💳 退款政策：\n· 收货后 14 天内可申请退货退款\n· 商品需保持原状，附原包装和评级证书\n· 退款在收到退货后 5-10 个工作日处理\n· 商品描述有误时，退货运费由卖家承担\n\n您可以在「我的订单」中申请退款。',
      'zh-TW': '💳 退款政策：\n· 收貨後 14 天內可申請退貨退款\n· 商品需保持原狀，附原包裝和評級證書\n· 退款在收到退貨後 5-10 個工作日處理\n· 商品描述有誤時，退貨運費由賣家承擔\n\n您可以在「我的訂單」中申請退款。',
      'ja': '💳 返金ポリシー：\n· 受領後14日以内に返品・返金申請可能\n· 商品は元の状態、元の梱包と鑑定書が必要\n· 返金は返品受領後5〜10営業日で処理\n· 商品説明に誤りがある場合、返送料は販売者が負担\n\n「注文履歴」から返金を申請できます。',
      'en': '💳 Refund policy:\n· 14-day return window after receipt\n· Items must be in original condition with packaging & cert\n· Refunds processed in 5-10 business days\n· Seller covers return shipping if item was misdescribed\n\nYou can request refund in "My Orders".'
    }
  },
  {
    id: 'payment',
    match: ['支付', '付款', '信用卡', '银行', '支付宝', 'pay', 'payment', 'credit'],
    answers: {
      'zh-CN': '💳 我们支持多种支付方式：\n· 信用卡 / PayPal\n· 支付宝 / 微信支付\n· GrabPay / PayNow / 银行转账\n\n在结算时选择您方便的方式即可。',
      'zh-TW': '💳 我們支持多種支付方式：\n· 信用卡 / PayPal\n· 支付寶 / 微信支付\n· GrabPay / PayNow / 銀行轉賬\n\n在結算時選擇您方便的方式即可。',
      'ja': '💳 対応する支払い方法：\n· クレジットカード / PayPal\n· Alipay / WeChat Pay\n· GrabPay / PayNow / 銀行振込\n\nチェックアウト時にお選びください。',
      'en': '💳 We support:\n· Credit Card / PayPal\n· Alipay / WeChat Pay\n· GrabPay / PayNow / Bank Transfer\n\nChoose your preferred method at checkout.'
    }
  },
  {
    id: 'order',
    match: ['订单', '查询', '状态', 'order', '我的订单', '下单'],
    answers: {
      'zh-CN': '📋 订单查询：\n· 登录后进入「我的账户」→「我的订单」\n· 可以查看订单状态、支付、物流信息\n· 待支付订单可以立即支付或取消\n· 遇到问题可以直接在订单详情联系我们\n\n需要我帮您处理具体订单问题吗？可以描述一下。',
      'zh-TW': '📋 訂單查詢：\n· 登入後進入「我的帳戶」→「我的訂單」\n· 可以查看訂單狀態、支付、物流資訊\n· 待支付訂單可以立即支付或取消\n· 遇到問題可以直接在訂單詳情聯絡我們\n\n需要我幫您處理具體訂單問題嗎？可以描述一下。',
      'ja': '📋 注文照会：\n· ログイン後「マイアカウント」→「注文履歴」\n· 注文ステータス、支払い、配送情報を確認\n· 未払い注文はすぐに支払いまたはキャンセル可能\n· 問題があれば注文詳細からお問い合わせください',
      'en': '📋 Order inquiry:\n· Login → "My Account" → "My Orders"\n· View status, payment, tracking\n· Pending orders can be paid or cancelled\n· Contact us from order detail if you need help'
    }
  },
  {
    id: 'seller',
    match: ['卖家', '入驻', '开店', 'sell', 'seller', '开店申请', '成为卖家'],
    answers: {
      'zh-CN': '🏪 成为卖家：\n· 登录后进入「我的账户」→「我要开店」\n· 填写店铺名称和介绍提交申请\n· 我们会在 1-2 个工作日内审核\n· 审核通过后即可上架商品\n\n需要更多了解入驻流程吗？',
      'zh-TW': '🏪 成為賣家：\n· 登入後進入「我的帳戶」→「我要開店」\n· 填寫店鋪名稱和介紹提交申請\n· 我們會在 1-2 個工作日內審核\n· 審核通過後即可上架商品\n\n需要更多了解入駐流程嗎？',
      'ja': '🏪 販売者になる：\n· ログイン後「マイアカウント」→「販売者になる」\n· 店舗名と紹介文を入力して申請\n· 1〜2営業日以内に審査\n· 承認後すぐに商品を出品できます',
      'en': '🏪 Become a seller:\n· Login → "My Account" → "Open Store"\n· Submit shop name & description\n· Reviewed within 1-2 business days\n· Start listing products after approval'
    }
  },
  {
    id: 'authenticity',
    match: ['正品', '真假', '评级', '证书', 'authentic', 'grading', 'NGC', 'PCGS', '鉴定'],
    answers: {
      'zh-CN': '🔍 正品保障：\n· 所有商品均经 NGC / PCGS / PMG 专业评级\n· 每件藏品附带评级证书\n· 卖家必须通过资质审核\n· 平台提供买家保护计划\n\n放心选购！',
      'zh-TW': '🔍 正品保障：\n· 所有商品均經 NGC / PCGS / PMG 專業評級\n· 每件藏品附帶評級證書\n· 賣家必須通過資質審核\n· 平台提供買家保護計劃\n\n放心選購！',
      'ja': '🔍 正品保証：\n· すべての商品はNGC / PCGS / PMGの専門鑑定済み\n· 各コインに鑑定書を添付\n· 販売者は資格審査を通過\n· バイヤー保護プログラムあり\n\n安心してお買い求めください！',
      'en': '🔍 Authenticity guarantee:\n· All coins graded by NGC / PCGS / PMG\n· Every item includes grading certificate\n· Sellers are vetted\n· Buyer protection program\n\nShop with confidence!'
    }
  },
  {
    id: 'discount',
    match: ['优惠', '折扣', '促销', 'discount', 'coupon', 'promo', '优惠券'],
    answers: {
      'zh-CN': '🎁 优惠信息：\n· 订单满 $500 免运费\n· 关注我们获取最新促销活动\n· 卖家会不定期推出折扣\n\n目前没有自动优惠码，但我们会及时更新活动！',
      'zh-TW': '🎁 優惠資訊：\n· 訂單滿 $500 免運費\n· 關注我們獲取最新促銷活動\n· 賣家會不定期推出折扣\n\n目前沒有自動優惠碼，但我們會及時更新活動！',
      'ja': '🎁 お得な情報：\n· $500以上の注文で送料無料\n· 最新セールはフォローでチェック\n· 販売者による期間限定割引あり\n\nキャンペーンは随時更新します！',
      'en': '🎁 Offers:\n· Free shipping over $500\n· Follow us for latest promos\n· Sellers run occasional discounts\n\nNo auto-coupons currently, but stay tuned!'
    }
  },
  {
    id: 'human',
    match: ['人工', '客服', '真人', 'human', 'agent', '人工客服', '转人工', '联系'],
    answers: {
      'zh-CN': '👨‍💼 人工客服：\n我们的智能客服 24 小时在线，可以回答大部分问题。\n如果您需要人工帮助，请填写下方的留言表单，\n我们会在 24 小时内回复您。\n\n或者发送邮件至 support@coinmarket.com',
      'zh-TW': '👨‍💼 人工客服：\n我們的智能客服 24 小時在線，可以回答大部分問題。\n如果您需要人工幫助，請填寫下方的留言表單，\n我們會在 24 小時內回覆您。\n\n或者發送郵件至 support@coinmarket.com',
      'ja': '👨‍💼 有人対応：\nAIカスタマーサービスは24時間対応で、多くの質問にお答えできます。\n有人対応をご希望の場合は、下のフォームをご利用ください。\n24時間以内に返信いたします。\n\nまたは support@coinmarket.com までメール',
      'en': '👨‍💼 Human support:\nOur AI assistant is available 24/7 for most questions.\nFor human help, use the message form below.\nWe reply within 24 hours.\n\nOr email support@coinmarket.com'
    }
  },
  {
    id: 'account',
    match: ['账户', '登录', '密码', '账号', 'account', 'login', 'password'],
    answers: {
      'zh-CN': '👤 账户帮助：\n· 登录问题可以点「忘记密码」重置\n· 修改个人资料在「我的账户」\n· 地址管理在「我的账户」→「收货地址」\n\n如果遇到登录异常，请描述具体情况，我帮您排查。',
      'zh-TW': '👤 帳戶幫助：\n· 登入問題可以點「忘記密碼」重置\n· 修改個人資料在「我的帳戶」\n· 地址管理在「我的帳戶」→「收貨地址」\n\n如果遇到登入異常，請描述具體情況，我幫您排查。',
      'ja': '👤 アカウントヘルプ：\n· ログイン問題は「パスワードを忘れた」でリセット\n· プロフィール編集は「マイアカウント」\n· 住所管理は「マイアカウント」→「住所管理」\n\n問題があれば状況を詳しく教えてください。',
      "en": "👤 Account help:\n· Use 'Forgot password' to reset login\n· Edit profile in 'My Account'\n· Manage addresses in 'My Account'\n\nDescribe the issue and I'll help troubleshoot."
    }
  }
]

// 默认/未知问题回复
export const chatbotFallback = {
  'zh-CN': '🤔 抱歉，我还不太明白您的问题。您可以换个说法试试，或者问我：\n· 📦 发货配送\n· 💳 退款政策\n· 💳 支付方式\n· 📋 订单查询\n· 🏪 成为卖家\n· 🔍 正品保障\n\n如果还是无法解决，请填写下方的留言表单，人工客服 24 小时内回复您。',
  'zh-TW': '🤔 抱歉，我還不太明白您的問題。您可以換個說法試試，或者問我：\n· 📦 發貨配送\n· 💳 退款政策\n· 💳 支付方式\n· 📋 訂單查詢\n· 🏪 成為賣家\n· 🔍 正品保障\n\n如果還是無法解決，請填寫下方的留言表單，人工客服 24 小時內回覆您。',
  'ja': '🤔 申し訳ありません。ご質問を理解できませんでした。別の表現でお試しいただくか、以下をご質問ください：\n· 📦 配送について\n· 💳 返金ポリシー\n· 💳 支払い方法\n· 📋 注文照会\n· 🏪 販売者になる\n· 🔍 正品保証\n\n解決しない場合は、下のフォームからお問い合わせください。24時間以内に返信します。',
  'en': '🤔 Sorry, I didn\'t quite understand. Try rephrasing, or ask me about:\n· 📦 Shipping\n· 💳 Refund policy\n· 💳 Payment methods\n· 📋 Order inquiry\n· 🏪 Becoming a seller\n· 🔍 Authenticity guarantee\n\nStill stuck? Use the message form below and we\'ll reply within 24 hours.'
}

// 开场白
export const chatbotGreeting = {
  'zh-CN': '👋 您好！我是 CoinMarket 智能客服，24 小时为您服务。\n\n您可以问我：\n· 📦 发货配送\n· 💳 退款政策\n· 💳 支付方式\n· 📋 订单查询\n· 🏪 成为卖家\n· 🔍 正品保障\n\n或者直接输入您的问题！',
  'zh-TW': '👋 您好！我是 CoinMarket 智能客服，24 小時為您服務。\n\n您可以問我：\n· 📦 發貨配送\n· 💳 退款政策\n· 💳 支付方式\n· 📋 訂單查詢\n· 🏪 成為賣家\n· 🔍 正品保障\n\n或者直接輸入您的問題！',
  'ja': '👋 こんにちは！CoinMarket のAIカスタマーサービスです。24時間対応しています。\n\nお聞きになりたいことをどうぞ：\n· 📦 配送について\n· 💳 返金ポリシー\n· 💳 支払い方法\n· 📋 注文照会\n· 🏪 販売者になる\n· 🔍 正品保証\n\nまたは直接ご質問ください！',
  'en': '👋 Hi! I\'m CoinMarket\'s AI assistant, available 24/7.\n\nAsk me about:\n· 📦 Shipping\n· 💳 Refund policy\n· 💳 Payment methods\n· 📋 Order inquiry\n· 🏪 Becoming a seller\n· 🔍 Authenticity guarantee\n\nOr just type your question!'
}

// 快捷问题
export const chatbotQuickQuestions = {
  'zh-CN': ['📦 怎么发货？', '💳 怎么退款？', '📋 怎么查订单？', '🏪 怎么开店？'],
  'zh-TW': ['📦 怎麼發貨？', '💳 怎麼退款？', '📋 怎麼查訂單？', '🏪 怎麼開店？'],
  'ja': ['📦 配送はどうなりますか？', '💳 返金は？', '📋 注文確認は？', '🏪 販売者になるには？'],
  'en': ['📦 How does shipping work?', '💳 How do refunds work?', '📋 How to check orders?', '🏪 How to sell?']
}
