const { test, expect } = require('@playwright/test')

const SELLER = 'http://localhost:3001'

test.describe('Seller flows', () => {
  test('logs in with seller account', async ({ page }) => {
    await page.goto(SELLER + '/login')
    const inputs = page.locator('.login-card input')
    await expect(inputs.nth(0)).toBeVisible({ timeout: 20000 })
    await inputs.nth(0).fill('seller')
    await inputs.nth(1).fill('123')
    await page.locator('.login-card .el-button').click()

    // Seller with a profile lands on /dashboard
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 20000 })
  })

  test('dashboard loads with stats', async ({ page }) => {
    await page.goto(SELLER + '/login')
    const inputs = page.locator('.login-card input')
    await expect(inputs.nth(0)).toBeVisible()
    await inputs.nth(0).fill('seller')
    await inputs.nth(1).fill('123')
    await page.locator('.login-card .el-button').click()

    await expect(page).toHaveURL(/\/dashboard/, { timeout: 20000 })
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })
    await expect(page.locator('.stat-value').first()).toBeVisible()
  })

  test('navigates to products page via sidebar', async ({ page }) => {
    await page.goto(SELLER + '/login')
    const inputs = page.locator('.login-card input')
    await expect(inputs.nth(0)).toBeVisible()
    await inputs.nth(0).fill('seller')
    await inputs.nth(1).fill('123')
    await page.locator('.login-card .el-button').click()
    await expect(page.locator('.stat-card').first()).toBeVisible({ timeout: 20000 })

    // Click "Products" in the sidebar menu
    await page.locator('.el-menu-item', { hasText: 'Products' }).click()

    await expect(page).toHaveURL(/\/products/, { timeout: 20000 })
    await expect(page.locator('.products-page')).toBeVisible({ timeout: 20000 })
    await expect(page.locator('.el-table').first()).toBeVisible({ timeout: 20000 })
  })
})
