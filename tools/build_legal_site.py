#!/usr/bin/env python3
"""Build the public, tracker-free policy site from Loom's release configuration."""
import argparse
from html import escape
import json
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[1]

def configuration(allow_draft=False):
    values = {}
    for line in (ROOT / 'config/loom-legal.properties').read_text().splitlines():
        if line.strip() and not line.startswith('#'):
            key, value = line.split('=', 1)
            values[key] = value.strip()
    required = ('LEGAL_SITE_URL', 'SUPPORT_EMAIL', 'PUBLISHER_NAME', 'POLICY_VERSION')
    missing = [key for key in required if not values.get(key)]
    if missing and not allow_draft:
        raise ValueError('Confirm these publisher details before publication: ' + ', '.join(missing))
    return values


def build(output, allow_draft=False):
    config = configuration(allow_draft)
    output.mkdir(parents=True, exist_ok=True)
    publisher = escape(config['PUBLISHER_NAME'] or '[publisher name awaiting confirmation]')
    email = escape(config['SUPPORT_EMAIL'])
    version = escape(config['POLICY_VERSION'])
    site = config['LEGAL_SITE_URL'].rstrip('/')
    prefix = '/' + '/'.join(site.split('/')[3:]).strip('/')
    if prefix == '/': prefix = ''
    contact = f'<a href="mailto:{email}">{email}</a>'
    provider = f'Loom is provided by its independent developer. Contact: {contact}.'
    pages = {
        '': ('Life, woven together.', f'''
            <p class="lead">A little less household admin. A little more life together.</p>
            <p>Loom helps roommates and couples manage shared shopping lists, reminders, expenses, recurring bills and household feeds.</p>
            <div class="cards">
                <a href="{prefix}/privacy/"><strong>Your privacy</strong><span>Understand the data behind your household.</span></a>
                <a href="{prefix}/terms/"><strong>Terms of use</strong><span>The agreement for using Loom together.</span></a>
                <a href="{prefix}/delete-account/"><strong>Delete your account</strong><span>Request deletion without installing the app.</span></a>
            </div><p>Support and privacy contact: {contact}</p>'''),
        'privacy': ('Privacy policy', f'''
            <p class="lead">Your household information belongs in your household.</p>
            <p><strong>Effective date and policy version: {version}.</strong> This policy covers Loom, Android package <code>com.eyther.lumbridge</code>, and its public policy pages.</p>
            <h2>Who is responsible</h2><p>{provider} We act as the controller of the personal data described here.</p>
            <h2>Information Loom processes</h2>
            <ul>
              <li><strong>Account information:</strong> your email address, account identifier, display name and authentication/session information. Supabase Auth handles passwords and authentication; Loom stores sessions using the operating system's protected storage.</li>
              <li><strong>Household information:</strong> household names, membership and roles, invitations, chosen currency and timezone.</li>
              <li><strong>Shared content:</strong> shopping lists and items, reminder titles and schedules, expenses, participants and shares, repayment records, recurring schedules and monthly statement snapshots.</li>
              <li><strong>Feed information:</strong> RSS/Atom subscription URLs and your article-read state.</li>
              <li><strong>Device preferences:</strong> theme, analytics consent and notification preferences. Enabling push notifications registers a device token and platform with Loom for delivery. The Firebase Messaging SDK can initialize installation and delivery identifiers when the Android app starts, separately from optional usage analytics.</li>
              <li><strong>Operational data:</strong> our hosting and infrastructure providers process request information, including IP addresses, timestamps and security/error logs, to operate and protect the service.</li>
            </ul>
            <h2>Why we use it</h2><p>We process account and household data to provide the service you request, including authentication, household sharing, calculations, reminders, feed reading and exports. We rely on performance of our agreement for these functions, consent for optional analytics, and legitimate interests in service reliability and security. We may also process information to meet applicable legal obligations or respond to rights requests.</p>
            <h2>Optional Firebase Analytics</h2><p>The Android app offers Google Firebase Analytics. Collection is <strong>off by default</strong> until you turn on Optional usage analytics in Settings. We log a limited set of screen names and successful feature-use events. Firebase also processes app and device information, a pseudonymous app-instance identifier, session information and approximate geographic information derived by Google from network requests. We do not attach your account ID, email, household identifier, shopping/reminder text, RSS URLs, expense amounts or financial records to analytics events.</p>
            <p>Loom disables advertising-ID collection and advertising/personalization consent. The app has no advertisements. Analytics is currently available in the Android build. The public policy website contains no analytics scripts or advertising trackers.</p>
            <p>Turn analytics off in Settings at any time to stop future collection and reset local analytics data and its app-instance identifier. Previously processed data follows the Google Analytics property's retention settings; aggregated reports may remain. Contact us about analytics data requests. Learn more from <a href="https://firebase.google.com/support/privacy">Firebase privacy information</a> and <a href="https://policies.google.com/privacy">Google's Privacy Policy</a>.</p>
            <h2>Who can see information</h2><p>Members of a household can see the content and financial records shared with that household, together with member display names. Treat shared lists, reminders and descriptions as visible to those members. You choose the people or apps that receive exported statements.</p>
            <p>Supabase provides authentication, database hosting, server functions and related infrastructure. Google Firebase Cloud Messaging delivers Android notifications; Apple Push Notification service delivers iOS notifications. Notification messages may contain household content and may appear on a device's lock screen. Google processes optional Android analytics when enabled. GitHub hosts these public legal pages. These providers process information needed for their services; their documentation describes their locations and safeguards.</p>
            <p>We do not sell personal data. We do not use your household content to target advertising. Information may be processed outside your country; applicable provider contractual safeguards and data-protection requirements apply to international transfers.</p>
            <h2>Retention and deletion</h2><p>We keep active account information and household content for the service's operation. You can request account and associated-data deletion using Settings → Delete account and data, or the <a href="{prefix}/delete-account/">public deletion page</a>. We verify requests and normally complete them within 30 days, or explain any lawful extension.</p>
            <p>Deletion removes account credentials and identifying profile information, device registrations, personal notification preferences and personal article-read state. Household ownership may need to be transferred or a household you use alone removed. Shared records may be retained in pseudonymized form where a documented legal obligation or other lawful exception requires retention; we explain any retained information and the applicable reason when handling your request. You may also request redaction of personal information from shared content.</p>
            <p>Provider backups and security logs can retain residual information until their normal retention periods expire. We do not return deleted identifying information to active use when restoring ordinary service backups. Data you exported or shared independently is controlled by its recipients.</p>
            <h2>Your choices and rights</h2><p>You can withdraw optional analytics consent, manage notification permissions, switch households and export monthly statements. Depending on the law that applies to you, you may request access, correction, deletion, portability or restriction of your data, object to processing, and complain to your local data-protection authority. Contact {contact}; we may verify your identity before responding.</p>
            <h2>Age and security</h2><p>Loom is intended for adults aged 18 or over. We use authenticated connections, household access controls and platform-protected session storage. No service can guarantee complete security; contact us if you believe an account or household has been compromised.</p>
            <h2>Changes</h2><p>We update the effective date when this policy changes. Material changes will be communicated through the app or another appropriate channel. New optional data uses require an appropriate choice before they begin.</p>'''),
        'terms': ('Terms of use', f'''
            <p class="lead">A shared home works better with clear expectations.</p><p><strong>Effective date and terms version: {version}.</strong></p>
            <h2>The agreement</h2><p>{provider} These terms govern Loom. By creating an account and accepting the terms, you agree to them. The <a href="{prefix}/privacy/">Privacy Policy</a> explains personal-data processing. You must be at least 18 and able to enter this agreement.</p>
            <h2>Your account and households</h2><p>Provide an email you control, protect your sign-in credentials, and contact us about unauthorized access. Invite people only to households you are authorized to manage. Other members can see information shared with their household. Household owners should use membership and invitation privileges responsibly.</p>
            <h2>Expenses and reminders</h2><p>Loom records information and calculates balances from the data members enter. Members remain responsible for checking amounts, splits, recurring schedules, statements and repayments. Payments happen through methods members arrange independently. Loom provides a recordkeeping tool; it does not provide banking, payment execution or financial advice.</p>
            <p>Recurring schedules can generate additional records until paused or cancelled. Notifications depend on device settings, permissions and service availability. Keep independent arrangements for critical or time-sensitive obligations.</p>
            <h2>Your content</h2><p>You retain your rights in content you add. You give us permission to host, process and display it as needed to provide Loom to you and the household members you choose. Add content you have permission to share. Respect other people's privacy and rights.</p>
            <h2>Acceptable use</h2><p>Use Loom lawfully. Do not attempt to access other households, compromise security, abuse invitations or notifications, distribute unlawful content, or disrupt the service. We may restrict access to address abuse, security concerns or legal requirements, and will provide notice where appropriate.</p>
            <h2>Third-party services and feeds</h2><p>Loom relies on infrastructure and notification providers. RSS publishers control their content and linked websites. Opening external links or sharing an export can involve another provider's terms and privacy practices.</p>
            <h2>Availability and changes</h2><p>We aim to provide a useful service but availability, delivery times and uninterrupted operation can vary. Keep copies of information you need. We may improve, change or discontinue features, with reasonable notice for material changes where practicable or legally required. Paid features, if introduced, require separate price and purchase disclosures.</p>
            <h2>Responsibility and consumer rights</h2><p>You are responsible for your entries and household agreements. Our responsibility is subject to applicable law. Nothing in these terms excludes rights or liabilities that cannot lawfully be excluded, including mandatory consumer protections and liability for conduct for which the law prohibits exclusion.</p>
            <h2>Leaving Loom</h2><p>You can stop using Loom and <a href="{prefix}/delete-account/">request deletion of your account and associated data</a>. Shared records and household ownership are handled as explained in the Privacy Policy and deletion process. Uninstalling the app alone does not delete your server account.</p>
            <h2>Applicable law and contact</h2><p>Mandatory consumer protections, applicable laws and dispute-resolution rights in the jurisdiction that applies to you remain in effect. Contact {contact} to raise an issue or ask about these terms.</p>'''),
        'delete-account': ('Delete your Loom account and data', f'''
            <p class="lead">You can request deletion here, even if you have already uninstalled Loom.</p>
            <div class="request"><h2>Send a deletion request</h2><p>Email {contact} from the email address used for your Loom account. Use the subject <strong>Loom account deletion</strong>.</p><a class="button" href="mailto:{email}?subject=Loom%20account%20deletion">Request account deletion</a></div>
            <h2>What to include</h2><p>Tell us that you want your Loom account and associated data deleted. If you cannot access your account email, tell us how to reach you so we can arrange verification. Never send passwords, signing keys or authentication tokens. If you own a shared household, identify it so we can discuss ownership transfer.</p>
            <h2>What happens next</h2><ol><li>We verify that the request comes from the account holder.</li><li>We explain any required ownership transfer and any shared records that must be retained.</li><li>We remove credentials, identifying profile information, device tokens, personal notification preferences and article-read state, and remove households used only by you where appropriate.</li><li>We confirm completion, normally within 30 days of verification, or explain a lawful extension.</li></ol>
            <p>Some shared records may remain in pseudonymized form where a documented legal obligation or other lawful exception requires retention. Backups and security logs follow provider retention periods. We explain relevant exceptions in our response. You can request deletion or correction of specific personal information without deleting your entire account.</p>
            <h2>Analytics and your device</h2><p>Turn off Optional usage analytics in the Android app's Settings to stop future collection and reset its local analytics identifier. Contact us about historic analytics data requests; we do not link analytics events to your Loom account. Uninstalling alone does not submit an account deletion request.</p>
            <h2>Contact</h2><p>{provider} See the <a href="{prefix}/privacy/">Privacy Policy</a> for the complete data-handling explanation.</p>'''),
    }
    style = '''*{box-sizing:border-box}body{margin:0;background:#fbf8f1;color:#2d302b;font:17px/1.75 system-ui,-apple-system,sans-serif}a{color:#78551b;text-underline-offset:4px}a:focus-visible{outline:3px solid #9b772b;outline-offset:5px}header,main,footer{max-width:900px;margin:auto;padding:28px}header{display:flex;gap:24px;align-items:center;justify-content:space-between;border-bottom:1px solid #e4ddce}header>a{display:flex;align-items:center;gap:12px;text-decoration:none;font-size:24px;font-weight:750;color:#2d302b}header img{width:44px;height:44px;border-radius:12px}nav{display:flex;gap:20px;flex-wrap:wrap;font-size:14px}main{padding-top:64px;padding-bottom:72px}h1{font-size:clamp(35px,6vw,58px);line-height:1.12;letter-spacing:-.04em;font-weight:650;max-width:750px;margin:0 0 24px}h2{font-size:24px;line-height:1.3;margin-top:42px;letter-spacing:-.02em}.eyebrow{text-transform:uppercase;letter-spacing:.18em;color:#78551b;font-size:12px;font-weight:750}.lead{font-size:23px;line-height:1.5;color:#65675f;max-width:700px}li{padding:5px 0}footer{font-size:13px;border-top:1px solid #e4ddce;color:#65675f}.cards{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin:44px 0}.cards a,.request{border:1px solid #ded6c4;border-radius:20px;background:#fffdf7;padding:24px}.cards a{text-decoration:none}.cards span{display:block;color:#65675f;font-size:15px;margin-top:12px}.request h2{margin-top:0}.button{display:inline-block;background:#78551b;color:white;border-radius:12px;padding:12px 22px;text-decoration:none}.draft{padding:12px;background:#fff0c4;border:1px solid #b18a2f}@media(max-width:640px){header{align-items:flex-start;flex-direction:column;gap:16px}.cards{grid-template-columns:1fr}main{padding-top:36px}body{font-size:16px}}'''
    (output / 'assets').mkdir(exist_ok=True)
    (output / 'assets/style.css').write_text(style)
    logo = ROOT / 'shared/entrypoint/src/commonMain/composeResources/drawable/loom_logo.png'
    shutil.copyfile(logo, output / 'assets/loom-logo.png')
    for slug, (title, body) in pages.items():
        target = output / slug
        target.mkdir(exist_ok=True)
        warning = '<p class="draft">Unpublished draft — publisher details await confirmation.</p>' if allow_draft else ''
        html = f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="description" content="Loom household management: privacy, terms and account deletion."><meta name="loom-policy-version" content="{version}"><link rel="stylesheet" href="{prefix}/assets/style.css"><title>{escape(title)} · Loom</title></head><body><header><a href="{prefix}/"><img src="{prefix}/assets/loom-logo.png" alt="">Loom</a><nav aria-label="Legal documents"><a href="{prefix}/privacy/">Privacy</a><a href="{prefix}/terms/">Terms</a><a href="{prefix}/delete-account/">Delete account</a></nav></header><main>{warning}<p class="eyebrow">Life, woven together</p><h1>{escape(title)}</h1>{body}</main><footer>{publisher} · {contact}<br>Policy version {version}</footer></body></html>'''
        (target / 'index.html').write_text(html)
    (output / '.nojekyll').write_text('')
    (output / 'release.json').write_text(json.dumps({'app':'Loom','package':'com.eyther.lumbridge','version':config['POLICY_VERSION'],'publisher':config['PUBLISHER_NAME'],'draft':allow_draft}))
    print('Built Loom policy pages' + (' (unpublished draft)' if allow_draft else ''))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--draft', action='store_true')
    parser.add_argument('--output', type=Path, default=ROOT/'legal-site/dist')
    arguments = parser.parse_args()
    try:
        build(arguments.output, arguments.draft)
    except ValueError as error:
        raise SystemExit(str(error))
