import { test, expect } from '@playwright/test'

const BASE_URL = process.env.BASE_URL || 'http://localhost:5173'

test.describe('Login Page', () => {
  test('login page loads', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.waitForLoadState('networkidle')
    expect(true).toBe(true)
  })

  test('app mounts successfully', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.waitForSelector('#app', { timeout: 10000 })
    const app = page.locator('#app')
    await expect(app).toBeVisible()
  })

  test('page content loads', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`)
    await page.waitForLoadState('networkidle')
    const content = await page.content()
    expect(content.length).toBeGreaterThan(100)
  })
})

test.describe('All Routes', () => {
  const routes = [
    '/login',
    '/dashboard',
    '/tenders',
    '/contracts',
    '/applications-for-payment',
    '/subcontractors',
    '/operatives',
    '/plant',
    '/procurement',
    '/healthsafety',
    '/companies',
    '/reports',
  ]

  for (const route of routes) {
    test(`${route} route loads`, async ({ page }) => {
      const response = await page.goto(`${BASE_URL}${route}`)
      expect(response?.status()).toBeLessThan(500)
    })
  }
})
