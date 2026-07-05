# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it responsibly.

**Do not open a public GitHub issue for security vulnerabilities.**

Instead, please contact the maintainer directly through GitHub by using the [Security Advisories](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/security/advisories) feature, or open a private issue if available.

Please include:

- A description of the vulnerability
- Steps to reproduce
- Potential impact
- Any suggested fix (if applicable)

## Response

The maintainer will acknowledge receipt within 72 hours and provide an update on the fix timeline as soon as possible.

## Scope

This security policy applies to the application source code in this repository. It does not cover:

- The BLE protocol itself (which is a vendor-defined protocol)
- The security of the BMS hardware
- Third-party dependencies (report those to their respective maintainers)

## BLE Security Considerations

This application communicates with BMS devices over Bluetooth Low Energy. Users should be aware:

- BLE communication is not encrypted by default in the JBD protocol
- Physical proximity is required for communication (typically within a few meters)
- The application does not transmit data over the internet
- BMS passwords are sent over BLE in plaintext as defined by the protocol specification
- Users are responsible for the physical security of their devices
