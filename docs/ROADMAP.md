# Roadmap

This document outlines the planned development direction for E-Rickshaw Battery Unlock. Priorities may shift based on community feedback and testing results.

## Current Focus

- Community testing across different BMS hardware and Android devices
- Bug fixes and stability improvements based on real-world usage
- Documentation improvements

## Planned Features

### Short-term

- [ ] Additional BMS protocol support (DALY BMS)
- [ ] Improved error messages with protocol-specific diagnostics
- [ ] Connection stability improvements for specific Android devices
- [ ] Unit and integration test coverage for BLE protocol code

### Medium-term

- [ ] Additional BMS protocol support (ANT BMS, PACE BMS)
- [ ] Cell voltage balancing visualization
- [ ] Battery session history and logging
- [ ] Export diagnostics data (CSV/JSON)
- [ ] Multi-language support (Hindi, Bengali, Tamil, etc.)

### Long-term

- [ ] Additional BMS protocol support (JKBMS, other vendors)
- [ ] Battery health analysis and trending
- [ ] Widget for quick battery status
- [ ] Notification for connection loss or battery alerts
- [ ] Support for multiple simultaneous battery profiles
- [ ] Desktop companion app (for broader platform support)

## Protocol Expansion

The highest priority for protocol expansion is determined by:

1. Community demand -- which BMS vendors are most commonly used in e-rickshaws
2. Hardware availability -- protocols that can be tested against real devices
3. Documentation quality -- protocols with publicly available specifications

To request support for a specific BMS protocol, please [open a feature request](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=feature_request.md) with details about the BMS manufacturer and any protocol documentation you can share.

## Contributing to the Roadmap

This roadmap is a living document. If you would like to influence priorities:

- Test the application on your hardware and report results
- Contribute code for new features or protocol implementations
- Open issues describing use cases that are not yet covered
- Improve documentation to help other users
