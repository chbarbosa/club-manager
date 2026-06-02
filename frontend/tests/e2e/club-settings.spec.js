import { test } from '@playwright/test'

test.skip('admin updates club identity and primary colour', async ({ page }) => {
  // Prompt 03 provides the real admin login flow required to reach this page.
  await page.goto('/login')
})

