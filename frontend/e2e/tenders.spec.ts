import { test, expect } from '@playwright/test'

const BASE_URL = process.env.BASE_URL || 'http://localhost:5173'

test.describe('Frontend Routes', () => {
  const routes = [
    '/tenders',
    '/dashboard',
    '/contracts',
    '/applications',
    '/subcontractors',
    '/operatives',
    '/plant',
    '/procurement',
    '/health-safety',
    '/companies',
    '/reports',
    '/contacts',
    '/sites',
    '/projects',
    '/admin',
    '/quality',
    '/wip',
    '/adoption',
  ]

  for (const route of routes) {
    test(`${route} loads without 500 error`, async ({ page }) => {
      const response = await page.goto(`${BASE_URL}${route}`)
      expect(response?.status()).toBeLessThan(500)
    })
  }
})
