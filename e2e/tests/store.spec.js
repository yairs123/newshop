const { test, expect } = require('@playwright/test')

const STORE = 'http://localhost:3000'

test.describe('Store (buyer) flows', () => {
  test('home page loads products', async ({ page }) => {
    await page.goto(STORE + '/')
    // Wait for product cards to render after API load
    const card = page.locator('.product-card').first()
    await expect(card).toBeVisible({ timeout: 20000 })
    // A product card should have a title and a price
    await expect(card.locator('.card-title')).toBeVisible()
    await expect(card.locator('.card-price')).toBeVisible()
  })

  test('product search returns matching results', async ({ page }) => {
    await page.goto(STORE + '/')
    // Type in the header search bar and press Enter
    const searchInput = page.locator('.search-bar input')
    await expect(searchInput).toBeVisible()
    await searchInput.fill('Morgan')
    await searchInput.press('Enter')

    // Should land on the /products page with the keyword indicator
    await expect(page).toHaveURL(/\/products\?keyword=Morgan/)
    const searchTag = page.locator('.active-search-value')
    await expect(searchTag).toHaveText('Morgan')

    // Results should appear (Morgan Silver Dollar exists in seed data)
    await expect(page.locator('.product-card').first()).toBeVisible({ timeout: 20000 })
  })

  test('clicking a product opens its detail page', async ({ page }) => {
    await page.goto(STORE + '/')
    const firstCard = page.locator('.product-card').first()
    await expect(firstCard).toBeVisible({ timeout: 20000 })
    // Read the card title before navigating
    const title = await firstCard.locator('.card-title').innerText()
    await firstCard.click()

    await expect(page).toHaveURL(/\/products\/\d+/)
    await expect(page.locator('.product-detail-page')).toBeVisible({ timeout: 20000 })
    await expect(page.locator('.product-title')).toHaveText(title)
    await expect(page.locator('.add-to-cart-btn')).toBeVisible()
  })

  test('adds product to cart and cart drawer shows item', async ({ page }) => {
    await page.goto(STORE + '/products/81')
    await expect(page.locator('.product-title')).toBeVisible({ timeout: 20000 })
    const addBtn = page.locator('.add-to-cart-btn')
    await expect(addBtn).toBeVisible()
    await addBtn.click()

    // Cart drawer opens with the added item
    const drawerItem = page.locator('.drawer-item').first()
    await expect(drawerItem).toBeVisible({ timeout: 10000 })
    await expect(drawerItem.locator('.di-title')).toContainText('test')
    // Header cart badge should show count >= 1
    await expect(page.locator('.cart-badge .el-badge__content')).toHaveText(/[1-9]/)
  })

  test('login flow works with buyer account', async ({ page }) => {
    await page.goto(STORE + '/auth')
    const usernameInput = page.locator('.auth-form input').first()
    await expect(usernameInput).toBeVisible({ timeout: 20000 })
    await usernameInput.fill('buyer')
    await usernameInput.press('Enter')

    // Step 2: password
    const passwordInput = page.locator('.auth-form input[type="password"]')
    await expect(passwordInput).toBeVisible()
    await passwordInput.fill('123')
    await passwordInput.press('Enter')

    // Redirected home and user name shown in header
    await expect(page).toHaveURL(STORE + '/')
    await expect(page.locator('.user-name')).toHaveText('buyer', { timeout: 20000 })
  })
})
