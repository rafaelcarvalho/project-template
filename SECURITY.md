# Security Policy

## Reporting a Vulnerability

This project's maintainers take security seriously. We appreciate your efforts to responsibly disclose your findings.

### How to Report

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report security vulnerabilities by emailing:

**security@example.com**

Include as much of the following information as possible:

- Type of vulnerability (e.g., injection, XSS, authentication bypass)
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the vulnerability, including how an attacker might exploit it

### What to Expect

1. **Acknowledgment** — We'll acknowledge receipt of your vulnerability report within 48 hours
2. **Investigation** — Our team will investigate and validate the report
3. **Updates** — We'll keep you informed about our progress
4. **Resolution** — Once fixed, we'll notify you and coordinate public disclosure
5. **Credit** — With your permission, we'll credit you in our security advisories

### Supported Versions

We provide security updates for:

| Version | Supported          |
| ------- | ------------------ |
| Latest  | :white_check_mark: |
| < 1.0   | :x:                |

### Security Best Practices

When deploying this project:

- Always use the latest stable version
- Enable HTTPS/TLS for all external communications
- Follow the principle of least privilege for service accounts
- Regularly update dependencies (we automate this with Dependabot)
- Monitor security advisories from Spring Boot and Kotlin ecosystems
- Run security scans in your CI/CD pipeline

### Disclosure Policy

We follow **coordinated disclosure**:

1. Security issues are fixed privately
2. Patches are released as soon as possible
3. Public disclosure occurs after mitigation is available
4. CVE identifiers are requested for significant vulnerabilities

### Security Features

This project includes:

- **Automated dependency scanning** via Dependabot
- **SARIF reports** uploaded to GitHub Security
- **Architecture validation** to prevent insecure patterns
- **Reactive programming** to avoid blocking vulnerabilities
- **Minimal attack surface** (no unnecessary dependencies)

### Recognition

We maintain a list of security researchers who have helped improve this project:

- _(No vulnerabilities reported yet)_

Thank you for helping keep this project and its users safe! 🛡️
