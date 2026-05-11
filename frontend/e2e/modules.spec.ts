import { test, expect } from '@playwright/test'

const BASE_URL = process.env.BASE_URL || 'http://localhost:5173'

test.describe('Application Module Tests', () => {
  const pages = [
    '/applications',
    '/subcontractors',
    '/health-safety',
    '/plant',
    '/operatives',
    '/procurement',
    '/reports',
    '/companies',
    '/quality',
    '/wip',
    '/adoption',
  ]

  for (const pagePath of pages) {
    test(`${pagePath} page accessible`, async ({ page }) => {
      const response = await page.goto(`${BASE_URL}${pagePath}`)
      expect(response?.status()).toBeLessThan(500)
    })
  }
})
