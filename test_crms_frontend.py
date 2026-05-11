#!/usr/bin/env python3
"""
CRMS Frontend Comprehensive UI Test
Uses webapp-testing skill approach with Playwright
"""

from playwright.sync_api import sync_playwright
import sys
import json
from datetime import datetime

def test_crms_frontend():
    """Comprehensive test of CRMS frontend UI"""
    print("="*70)
    print("CRMS FRONTEND UI TEST - Comprehensive Analysis")
    print("="*70)

    results = {
        'timestamp': datetime.now().isoformat(),
        'tests': [],
        'screenshots': [],
        'console_errors': [],
        'ui_elements': {},
        'navigation': {}
    }

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={'width': 1920, 'height': 1080},
            locale='en-GB'
        )
        page = context.new_page()

        console_errors = []
        page.on("console", lambda msg: console_errors.append({
            'type': msg.type,
            'text': msg.text,
            'location': msg.location
        }) if msg.type == "error" else None)

        print("\n" + "="*70)
        print("1. LOGIN PAGE TEST")
        print("="*70)

        try:
            print("\n[1.1] Loading login page...")
            page.goto('http://localhost:5173/login', timeout=30000)
            page.wait_for_load_state('networkidle', timeout=30000)
            page.wait_for_timeout(3000)

            page.screenshot(path='/workspace/screenshots/login_page.png', full_page=True)
            print("    ✓ Screenshot saved: /workspace/screenshots/login_page.png")
            results['screenshots'].append('login_page.png')

            print("\n[1.2] Checking page title...")
            title = page.title()
            print(f"    Page title: {title}")
            results['tests'].append({
                'name': 'Page Title',
                'status': 'PASS' if 'CRMS' in title else 'FAIL',
                'value': title
            })

            print("\n[1.3] Identifying UI elements...")
            ui_elements = {
                'login_view': page.locator('.login-view').count(),
                'login_container': page.locator('.login-container').count(),
                'login_header': page.locator('.login-header').count(),
                'h1_title': page.locator('h1.title').count(),
                'el_card': page.locator('.el-card').count(),
                'login_form': page.locator('form').count(),
                'username_input': page.locator('input').count(),
                'password_input': page.locator('input[type="password"]').count(),
                'submit_button': page.locator('button[type="submit"]').count(),
                'el_button': page.locator('.el-button').count(),
                'el_checkbox': page.locator('.el-checkbox').count(),
                'demo_buttons': page.locator('.demo-buttons button').count(),
            }

            for element, count in ui_elements.items():
                status = "✓" if count > 0 else "✗"
                print(f"    {status} {element}: {count}")
                results['ui_elements'][element] = count

            print("\n[1.4] Testing form interactions (without backend)...")
            try:
                username_input = page.locator('input').first
                if username_input.count() > 0:
                    username_input.fill('admin@crms.local')
                    print("    ✓ Username field: interactive")

                    password_input = page.locator('input[type="password"]').first
                    if password_input.count() > 0:
                        password_input.fill('Admin123!')
                        print("    ✓ Password field: interactive")

                        page.wait_for_timeout(1000)
                        page.screenshot(path='/workspace/screenshots/login_form_filled.png')
                        print("    ✓ Screenshot with filled form saved")
                        results['screenshots'].append('login_form_filled.png')
                else:
                    print("    ⚠ Input fields not rendered (backend may be required)")
            except Exception as e:
                print(f"    ⚠ Form interaction error: {e}")

            results['tests'].append({
                'name': 'Login Page Load',
                'status': 'PASS',
                'value': f'Elements found: {sum(ui_elements.values())}'
            })

        except Exception as e:
            print(f"\n    ✗ Error during login page test: {e}")
            results['tests'].append({
                'name': 'Login Page Load',
                'status': 'FAIL',
                'value': str(e)
            })

        print("\n" + "="*70)
        print("2. NAVIGATION TEST")
        print("="*70)

        try:
            print("\n[2.1] Testing Vue Router navigation...")

            nav_items = []
            try:
                nav_selectors = [
                    '.el-menu-item',
                    'nav a',
                    '.sidebar-menu a',
                    '[role="navigation"] a'
                ]

                for selector in nav_selectors:
                    items = page.locator(selector).all()
                    if items:
                        for item in items[:10]:
                            text = item.inner_text().strip()
                            href = item.get_attribute('href') or ''
                            if text or href:
                                nav_items.append({'text': text, 'href': href})
                                print(f"    - Nav item: '{text}' -> {href}")
            except Exception as e:
                print(f"    ⚠ Navigation items not accessible: {e}")

            results['navigation']['items'] = nav_items
            results['navigation']['count'] = len(nav_items)

        except Exception as e:
            print(f"\n    ⚠ Navigation test error: {e}")

        print("\n" + "="*70)
        print("3. PAGE STRUCTURE ANALYSIS")
        print("="*70)

        try:
            print("\n[3.1] Analyzing DOM structure...")

            app_content = page.locator('#app').inner_html()
            print(f"    App content length: {len(app_content)} characters")

            if '<!---->' in app_content or '<span><!---->' in app_content:
                print("    ⚠ Vue empty template markers detected (components may need backend)")
            else:
                print("    ✓ Vue components rendered")

            page.screenshot(path='/workspace/screenshots/page_structure.png', full_page=True)
            print("    ✓ Page structure screenshot saved")
            results['screenshots'].append('page_structure.png')

        except Exception as e:
            print(f"    ⚠ Structure analysis error: {e}")

        print("\n" + "="*70)
        print("4. CONSOLE ERROR ANALYSIS")
        print("="*70)

        critical_errors = [e for e in console_errors if not any(
            keyword in e['text'].lower()
            for keyword in ['service worker', 'sw.js', 'pwa', 'mime type']
        )]

        print(f"\nTotal console errors: {len(console_errors)}")
        print(f"Critical errors (excluding PWA): {len(critical_errors)}")

        if console_errors:
            print("\n    All errors:")
            for i, err in enumerate(console_errors[:10], 1):
                print(f"    {i}. [{err['type']}] {err['text'][:100]}")

        results['console_errors'] = console_errors
        results['tests'].append({
            'name': 'Console Errors',
            'status': 'PASS' if len(critical_errors) == 0 else 'FAIL',
            'value': f'{len(critical_errors)} critical, {len(console_errors)} total'
        })

        browser.close()

    print("\n" + "="*70)
    print("5. SAVING TEST RESULTS")
    print("="*70)

    with open('/workspace/test_results.json', 'w') as f:
        json.dump(results, f, indent=2)
    print("\n    ✓ Results saved to: /workspace/test_results.json")

    print("\n" + "="*70)
    print("TEST SUMMARY")
    print("="*70)

    passed = sum(1 for t in results['tests'] if t['status'] == 'PASS')
    failed = sum(1 for t in results['tests'] if t['status'] == 'FAIL')

    print(f"\nTests Passed:  {passed}")
    print(f"Tests Failed:  {failed}")
    print(f"Screenshots:   {len(results['screenshots'])}")
    print(f"Console Errors: {len(console_errors)}")
    print(f"UI Elements:   {sum(results['ui_elements'].values())}")
    print(f"Nav Items:     {results['navigation']['count']}")

    print("\n" + "="*70)
    print("RECOMMENDATIONS")
    print("="*70)

    if failed > 0:
        print("\n    1. Fix failed tests before proceeding")

    if len(critical_errors) > 0:
        print("\n    2. Address critical console errors")

    if sum(results['ui_elements'].values()) == 0:
        print("\n    3. Start backend services for full UI rendering")
        print("       - Requires: PostgreSQL, Redis, MinIO")
        print("       - Run: docker compose -f docker-compose.dev.yml up -d")

    if results['navigation']['count'] == 0:
        print("\n    4. Authentication required to access navigation")

    print("\n" + "="*70)

    return failed == 0 and len(critical_errors) == 0

if __name__ == "__main__":
    success = test_crms_frontend()
    sys.exit(0 if success else 1)
