# 🚀 GostDroid

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.0.7-green.svg?style=for-the-badge)
![API](https://img.shields.io/badge/API-21%2B-orange.svg?style=for-the-badge)

_A powerful Android VPN client that routes traffic through SOCKS proxies with advanced Gost tunnel support_

</div>

## ✨ Features

### 🔐 **Core Functionality**

- **SOCKS5 Proxy Support** - Route all your traffic through SOCKS5 proxies
- **VPN Interface** - Clean, intuitive VPN-like user interface
- **Multiple Profiles** - Create and manage multiple proxy configurations
- **Auto-Connect** - Automatically connect on device boot

### 🌐 **Advanced Networking**

- **IPv6 Support** - Access IPv6 content from IPv4 networks
- **UDP Forwarding** - Forward UDP packets via badvpn-udpgw
- **Custom DNS** - Configure custom DNS servers with TCP support
- **Smart Routing** - Route all traffic or bypass Chinese IPs

### 🚇 **Gost Tunnel Integration**

- **WebSocket Transport** - Advanced transport protocols via Gost
- **Flexible Configuration** - Support for various Gost transport methods
- **Custom Server Settings** - Separate Gost server configuration

### 📱 **Per-App Control**

- **Selective Proxying** - Choose which apps use the proxy
- **Bypass Mode** - Exclude specific apps from proxying
- **Package Management** - Easy app selection interface

### 🔒 **Security & Authentication**

- **Username/Password Auth** - Secure SOCKS5 authentication
- **Foreground Service** - Persistent connection protection
- **Boot Receiver** - Automatic startup capability

## 📱 Screenshots

_Coming soon - Add your app screenshots here_

## 🚀 Quick Start

### Prerequisites

- Android 5.0+ (API level 21)
- VPN permission (automatically requested)

### Installation

1. **Download the latest APK** from the [Releases](../../releases) page
2. **Enable Unknown Sources** in your Android settings
3. **Install the APK** and grant VPN permissions
4. **Configure your proxy** settings
5. **Connect** and enjoy secure browsing!

### Basic Configuration

1. **Open GostDroid**
2. **Tap Settings** to configure your proxy
3. **Enter Server Details**:
   - Server IP address
   - Server port
   - Username/password (if required)
4. **Choose Advanced Options** (optional):
   - Enable IPv6 forwarding
   - Configure UDP forwarding
   - Set up custom DNS
5. **Tap Connect** to start the VPN

## ⚙️ Configuration Guide

### SOCKS5 Proxy Setup

```
Server IP: your.proxy.server.com
Port: 1080
Username: your_username (optional)
Password: your_password (optional)
```

### Gost Tunnel Configuration

For advanced users who want to use Gost tunneling:

1. Enable "Use Gost Tunnel" in settings
2. Select your preferred transport protocol
3. Optionally specify a different Gost server
4. Configure authentication if required

### UDP Forwarding

To enable UDP forwarding (for DNS, gaming, etc.):

1. Enable "UDP Forwarding" in advanced settings
2. Ensure badvpn-udpgw is running on your server:
   ```bash
   badvpn-udpgw --listen-addr 127.0.0.1:7300
   ```
3. Set UDP Gateway to `127.0.0.1:7300`

## 🏗️ Building from Source

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK with API level 34

### Build Steps

1. **Clone the repository**:

   ```bash
   git clone https://github.com/tje3d/GostDroid.git
   cd GostDroid
   ```

2. **Open in Android Studio** or build via command line:

   ```bash
   ./gradlew assembleDebug
   ```

3. **Install the APK**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow Android development best practices
- Test on multiple Android versions
- Update documentation for new features
- Ensure backward compatibility

## 📋 Roadmap

- [ ] **Material Design 3** UI update
- [ ] **WireGuard** protocol support
- [ ] **Traffic Statistics** and monitoring
- [ ] **Dark Mode** theme
- [ ] **Export/Import** configuration
- [ ] **Multiple Language** support
- [ ] **Widget** for quick connect/disconnect

## 🐛 Troubleshooting

### Common Issues

**Connection fails immediately**

- Check server address and port
- Verify authentication credentials
- Ensure server is accessible

**UDP apps don't work**

- Enable UDP forwarding in settings
- Verify badvpn-udpgw is running on server
- Check UDP gateway configuration

**Apps bypass the proxy**

- Check per-app proxy settings
- Verify bypass mode configuration
- Restart the VPN connection

### Getting Help

- 📖 Check the [Wiki](../../wiki) for detailed guides
- 🐛 Report bugs in [Issues](../../issues)
- 💬 Join discussions in [Discussions](../../discussions)

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **BadVPN** - For the excellent tun2socks implementation
- **Gost** - For the advanced tunneling capabilities
- **Android VPN API** - For making VPN apps possible
- **Open Source Community** - For continuous inspiration and support

## 📞 Support

If you find this project helpful, please consider:

- ⭐ **Starring** the repository
- 🐛 **Reporting** bugs and issues
- 💡 **Suggesting** new features
- 🤝 **Contributing** to the codebase

---

<div align="center">

**Made with ❤️ for the Android community**

_Secure browsing should be accessible to everyone_

</div>
