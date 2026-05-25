import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    seller: {
      title: 'Seller Center',
      menu: { dashboard: 'Dashboard', products: 'Products', orders: 'Orders', profile: 'Profile' },
      application: { title: 'Apply to Become a Seller', shopName: 'Shop Name', shopDescription: 'Shop Description', idDocument: 'ID Document' },
      validation: { shopNameRequired: 'Shop name is required', idRequired: 'ID document is required' }
    },
    common: { logout: 'Logout', submit: 'Submit', upload: 'Upload', search: 'Search' }
  },
  'zh-CN': {
    seller: {
      title: '卖家中心',
      menu: { dashboard: '控制台', products: '商品管理', orders: '订单管理', profile: '资料设置' },
      application: { title: '申请成为卖家', shopName: '店铺名称', shopDescription: '店铺描述', idDocument: '身份证明' },
      validation: { shopNameRequired: '请输入店铺名称', idRequired: '请上传身份证明' }
    },
    common: { logout: '退出登录', submit: '提交', upload: '上传', search: '搜索' }
  },
  'zh-TW': {
    seller: {
      title: '賣家中心',
      menu: { dashboard: '控制台', products: '商品管理', orders: '訂單管理', profile: '資料設置' },
      application: { title: '申請成為賣家', shopName: '店鋪名稱', shopDescription: '店鋪描述', idDocument: '身份證明' },
      validation: { shopNameRequired: '請輸入店鋪名稱', idRequired: '請上傳身份證明' }
    },
    common: { logout: '退出登錄', submit: '提交', upload: '上傳', search: '搜索' }
  },
  ja: {
    seller: {
      title: '売りセンタ',
      menu: { dashboard: 'ダッシュボード', products: '商品管理', orders: '注文管理', profile: 'プロフィール' },
      application: { title: '売り手になる', shopName: 'ショップ名', shopDescription: 'ショップ説明', idDocument: '身分証明書' },
      validation: { shopNameRequired: 'ショップ名は必須です', idRequired: '身分証明書が必要です' }
    },
    common: { logout: 'ログアウト', submit: '送信', upload: 'アップロード', search: '検索' }
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
