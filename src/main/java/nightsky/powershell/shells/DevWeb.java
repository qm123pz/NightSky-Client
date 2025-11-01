package nightsky.powershell.shells;

import nightsky.NightSky;
import nightsky.powershell.PowerShell;
import nightsky.util.ChatUtil;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class DevWeb extends PowerShell {
    private static final String HTML_CONTENT =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>NightSky Devlog</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "            font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;\n" +
                    "        }\n" +
                    "        \n" +
                    "        body {\n" +
                    "            background: radial-gradient(ellipse at center, #0a0a1a 0%, #050515 50%, #000000 100%);\n" +
                    "            color: #e0e0ff;\n" +
                    "            min-height: 100vh;\n" +
                    "            overflow-x: hidden;\n" +
                    "            position: relative;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .particles {\n" +
                    "            position: fixed;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            z-index: -2;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .particle {\n" +
                    "            position: absolute;\n" +
                    "            background: linear-gradient(45deg, #4a76ee, #6a11cb);\n" +
                    "            border-radius: 50%;\n" +
                    "            filter: blur(1px);\n" +
                    "            animation: particleFloat 25s infinite linear;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes particleFloat {\n" +
                    "            0% { transform: translateY(100vh) rotate(0deg) scale(0); opacity: 0; }\n" +
                    "            10% { opacity: 0.8; transform: translateY(80vh) rotate(90deg) scale(1); }\n" +
                    "            90% { opacity: 0.8; }\n" +
                    "            100% { transform: translateY(-100px) rotate(360deg) scale(0); opacity: 0; }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .nebula {\n" +
                    "            position: fixed;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            z-index: -1;\n" +
                    "            opacity: 0.3;\n" +
                    "            background: \n" +
                    "                radial-gradient(circle at 20% 30%, rgba(74, 118, 238, 0.1) 0%, transparent 50%),\n" +
                    "                radial-gradient(circle at 80% 70%, rgba(106, 17, 203, 0.1) 0%, transparent 50%),\n" +
                    "                radial-gradient(circle at 40% 80%, rgba(42, 90, 170, 0.05) 0%, transparent 50%);\n" +
                    "            animation: nebulaShift 15s ease-in-out infinite alternate;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes nebulaShift {\n" +
                    "            0% { transform: scale(1) rotate(0deg); }\n" +
                    "            100% { transform: scale(1.1) rotate(1deg); }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .container {\n" +
                    "            max-width: 1200px;\n" +
                    "            margin: 0 auto;\n" +
                    "            padding: 40px 20px;\n" +
                    "            position: relative;\n" +
                    "            z-index: 1;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .hero {\n" +
                    "            text-align: center;\n" +
                    "            padding: 80px 0 60px;\n" +
                    "            position: relative;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .hero::before {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            top: 50%;\n" +
                    "            left: 50%;\n" +
                    "            transform: translate(-50%, -50%);\n" +
                    "            width: 300px;\n" +
                    "            height: 300px;\n" +
                    "            background: radial-gradient(circle, rgba(74, 118, 238, 0.15) 0%, transparent 70%);\n" +
                    "            border-radius: 50%;\n" +
                    "            animation: heroOrbit 8s linear infinite;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes heroOrbit {\n" +
                    "            0% { transform: translate(-50%, -50%) rotate(0deg) scale(1); }\n" +
                    "            50% { transform: translate(-50%, -50%) rotate(180deg) scale(1.1); }\n" +
                    "            100% { transform: translate(-50%, -50%) rotate(360deg) scale(1); }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .main-title {\n" +
                    "            font-size: 4rem;\n" +
                    "            font-weight: 300;\n" +
                    "            letter-spacing: 4px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            position: relative;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .title-gradient {\n" +
                    "            background: linear-gradient(135deg, #fff 0%, #4a76ee 25%, #6a11cb 50%, #4a76ee 75%, #fff 100%);\n" +
                    "            background-size: 200% 200%;\n" +
                    "            -webkit-background-clip: text;\n" +
                    "            -webkit-text-fill-color: transparent;\n" +
                    "            background-clip: text;\n" +
                    "            animation: titleFlow 4s ease-in-out infinite;\n" +
                    "            text-shadow: 0 0 60px rgba(74, 118, 238, 0.5);\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes titleFlow {\n" +
                    "            0%, 100% { background-position: 0% 50%; }\n" +
                    "            50% { background-position: 100% 50%; }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .subtitle {\n" +
                    "            font-size: 1.2rem;\n" +
                    "            color: #a0a0ff;\n" +
                    "            font-weight: 300;\n" +
                    "            letter-spacing: 2px;\n" +
                    "            animation: subtitleGlow 3s ease-in-out infinite alternate;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes subtitleGlow {\n" +
                    "            0% { text-shadow: 0 0 20px rgba(160, 160, 255, 0.3); }\n" +
                    "            100% { text-shadow: 0 0 30px rgba(160, 160, 255, 0.6); }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .timeline {\n" +
                    "            position: relative;\n" +
                    "            max-width: 800px;\n" +
                    "            margin: 0 auto;\n" +
                    "            padding: 40px 0;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .timeline::before {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            left: 50%;\n" +
                    "            top: 0;\n" +
                    "            bottom: 0;\n" +
                    "            width: 2px;\n" +
                    "            background: linear-gradient(180deg, transparent, #4a76ee, #6a11cb, transparent);\n" +
                    "            transform: translateX(-50%);\n" +
                    "            animation: timelinePulse 3s ease-in-out infinite;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes timelinePulse {\n" +
                    "            0%, 100% { opacity: 0.6; }\n" +
                    "            50% { opacity: 1; }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release {\n" +
                    "            position: relative;\n" +
                    "            margin: 60px 0;\n" +
                    "            width: 45%;\n" +
                    "            opacity: 0;\n" +
                    "            transform: translateY(50px);\n" +
                    "            transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release.visible {\n" +
                    "            opacity: 1;\n" +
                    "            transform: translateY(0);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release:nth-child(odd) {\n" +
                    "            left: 0;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release:nth-child(even) {\n" +
                    "            left: 55%;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-card {\n" +
                    "            background: linear-gradient(135deg, rgba(20, 20, 40, 0.9), rgba(30, 30, 60, 0.7));\n" +
                    "            backdrop-filter: blur(20px);\n" +
                    "            border: 1px solid rgba(74, 118, 238, 0.2);\n" +
                    "            border-radius: 20px;\n" +
                    "            padding: 35px;\n" +
                    "            position: relative;\n" +
                    "            overflow: hidden;\n" +
                    "            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);\n" +
                    "            transition: all 0.4s ease;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-card::before {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            top: -2px;\n" +
                    "            left: -2px;\n" +
                    "            right: -2px;\n" +
                    "            bottom: -2px;\n" +
                    "            background: linear-gradient(45deg, #4a76ee, #6a11cb, #4a76ee);\n" +
                    "            border-radius: 22px;\n" +
                    "            z-index: -1;\n" +
                    "            opacity: 0;\n" +
                    "            transition: opacity 0.4s ease;\n" +
                    "            animation: borderRotate 3s linear infinite;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes borderRotate {\n" +
                    "            0% { transform: rotate(0deg); }\n" +
                    "            100% { transform: rotate(360deg); }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-card:hover::before {\n" +
                    "            opacity: 1;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-card:hover {\n" +
                    "            transform: translateY(-10px) scale(1.02);\n" +
                    "            box-shadow: 0 25px 50px rgba(74, 118, 238, 0.3);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-header {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            padding-bottom: 15px;\n" +
                    "            border-bottom: 1px solid rgba(74, 118, 238, 0.3);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-title {\n" +
                    "            font-size: 1.8rem;\n" +
                    "            font-weight: 500;\n" +
                    "            color: #fff;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .version {\n" +
                    "            background: linear-gradient(135deg, #4a76ee, #6a11cb);\n" +
                    "            color: white;\n" +
                    "            padding: 8px 16px;\n" +
                    "            border-radius: 15px;\n" +
                    "            font-size: 0.9rem;\n" +
                    "            font-weight: 600;\n" +
                    "            box-shadow: 0 5px 15px rgba(74, 118, 238, 0.4);\n" +
                    "            animation: versionGlow 2s ease-in-out infinite alternate;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes versionGlow {\n" +
                    "            0% { box-shadow: 0 5px 15px rgba(74, 118, 238, 0.4); }\n" +
                    "            100% { box-shadow: 0 5px 25px rgba(74, 118, 238, 0.7), 0 0 30px rgba(106, 17, 203, 0.3); }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-content {\n" +
                    "            color: #ccccff;\n" +
                    "            line-height: 1.8;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-item {\n" +
                    "            margin: 12px 0;\n" +
                    "            padding-left: 20px;\n" +
                    "            position: relative;\n" +
                    "            transition: all 0.3s ease;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-item::before {\n" +
                    "            content: '▶';\n" +
                    "            position: absolute;\n" +
                    "            left: 0;\n" +
                    "            color: #4a76ee;\n" +
                    "            font-size: 0.8rem;\n" +
                    "            transition: all 0.3s ease;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-item:hover {\n" +
                    "            transform: translateX(10px);\n" +
                    "            color: #fff;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release-item:hover::before {\n" +
                    "            transform: scale(1.5);\n" +
                    "            color: #6a11cb;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .milestone {\n" +
                    "            background: linear-gradient(135deg, rgba(42, 90, 42, 0.3), rgba(58, 122, 58, 0.2));\n" +
                    "            border: 1px solid rgba(144, 255, 144, 0.3);\n" +
                    "            color: #90ff90;\n" +
                    "            padding: 8px 16px;\n" +
                    "            border-radius: 15px;\n" +
                    "            font-size: 0.8rem;\n" +
                    "            font-weight: 600;\n" +
                    "            display: inline-block;\n" +
                    "            margin-top: 15px;\n" +
                    "            animation: milestonePulse 3s ease-in-out infinite;\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes milestonePulse {\n" +
                    "            0%, 100% { \n" +
                    "                box-shadow: 0 0 20px rgba(144, 255, 144, 0.2);\n" +
                    "                transform: scale(1);\n" +
                    "            }\n" +
                    "            50% { \n" +
                    "                box-shadow: 0 0 30px rgba(144, 255, 144, 0.4);\n" +
                    "                transform: scale(1.05);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        .timeline-node {\n" +
                    "            position: absolute;\n" +
                    "            top: 50%;\n" +
                    "            width: 16px;\n" +
                    "            height: 16px;\n" +
                    "            background: linear-gradient(135deg, #4a76ee, #6a11cb);\n" +
                    "            border-radius: 50%;\n" +
                    "            border: 3px solid #0a0a1a;\n" +
                    "            z-index: 2;\n" +
                    "            animation: nodePulse 2s ease-in-out infinite;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release:nth-child(odd) .timeline-node {\n" +
                    "            right: -8px;\n" +
                    "            transform: translateY(-50%);\n" +
                    "        }\n" +
                    "        \n" +
                    "        .release:nth-child(even) .timeline-node {\n" +
                    "            left: -8px;\n" +
                    "            transform: translateY(-50%);\n" +
                    "        }\n" +
                    "        \n" +
                    "        @keyframes nodePulse {\n" +
                    "            0%, 100% { \n" +
                    "                box-shadow: 0 0 0 0 rgba(74, 118, 238, 0.4);\n" +
                    "                transform: translateY(-50%) scale(1);\n" +
                    "            }\n" +
                    "            50% { \n" +
                    "                box-shadow: 0 0 0 10px rgba(74, 118, 238, 0);\n" +
                    "                transform: translateY(-50%) scale(1.1);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        footer {\n" +
                    "            text-align: center;\n" +
                    "            padding: 60px 20px 40px;\n" +
                    "            margin-top: 80px;\n" +
                    "            color: #8888aa;\n" +
                    "            font-size: 0.9rem;\n" +
                    "            position: relative;\n" +
                    "        }\n" +
                    "        \n" +
                    "        footer::before {\n" +
                    "            content: '';\n" +
                    "            position: absolute;\n" +
                    "            top: 0;\n" +
                    "            left: 50%;\n" +
                    "            transform: translateX(-50%);\n" +
                    "            width: 200px;\n" +
                    "            height: 1px;\n" +
                    "            background: linear-gradient(90deg, transparent, #4a76ee, transparent);\n" +
                    "        }\n" +
                    "        \n" +
                    "        @media (max-width: 768px) {\n" +
                    "            .timeline::before {\n" +
                    "                left: 30px;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .release {\n" +
                    "                width: calc(100% - 60px);\n" +
                    "                left: 30px !important;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .release:nth-child(even) .timeline-node,\n" +
                    "            .release:nth-child(odd) .timeline-node {\n" +
                    "                left: -23px;\n" +
                    "                right: auto;\n" +
                    "            }\n" +
                    "            \n" +
                    "            .main-title {\n" +
                    "                font-size: 2.5rem;\n" +
                    "            }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"nebula\"></div>\n" +
                    "    <div class=\"particles\" id=\"particles\"></div>\n" +
                    "    \n" +
                    "    <div class=\"container\">\n" +
                    "        <div class=\"hero\">\n" +
                    "            <h1 class=\"main-title\">\n" +
                    "                <span class=\"title-gradient\">NIGHTSKY</span>\n" +
                    "            </h1>\n" +
                    "            <div class=\"subtitle\">DEVELOPMENT LOG</div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"timeline\">\n" +
                    "            <div class=\"release\">\n" +
                    "                <div class=\"timeline-node\"></div>\n" +
                    "                <div class=\"release-card\">\n" +
                    "                    <div class=\"release-header\">\n" +
                    "                        <div class=\"release-title\">Release B1.1</div>\n" +
                    "                        <div class=\"version\">v1.1</div>\n" +
                    "                    </div>\n" +
                    "                    <div class=\"release-content\">\n" +
                    "                        <div class=\"release-item\">Added BlockAnimation</div>\n" +
                    "                        <div class=\"release-item\">Added window title and icon</div>\n" +
                    "                        <div class=\"release-item\">Fix some bugs</div>\n" +
                    "                        <div class=\"release-item\">Improved somethings</div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"release\">\n" +
                    "                <div class=\"timeline-node\"></div>\n" +
                    "                <div class=\"release-card\">\n" +
                    "                    <div class=\"release-header\">\n" +
                    "                        <div class=\"release-title\">Release B1</div>\n" +
                    "                        <div class=\"version\">v1.0</div>\n" +
                    "                    </div>\n" +
                    "                    <div class=\"release-content\">\n" +
                    "                        <div class=\"release-item\">First Release</div>\n" +
                    "                        <div class=\"release-item\">这是NightSky 的第一个正式版本！</div>\n" +
                    "                        <div class=\"milestone\">里程碑</div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <footer>\n" +
                    "            <p>© 2025 NightSky Development Team</p>\n" +
                    "        </footer>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        function createParticles() {\n" +
                    "            const container = document.getElementById('particles');\n" +
                    "            const count = 80;\n" +
                    "            \n" +
                    "            for (let i = 0; i < count; i++) {\n" +
                    "                const particle = document.createElement('div');\n" +
                    "                particle.className = 'particle';\n" +
                    "                \n" +
                    "                const size = Math.random() * 3 + 1;\n" +
                    "                particle.style.width = size + 'px';\n" +
                    "                particle.style.height = size + 'px';\n" +
                    "                \n" +
                    "                particle.style.left = Math.random() * 100 + '%';\n" +
                    "                \n" +
                    "                const delay = Math.random() * 25;\n" +
                    "                const duration = 20 + Math.random() * 20;\n" +
                    "                particle.style.animationDelay = delay + 's';\n" +
                    "                particle.style.animationDuration = duration + 's';\n" +
                    "                \n" +
                    "                container.appendChild(particle);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function animateOnScroll() {\n" +
                    "            const releases = document.querySelectorAll('.release');\n" +
                    "            releases.forEach((release, index) => {\n" +
                    "                const rect = release.getBoundingClientRect();\n" +
                    "                const isVisible = rect.top < window.innerHeight * 0.8;\n" +
                    "                \n" +
                    "                if (isVisible) {\n" +
                    "                    setTimeout(() => {\n" +
                    "                        release.classList.add('visible');\n" +
                    "                    }, index * 200);\n" +
                    "                }\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.addEventListener('DOMContentLoaded', () => {\n" +
                    "            createParticles();\n" +
                    "            animateOnScroll();\n" +
                    "        });\n" +
                    "        \n" +
                    "        window.addEventListener('scroll', animateOnScroll);\n" +
                    "        \n" +
                    "        document.addEventListener('mousemove', (e) => {\n" +
                    "            const cards = document.querySelectorAll('.release-card');\n" +
                    "            const mouseX = e.clientX / window.innerWidth;\n" +
                    "            const mouseY = e.clientY / window.innerHeight;\n" +
                    "            \n" +
                    "            cards.forEach(card => {\n" +
                    "                const rect = card.getBoundingClientRect();\n" +
                    "                const cardX = rect.left + rect.width / 2;\n" +
                    "                const cardY = rect.top + rect.height / 2;\n" +
                    "                \n" +
                    "                const distanceX = e.clientX - cardX;\n" +
                    "                const distanceY = e.clientY - cardY;\n" +
                    "                \n" +
                    "                const rotateY = distanceX / 50;\n" +
                    "                const rotateX = -distanceY / 50;\n" +
                    "                \n" +
                    "                card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;\n" +
                    "            });\n" +
                    "        });\n" +
                    "        \n" +
                    "        document.addEventListener('mouseleave', () => {\n" +
                    "            const cards = document.querySelectorAll('.release-card');\n" +
                    "            cards.forEach(card => {\n" +
                    "                card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)';\n" +
                    "            });\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";

    public DevWeb() {
        super(new ArrayList<>(Arrays.asList("ChangeLog")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        if (args.size() > 1 && "help".equalsIgnoreCase(args.get(1))) {
            sendUsage(args);
            return;
        }

        try {
            openHTMLFile();
            ChatUtil.sendFormatted(String.format("%s正在打开NightSky开发日志...", NightSky.clientName));
        } catch (Exception e) {
            ChatUtil.sendFormatted(String.format("%s打开网页时出错: %s", NightSky.clientName, e.getMessage()));
        }
    }

    private void openHTMLFile() {
        try {
            File tempFile = File.createTempFile("nightsky_devlog", ".html");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(HTML_CONTENT.getBytes(StandardCharsets.UTF_8));
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tempFile.toURI());
            } else {
                throw new Exception("无法打开浏览器，桌面操作不被支持");
            }
        } catch (IOException e) {
            throw new RuntimeException("创建HTML文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("打开浏览器失败: " + e.getMessage());
        }
    }

    private void sendUsage(ArrayList<String> args) {
        String baseCommand = args.get(0).toLowerCase();
        ChatUtil.sendFormatted(
                String.format("%sUsage: .%s - 打开NightSky开发日志页面",
                        NightSky.clientName,
                        baseCommand
                )
        );
    }
}
