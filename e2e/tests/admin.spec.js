const { test, expect } = require('@playwright/test')

const ADMIN = 'http://localhost:3002'

test.describe('Admin flows', () => {
  test('logs in with admin account', async ({ page }) => {
    await page.goto(ADMIN + '/login')
    const inputs = page.locator('.input-field')
    await expect(inputs.first()).toBeVisible({ timeout: 20000 })
    await inputs.nth(0).fill('admin')
    await inputs.nth(1).fill('123')
    await page.locator('.login-btn').click()

    await expect(page).toHaveURL(/\/dashboard/, { timeout: 20000 })
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })
  })

  test('dashboard loads with stat cards', async ({ page }) => {
    // Go straight to dashboard after setting token via UI login
    await page.goto(ADMIN + '/login')
    const inputs = page.locator('.input-field')
    await expect(inputs.first()).toBeVisible()
    await inputs.nth(0).fill('admin')
    await inputs.nth(1).fill('123')
    await page.locator('.login-btn').click()

    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })
    const statCards = page.locator('.stat-card')
    await expect(statCards).toHaveCount(4)
    await expect(statCards.first().locator('.stat-label')).toBeVisible()
  })

  test('navigates to orders page via sidebar', async ({ page }) => {
    await page.goto(ADMIN + '/login')
    const inputs = page.locator('.input-field')
    await expect(inputs.first()).toBeVisible()
    await inputs.nth(0).fill('admin')
    await inputs.nth(1).fill('123')
    await page.locator('.login-btn').click()
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })

    // Expand the "订单管理" submenu and click "订单列表"
    const ordersSubMenu = page.locator('.el-sub-menu', { hasText: '订单管理' })
    await ordersSubMenu.locator('.el-sub-menu__title').click()
    await page.locator('.el-menu-item', { hasText: '订单列表' }).click()

    await expect(page).toHaveURL(/\/orders/, { timeout: 20000 })
    await expect(page.locator('.orders-page')).toBeVisible({ timeout: 20000 })
  })

  test('navigates to products page via sidebar', async ({ page }) => {
    await page.goto(ADMIN + '/login')
    const inputs = page.locator('.input-field')
    await expect(inputs.first()).toBeVisible()
    await inputs.nth(0).fill('admin')
    await inputs.nth(1).fill('123')
    await page.locator('.login-btn').click()
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })

    // Expand the "商品管理" submenu and click "商品列表"
    const productsSubMenu = page.locator('.el-sub-menu', { hasText: '商品管理' })
    await productsSubMenu.locator('.el-sub-menu__title').click()
    await page.locator('.el-menu-item', { hasText: '商品列表' }).click()

    await expect(page).toHaveURL(/\/products/, { timeout: 20000 })
    await expect(page.locator('.products-page')).toBeVisible({ timeout: 20000 })
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 20000 })
  })
})
