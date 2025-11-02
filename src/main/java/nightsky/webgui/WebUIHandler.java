package nightsky.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class WebUIHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            WebServer.sendHTMLResponse(exchange, 200, getHTML());
        } else if (path.equals("/style.css")) {
            WebServer.sendCSSResponse(exchange, 200, getCSS());
        } else if (path.equals("/script.js")) {
            WebServer.sendJSResponse(exchange, 200, getJS());
        } else {
            WebServer.sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
        }
    }

    private String getHTML() {
        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"><title>NightSky WebGUI</title><link rel=\"stylesheet\" href=\"/style.css\"></head><body><div class=\"particles\"></div><div class=\"grid-bg\"></div><div class=\"theme-switcher\"><button class=\"theme-btn\" data-theme=\"cyber\" title=\"赛博朋克\"><svg viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z\"/></svg></button><button class=\"theme-btn\" data-theme=\"ocean\" title=\"深海\"><svg viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z\"/></svg></button><button class=\"theme-btn\" data-theme=\"sunset\" title=\"日落\"><svg viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1z\"/></svg></button><button class=\"theme-btn\" data-theme=\"forest\" title=\"森林\"><svg viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z\"/></svg></button></div><div class=\"sidebar\"><div class=\"brand\"><div class=\"logo\">NIGHTSKY</div><div class=\"version\">v1.2</div></div><div class=\"nav\"><button class=\"nav-btn active\" data-tab=\"modules\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><rect x=\"3\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"14\" width=\"7\" height=\"7\"/><rect x=\"3\" y=\"14\" width=\"7\" height=\"7\"/></svg><span>模块管理</span></button><button class=\"nav-btn\" data-tab=\"configs\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><polyline points=\"14 2 14 8 20 8\"/></svg><span>配置文件</span></button></div><div class=\"categories\" id=\"categories\"><div class=\"cat-title\">分类筛选</div><button class=\"cat-btn active\" data-cat=\"all\"><span class=\"cat-icon\">◆</span>全部模块</button><button class=\"cat-btn\" data-cat=\"COMBAT\"><span class=\"cat-icon\">⚔</span>战斗</button><button class=\"cat-btn\" data-cat=\"MOVEMENT\"><span class=\"cat-icon\">➤</span>移动</button><button class=\"cat-btn\" data-cat=\"PLAYER\"><svg class=\"cat-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2\"/><circle cx=\"12\" cy=\"7\" r=\"4\"/></svg>玩家</button><button class=\"cat-btn\" data-cat=\"RENDER\"><span class=\"cat-icon\">◉</span>渲染</button><button class=\"cat-btn\" data-cat=\"MISC\"><span class=\"cat-icon\">⚙</span>杂项</button></div><div class=\"sidebar-footer\"><div class=\"status-indicator\"><span class=\"status-dot\"></span><span>已连接</span></div></div></div><div class=\"main\"><div class=\"header\"><div class=\"search-container\"><svg class=\"search-icon\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><circle cx=\"11\" cy=\"11\" r=\"8\"/><path d=\"m21 21-4.35-4.35\"/></svg><input type=\"text\" id=\"search\" placeholder=\"搜索模块名称...\"></div></div><div id=\"modulesTab\" class=\"tab-content active\"><div id=\"modules\" class=\"modules-grid\"></div></div><div id=\"configsTab\" class=\"tab-content\"><div class=\"config-header\"><h2>配置管理</h2><div class=\"config-actions\"><input type=\"text\" id=\"configName\" placeholder=\"输入配置名称...\"><button onclick=\"saveConfig()\" class=\"btn btn-primary\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z\"/><polyline points=\"17 21 17 13 7 13 7 21\"/><polyline points=\"7 3 7 8 15 8\"/></svg>保存配置</button><button onclick=\"openFolder()\" class=\"btn btn-secondary\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z\"/></svg>打开文件夹</button></div></div><div id=\"configs\" class=\"config-list\"></div></div></div><script src=\"/script.js\"></script></body></html>";
    }

    private String getCSS() {
        StringBuilder css = new StringBuilder();
        css.append(":root{--bg:#0a0e27;--bg-card:#151932;--bg-hover:#1a1f3a;--border:#252b4a;--text:#e4e8ff;--text-dim:#8b92b8;--primary:#667eea;--primary-dark:#5568d3;--accent:#f093fb;--glow:rgba(102,126,234,0.3);--shadow:rgba(0,0,0,0.4)}");
        css.append("body.theme-ocean{--primary:#00d4ff;--accent:#0099ff;--glow:rgba(0,212,255,0.3)}");
        css.append("body.theme-sunset{--primary:#ff6b6b;--accent:#feca57;--glow:rgba(255,107,107,0.3)}");
        css.append("body.theme-forest{--primary:#00ff88;--accent:#00cc66;--glow:rgba(0,255,136,0.3)}");
        css.append("*{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',system-ui,sans-serif}");
        css.append("body{display:flex;height:100vh;background:var(--bg);color:var(--text);overflow:hidden;position:relative}");
        css.append("body::before{content:'';position:fixed;top:0;left:0;right:0;bottom:0;background:radial-gradient(circle at 20% 50%,var(--glow) 0%,transparent 50%),radial-gradient(circle at 80% 80%,var(--glow) 0%,transparent 50%);pointer-events:none;z-index:0;transition:all 0.5s ease}");
        css.append(".cursor-glow{position:fixed;width:400px;height:400px;background:radial-gradient(circle,var(--glow) 0%,transparent 70%);pointer-events:none;z-index:9999;transform:translate(-50%,-50%);transition:opacity 0.3s;opacity:0}");
        css.append(".theme-switcher{position:fixed;top:20px;right:20px;display:flex;gap:8px;z-index:1000;background:var(--bg-card);padding:8px;border-radius:12px;border:1px solid var(--border);backdrop-filter:blur(10px)}");
        css.append(".theme-btn{padding:8px 16px;background:transparent;border:1px solid var(--border);border-radius:8px;color:var(--text-dim);cursor:pointer;font-size:12px;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden}");
        css.append(".theme-btn::before{content:'';position:absolute;top:50%;left:50%;width:0;height:0;border-radius:50%;background:var(--primary);opacity:0.3;transform:translate(-50%,-50%);transition:width 0.6s,height 0.6s}");
        css.append(".theme-btn:hover::before{width:200px;height:200px}");
        css.append(".theme-btn:hover{border-color:var(--primary);color:var(--primary);transform:translateY(-2px)}");
        css.append(".sidebar{width:240px;background:var(--bg-card);border-right:1px solid var(--border);display:flex;flex-direction:column;padding:24px;position:relative;z-index:10;box-shadow:4px 0 20px var(--shadow);backdrop-filter:blur(10px);transform-style:preserve-3d}");
        css.append(".logo{font-size:28px;font-weight:700;background:linear-gradient(135deg,var(--primary),var(--accent));-webkit-background-clip:text;background-clip:text;color:transparent;margin-bottom:6px;letter-spacing:2px}");
        css.append(".subtitle{font-size:11px;color:var(--text-dim);margin-bottom:32px;letter-spacing:0.5px}");
        css.append(".nav{display:flex;flex-direction:column;gap:10px;margin-bottom:24px}");
        css.append(".nav-btn{padding:12px 16px;background:transparent;border:none;border-radius:10px;color:var(--text-dim);cursor:pointer;text-align:left;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);font-size:14px;position:relative;overflow:hidden}");
        css.append(".nav-btn::before{content:'';position:absolute;left:0;top:0;bottom:0;width:3px;background:var(--primary);transform:scaleY(0);transition:transform 0.3s cubic-bezier(0.4,0,0.2,1)}");
        css.append(".nav-btn:hover{background:var(--bg-hover);color:var(--text);transform:translateX(4px)}");
        css.append(".nav-btn.active{background:linear-gradient(135deg,var(--primary),var(--accent));color:#fff;font-weight:600;box-shadow:0 4px 12px var(--glow)}");
        css.append(".nav-btn.active::before{transform:scaleY(1)}");
        css.append(".categories{display:flex;flex-direction:column;gap:6px}");
        css.append(".cat-btn{padding:10px 14px;background:transparent;border:none;color:var(--text-dim);cursor:pointer;text-align:left;border-radius:8px;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);font-size:13px;position:relative}");
        css.append(".cat-btn::after{content:'';position:absolute;right:12px;top:50%;transform:translateY(-50%) scale(0);width:6px;height:6px;background:var(--primary);border-radius:50%;transition:transform 0.3s cubic-bezier(0.68,-0.55,0.265,1.55)}");
        css.append(".cat-btn:hover{background:var(--bg-hover);color:var(--text);transform:translateX(4px)}");
        css.append(".cat-btn.active{color:var(--primary);font-weight:600;background:var(--bg-hover)}");
        css.append(".cat-btn.active::after{transform:translateY(-50%) scale(1)}");
        css.append(".main{flex:1;overflow-y:auto;padding:24px;position:relative;z-index:1}");
        css.append(".main::-webkit-scrollbar{width:8px}");
        css.append(".main::-webkit-scrollbar-track{background:transparent}");
        css.append(".main::-webkit-scrollbar-thumb{background:var(--border);border-radius:4px;transition:background 0.3s}");
        css.append(".main::-webkit-scrollbar-thumb:hover{background:var(--primary)}");
        css.append(".search-bar{margin-bottom:24px;position:relative}");
        css.append("#search{width:100%;padding:14px 20px 14px 48px;background:var(--bg-card);border:2px solid var(--border);border-radius:12px;color:var(--text);font-size:14px;outline:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:0 2px 8px var(--shadow)}");
        css.append("#search:focus{border-color:var(--primary);box-shadow:0 4px 16px var(--glow),0 0 0 4px rgba(102,126,234,0.1);transform:translateY(-2px)}");
        css.append(".search-bar::before{content:'🔍';position:absolute;left:18px;top:50%;transform:translateY(-50%);font-size:16px;opacity:0.5}");
        css.append(".tab-content{display:none;animation:fadeIn 0.4s ease}");
        css.append(".tab-content.active{display:block}");
        css.append("@keyframes fadeIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}");
        css.append(".modules-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(320px,1fr));gap:24px;perspective:1000px}");
        css.append(".module{background:var(--bg-card);border:1px solid var(--border);border-radius:16px;padding:20px;transition:all 0.4s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden;box-shadow:0 8px 16px var(--shadow);transform-style:preserve-3d;backdrop-filter:blur(10px)}");
        css.append(".module::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:linear-gradient(90deg,var(--primary),var(--accent));transform:scaleX(0);transform-origin:left;transition:transform 0.4s cubic-bezier(0.4,0,0.2,1)}");
        css.append(".module::after{content:'';position:absolute;inset:0;background:radial-gradient(circle at var(--mouse-x,50%) var(--mouse-y,50%),var(--glow) 0%,transparent 50%);opacity:0;transition:opacity 0.3s;pointer-events:none}");
        css.append(".module:hover{border-color:var(--primary);transform:translateY(-8px) rotateX(2deg) scale(1.02);box-shadow:0 20px 40px var(--glow),0 0 0 1px var(--primary),inset 0 1px 0 rgba(255,255,255,0.1)}");
        css.append(".module:hover::before{transform:scaleX(1)}");
        css.append(".module:hover::after{opacity:0.3}");
        css.append(".module-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}");
        css.append(".module-name{font-size:16px;font-weight:600;color:var(--text)}");
        css.append(".module-cat{font-size:11px;color:var(--text-dim);margin-top:4px;text-transform:uppercase;letter-spacing:1px}");
        css.append(".toggle{width:52px;height:28px;background:var(--border);border-radius:14px;position:relative;cursor:pointer;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);box-shadow:inset 0 2px 4px rgba(0,0,0,0.2);user-select:none}");
        css.append(".toggle:hover{transform:scale(1.05);box-shadow:inset 0 2px 4px rgba(0,0,0,0.2),0 0 8px var(--glow)}");
        css.append(".toggle:active{transform:scale(0.95)}");
        css.append(".toggle::after{content:'';position:absolute;width:22px;height:22px;background:#fff;border-radius:50%;top:3px;left:3px;transition:all 0.3s cubic-bezier(0.68,-0.55,0.265,1.55);box-shadow:0 2px 4px rgba(0,0,0,0.2)}");
        css.append(".toggle.on{background:linear-gradient(135deg,var(--primary),var(--accent));box-shadow:0 0 12px var(--glow),inset 0 2px 4px rgba(0,0,0,0.2)}");
        css.append(".toggle.on::after{left:27px;box-shadow:0 2px 8px rgba(0,0,0,0.3)}");
        css.append(".toggle.on:hover{box-shadow:0 0 16px var(--glow),inset 0 2px 4px rgba(0,0,0,0.2)}");
        css.append(".values{display:flex;flex-direction:column;gap:14px;max-height:280px;overflow-y:auto;padding-right:4px}");
        css.append(".values::-webkit-scrollbar{width:4px}");
        css.append(".values::-webkit-scrollbar-track{background:transparent}");
        css.append(".values::-webkit-scrollbar-thumb{background:var(--border);border-radius:2px}");
        css.append(".value{display:flex;flex-direction:column;gap:8px}");
        css.append(".value-label{font-size:13px;color:var(--text-dim);font-weight:500;display:flex;justify-content:space-between;align-items:center}");
        css.append(".value-input,.value-select{padding:10px 14px;background:var(--bg-hover);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:13px;outline:none;transition:all 0.3s cubic-bezier(0.4,0,0.2,1)}");
        css.append(".value-input:focus,.value-select:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(102,126,234,0.1);background:var(--bg-card)}");
        css.append(".value-range{width:100%;height:6px;background:var(--bg-hover);border-radius:3px;outline:none;-webkit-appearance:none;position:relative;cursor:pointer}");
        css.append(".value-range::-webkit-slider-track{height:6px;background:linear-gradient(to right,var(--border) 0%,var(--primary) 100%);border-radius:3px}");
        css.append(".value-range::-webkit-slider-thumb{-webkit-appearance:none;width:18px;height:18px;background:linear-gradient(135deg,var(--primary),var(--accent));border-radius:50%;cursor:pointer;box-shadow:0 2px 8px var(--glow);transition:all 0.2s cubic-bezier(0.4,0,0.2,1);border:2px solid #fff}");
        css.append(".value-range::-webkit-slider-thumb:hover{transform:scale(1.2);box-shadow:0 4px 12px var(--glow)}");
        css.append(".value-range::-webkit-slider-thumb:active{transform:scale(1.1)}");
        css.append(".value-checkbox{width:20px;height:20px;cursor:pointer;accent-color:var(--primary);border-radius:4px}");
        css.append(".config-actions{display:flex;gap:12px;margin-bottom:24px;flex-wrap:wrap}");
        css.append("#configName{flex:1;min-width:200px;padding:12px 16px;background:var(--bg-card);border:2px solid var(--border);border-radius:10px;color:var(--text);outline:none;transition:all 0.3s}");
        css.append("#configName:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(102,126,234,0.1)}");
        css.append(".btn-primary,.btn-secondary{padding:12px 24px;border:none;border-radius:10px;color:#fff;cursor:pointer;font-weight:600;font-size:14px;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden}");
        css.append(".btn-primary{background:linear-gradient(135deg,var(--primary),var(--accent));box-shadow:0 4px 12px var(--glow)}");
        css.append(".btn-primary:hover{transform:translateY(-2px);box-shadow:0 6px 16px var(--glow)}");
        css.append(".btn-primary:active{transform:translateY(0)}");
        css.append(".btn-secondary{background:var(--bg-hover);border:1px solid var(--border);color:var(--text)}");
        css.append(".btn-secondary:hover{border-color:var(--primary);color:var(--primary)}");
        css.append(".config-list{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px}");
        css.append(".config-item{background:var(--bg-card);border:1px solid var(--border);border-radius:16px;padding:20px;display:flex;justify-content:space-between;align-items:center;transition:all 0.4s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden;box-shadow:0 4px 12px var(--shadow)}");
        css.append(".config-item::before{content:'';position:absolute;top:0;left:0;right:0;height:2px;background:linear-gradient(90deg,var(--primary),var(--accent));transform:scaleX(0);transform-origin:left;transition:transform 0.4s}");
        css.append(".config-item:hover{border-color:var(--primary);transform:translateY(-4px) scale(1.02);box-shadow:0 12px 24px var(--glow)}");
        css.append(".config-item:hover::before{transform:scaleX(1)}");
        css.append(".config-name{font-weight:700;font-size:16px;color:var(--text);display:flex;align-items:center;gap:10px}");
        css.append(".config-name::before{content:'📄';font-size:20px}");
        css.append(".config-info{font-size:11px;color:var(--text-dim);margin-top:4px}");
        css.append(".config-btns{display:flex;gap:10px}");
        css.append(".config-btns button{padding:10px 18px;border:none;border-radius:10px;cursor:pointer;font-size:13px;font-weight:600;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden;display:flex;align-items:center;gap:6px;user-select:none}");
        css.append(".config-btns button::before{content:'';position:absolute;inset:0;opacity:0;transition:opacity 0.3s}");
        css.append(".config-btns button::after{content:'';position:absolute;width:100%;height:100%;top:50%;left:50%;transform:translate(-50%,-50%) scale(0);border-radius:50%;background:rgba(255,255,255,0.5);opacity:0;transition:all 0.6s}");
        css.append(".config-btns button:hover{transform:translateY(-2px) scale(1.05);cursor:pointer}");
        css.append(".config-btns button:active{transform:translateY(0) scale(0.98)}");
        css.append(".config-btns button:active::after{transform:translate(-50%,-50%) scale(2);opacity:0.3;transition:all 0s}");
        css.append(".config-btns .btn-load{background:linear-gradient(135deg,var(--primary),var(--accent));color:#fff;box-shadow:0 4px 12px var(--glow)}");
        css.append(".config-btns .btn-load:hover{box-shadow:0 6px 16px var(--glow)}");
        css.append(".config-btns .btn-load::after{content:'▶';margin-left:2px;font-size:10px}");
        css.append(".config-btns .btn-delete{background:rgba(255,107,107,0.1);color:#ff6b6b;border:1px solid rgba(255,107,107,0.3)}");
        css.append(".config-btns .btn-delete:hover{background:#ff6b6b;color:#fff;border-color:#ff6b6b;box-shadow:0 4px 12px rgba(255,107,107,0.4)}");
        css.append(".config-btns .btn-delete::after{content:'✕';margin-left:2px;font-size:12px}");
        css.append(".particles{position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:0;background:radial-gradient(circle at 20% 30%,var(--glow) 0%,transparent 40%),radial-gradient(circle at 80% 70%,var(--glow) 0%,transparent 40%);opacity:0.6;animation:particleFloat 20s ease-in-out infinite}");
        css.append("@keyframes particleFloat{0%,100%{transform:translate(0,0)}50%{transform:translate(20px,-20px)}}");
        css.append(".grid-bg{position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:0;background-image:linear-gradient(var(--border) 1px,transparent 1px),linear-gradient(90deg,var(--border) 1px,transparent 1px);background-size:50px 50px;opacity:0.3;perspective:1000px;transform:rotateX(60deg) translateZ(-500px);transform-origin:center top;animation:gridMove 20s linear infinite}");
        css.append("@keyframes gridMove{0%{background-position:0 0}100%{background-position:0 50px}}");
        css.append(".brand{display:flex;align-items:baseline;gap:8px;margin-bottom:32px}");
        css.append(".logo{font-size:32px;font-weight:800;background:linear-gradient(135deg,var(--primary) 0%,var(--accent) 100%);-webkit-background-clip:text;background-clip:text;color:transparent;letter-spacing:1px;text-shadow:0 0 30px var(--glow)}");
        css.append(".version{font-size:10px;color:var(--text-dim);background:var(--bg-hover);padding:2px 8px;border-radius:4px;font-weight:600}");
        css.append(".nav{display:flex;flex-direction:column;gap:8px;margin-bottom:32px}");
        css.append(".nav-btn{display:flex;align-items:center;gap:12px;padding:12px 16px;background:transparent;border:none;border-radius:10px;color:var(--text-dim);cursor:pointer;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);font-size:14px;font-weight:500}");
        css.append(".nav-btn svg{width:20px;height:20px;opacity:0.6;transition:all 0.3s}");
        css.append(".nav-btn:hover{background:var(--bg-hover);color:var(--text);transform:translateX(4px)}");
        css.append(".nav-btn:hover svg{opacity:1;transform:scale(1.1)}");
        css.append(".nav-btn.active{background:linear-gradient(135deg,var(--primary),var(--accent));color:#fff;box-shadow:0 4px 12px var(--glow)}");
        css.append(".nav-btn.active svg{opacity:1}");
        css.append(".cat-title{font-size:11px;color:var(--text-dim);text-transform:uppercase;letter-spacing:1px;margin-bottom:12px;font-weight:600}");
        css.append(".cat-icon{display:inline-block;width:20px;text-align:center;margin-right:8px;font-size:14px}");
        css.append(".cat-btn svg.cat-icon{width:18px;height:18px;margin-right:8px;stroke-width:2.5}");
        css.append(".sidebar-footer{margin-top:auto;padding-top:20px;border-top:1px solid var(--border)}");
        css.append(".status-indicator{display:flex;align-items:center;gap:8px;font-size:12px;color:var(--text-dim)}");
        css.append(".status-dot{width:8px;height:8px;background:var(--primary);border-radius:50%;animation:pulse 2s ease-in-out infinite;box-shadow:0 0 8px var(--primary)}");
        css.append("@keyframes pulse{0%,100%{opacity:1}50%{opacity:0.5}}");
        css.append(".header{margin-bottom:24px}");
        css.append(".search-container{position:relative;max-width:600px}");
        css.append(".search-icon{position:absolute;left:16px;top:50%;transform:translateY(-50%);width:20px;height:20px;color:var(--text-dim);pointer-events:none}");
        css.append(".theme-btn{width:40px;height:40px;padding:0;display:flex;align-items:center;justify-content:center;background:var(--bg-card);border:1px solid var(--border);border-radius:10px;cursor:pointer;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden}");
        css.append(".theme-btn svg{width:20px;height:20px;color:var(--text-dim);transition:all 0.3s;position:relative;z-index:1}");
        css.append(".theme-btn::before{content:'';position:absolute;inset:0;background:linear-gradient(135deg,var(--primary),var(--accent));opacity:0;transition:opacity 0.3s}");
        css.append(".theme-btn:hover{border-color:var(--primary);transform:translateY(-2px);box-shadow:0 4px 12px var(--glow)}");
        css.append(".theme-btn:hover::before{opacity:0.1}");
        css.append(".theme-btn:hover svg{color:var(--primary);transform:scale(1.1)}");
        css.append(".config-header{margin-bottom:32px}");
        css.append(".config-header h2{font-size:24px;font-weight:700;color:var(--text);margin-bottom:16px;background:linear-gradient(135deg,var(--text),var(--text-dim));-webkit-background-clip:text;background-clip:text}");
        css.append(".btn{display:inline-flex;align-items:center;gap:8px;padding:12px 20px;border:none;border-radius:10px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden}");
        css.append(".btn svg{width:18px;height:18px}");
        css.append(".btn-primary{background:linear-gradient(135deg,var(--primary),var(--accent));color:#fff;box-shadow:0 4px 12px var(--glow)}");
        css.append(".btn-primary:hover{transform:translateY(-2px);box-shadow:0 6px 16px var(--glow)}");
        css.append(".btn-secondary{background:var(--bg-card);color:var(--text);border:1px solid var(--border)}");
        css.append(".btn-secondary:hover{border-color:var(--primary);color:var(--primary);transform:translateY(-2px)}");
        css.append(".value-item{display:flex;align-items:center;gap:12px;padding:12px;background:var(--bg-hover);border-radius:8px;transition:all 0.3s}");
        css.append(".value-item:hover{background:var(--border)}");
        css.append(".value-item label{flex:1;font-size:13px;color:var(--text);font-weight:500}");
        css.append(".value-item input[type=color]{width:48px;height:32px;border:2px solid var(--border);border-radius:6px;cursor:pointer;transition:all 0.3s}");
        css.append(".value-item input[type=color]:hover{border-color:var(--primary);transform:scale(1.05)}");
        css.append(".value-item input[type=text]{padding:8px 12px;background:var(--bg-card);border:1px solid var(--border);border-radius:6px;color:var(--text);font-size:12px;font-family:monospace;transition:all 0.3s}");
        css.append(".value-item input[type=text]:focus{outline:none;border-color:var(--primary);box-shadow:0 0 0 3px rgba(102,126,234,0.1)}");
        css.append(".btn-load{background:var(--primary);color:#fff}");
        css.append(".btn-load:hover{background:var(--primary-dark)}");
        css.append(".btn-delete{background:transparent;color:#ff6b6b;border:1px solid #ff6b6b}");
        css.append(".btn-delete:hover{background:#ff6b6b;color:#fff}");
        css.append(".mode-selector{display:flex;flex-direction:column;gap:10px;padding:12px;background:var(--bg-card);border-radius:10px;border:1px solid var(--border)}");
        css.append(".mode-selector label{font-size:13px;color:var(--text);font-weight:600}");
        css.append(".mode-buttons{display:flex;flex-wrap:wrap;gap:6px}");
        css.append(".mode-btn{padding:8px 16px;background:var(--bg-hover);border:1px solid var(--border);border-radius:8px;color:var(--text-dim);cursor:pointer;font-size:12px;font-weight:500;transition:all 0.3s cubic-bezier(0.4,0,0.2,1);position:relative;overflow:hidden;user-select:none}");
        css.append(".mode-btn::before{content:'';position:absolute;inset:0;background:linear-gradient(135deg,var(--primary),var(--accent));opacity:0;transition:opacity 0.3s}");
        css.append(".mode-btn:hover{border-color:var(--primary);color:var(--text);transform:translateY(-2px) scale(1.05);box-shadow:0 4px 8px var(--glow)}");
        css.append(".mode-btn:hover::before{opacity:0.1}");
        css.append(".mode-btn:active{transform:translateY(0) scale(1)}");
        css.append(".mode-btn.active{background:transparent;color:var(--text);border-color:var(--primary);box-shadow:0 4px 12px var(--glow);font-weight:600}");
        css.append(".mode-btn.active::before{opacity:1}");
        css.append(".color-picker-container{display:flex;align-items:center;gap:12px;padding:12px;background:var(--bg-card);border-radius:10px;border:1px solid var(--border)}");
        css.append(".color-preview{width:48px;height:48px;border-radius:8px;border:2px solid var(--border);cursor:pointer;transition:all 0.3s;position:relative;overflow:hidden;box-shadow:0 2px 8px var(--shadow)}");
        css.append(".color-preview:hover{border-color:var(--primary);transform:scale(1.1) rotate(5deg);box-shadow:0 4px 12px var(--glow)}");
        css.append(".color-preview input[type=color]{position:absolute;inset:0;opacity:0;cursor:pointer}");
        css.append(".color-hex-input{flex:1;padding:10px 14px;background:var(--bg-hover);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:13px;font-family:monospace;font-weight:600;text-transform:uppercase;transition:all 0.3s}");
        css.append(".color-hex-input:focus{outline:none;border-color:var(--primary);box-shadow:0 0 0 3px rgba(102,126,234,0.1);background:var(--bg-card)}");
        return css.toString();
    }

    private String getJS() {
        return "let modules=[],currentCat='all',searchQuery='',isInit=true;const throttle=(fn,delay)=>{let last=0;return(...args)=>{const now=Date.now();if(now-last>=delay){last=now;fn(...args)}}};async function loadModules(){try{const r=await fetch('/api/modules');const d=await r.json();if(isInit){modules=d.modules;renderModules();isInit=false}else{updateStates(d.modules)}}catch(e){console.error(e)}}function updateStates(newModules){newModules.forEach(nm=>{const om=modules.find(m=>m.name===nm.name);if(om&&om.enabled!==nm.enabled){const t=document.querySelector(`.module[data-name=\"${nm.name}\"] .toggle`);if(t)t.classList.toggle('on',nm.enabled)}});modules=newModules}function renderModules(){const c=document.getElementById('modules');const f=modules.filter(m=>{const mc=currentCat==='all'||m.category===currentCat;const ms=m.name.toLowerCase().includes(searchQuery.toLowerCase());return mc&&ms});c.innerHTML=f.map(m=>`<div class=\"module\" data-name=\"${m.name}\"><div class=\"module-header\"><div><div class=\"module-name\">${m.name}</div><div class=\"module-cat\">${getCatName(m.category)}</div></div><div class=\"toggle ${m.enabled?'on':''}\" onclick=\"toggleModule('${m.name}')\"></div></div><div class=\"values\" id=\"v-${m.name}\"></div></div>`).join('');f.forEach(m=>loadValues(m.name))}function getCatName(c){const n={'COMBAT':'战斗','MOVEMENT':'移动','PLAYER':'玩家','RENDER':'渲染','MISC':'杂项'};return n[c]||c}async function toggleModule(name){const m=modules.find(m=>m.name===name);if(!m)return;m.enabled=!m.enabled;const t=document.querySelector(`.module[data-name=\"${name}\"] .toggle`);t.classList.toggle('on',m.enabled);await fetch('/api/module/toggle',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({module:name,enabled:m.enabled})})}async function loadValues(name){try{const r=await fetch(`/api/module/values?module=${name}`);const d=await r.json();renderValues(name,d.values)}catch(e){console.error(e)}}function renderValues(name,values){const c=document.getElementById(`v-${name}`);if(!values||values.length===0){c.innerHTML='';return}c.innerHTML=values.filter(v=>v.visible).map(v=>{if(v.type==='BooleanValue')return`<div class=\"value-item\"><label>${v.name}</label><div class=\"toggle ${v.value?'on':''}\" onclick=\"this.classList.toggle('on');updateValue('${name}','${v.name}',!this.classList.contains('on'))\"></div></div>`;if(v.type==='FloatValue'||v.type==='IntValue'||v.type==='PercentValue')return`<div class=\"value-item\"><label>${v.name}: <span>${v.value}</span></label><input type=\"range\" min=\"${v.min}\" max=\"${v.max}\" value=\"${v.value}\" step=\"${v.type==='FloatValue'?0.01:1}\" oninput=\"updateValue('${name}','${v.name}',parseFloat(this.value));this.previousElementSibling.querySelector('span').textContent=this.value\"></div>`;if(v.type==='ModeValue')return`<div class=\"value-item mode-selector\"><label>${v.name}</label><div class=\"mode-buttons\">${v.modes.map(m=>`<button class=\"mode-btn ${m===v.value?'active':''}\" onclick=\"updateValue('${name}','${v.name}','${m}');this.parentElement.querySelectorAll('.mode-btn').forEach(b=>b.classList.remove('active'));this.classList.add('active')\">${m}</button>`).join('')}</div></div>`;if(v.type==='TextValue')return`<div class=\"value-item\"><label>${v.name}</label><input type=\"text\" value=\"${v.value}\" onchange=\"updateValue('${name}','${v.name}',this.value)\"></div>`;if(v.type==='ColorValue')return`<div class=\"value-item\"><label>${v.name}</label><div style=\"display:flex;gap:8px;align-items:center;\"><input type=\"color\" value=\"${v.value}\" onchange=\"updateValue('${name}','${v.name}',this.value)\"><input type=\"text\" value=\"${v.value}\" style=\"width:80px;\" onchange=\"updateValue('${name}','${v.name}',this.value)\"></div></div>`;return''}).join('')}const updateValue=throttle(async(module,value,newValue)=>{try{await fetch('/api/value/update',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({module,value,newValue})})}catch(e){console.error(e)}},100);async function loadConfigs(){try{const r=await fetch('/api/configs');const d=await r.json();renderConfigs(d.configs)}catch(e){console.error(e)}}function renderConfigs(configs){const c=document.getElementById('configs');c.innerHTML=configs.map(cfg=>`<div class=\"config-item\"><div class=\"config-name\">${cfg.name}</div><div class=\"config-actions\"><button onclick=\"loadConfig('${cfg.name}')\" class=\"btn-load\">加载</button><button onclick=\"deleteConfig('${cfg.name}')\" class=\"btn-delete\">删除</button></div></div>`).join('')}async function saveConfig(){const name=document.getElementById('configName').value.trim();if(!name)return alert('请输入配置名称');try{await fetch('/api/configs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({action:'save',name})});loadConfigs()}catch(e){console.error(e)}}async function loadConfig(name){try{await fetch('/api/configs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({action:'load',name})});setTimeout(loadModules,500)}catch(e){console.error(e)}}async function deleteConfig(name){if(!confirm(`确定删除配置 ${name}?`))return;try{await fetch('/api/configs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({action:'delete',name})});loadConfigs()}catch(e){console.error(e)}}async function openFolder(){try{await fetch('/api/configs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({action:'openFolder'})})}catch(e){console.error(e)}}document.querySelectorAll('.cat-btn').forEach(btn=>{btn.addEventListener('click',()=>{document.querySelectorAll('.cat-btn').forEach(b=>b.classList.remove('active'));btn.classList.add('active');currentCat=btn.dataset.cat;renderModules()})});document.getElementById('search').addEventListener('input',e=>{searchQuery=e.target.value;renderModules()});document.querySelectorAll('.nav-btn').forEach(btn=>{btn.addEventListener('click',()=>{document.querySelectorAll('.nav-btn').forEach(b=>b.classList.remove('active'));btn.classList.add('active');document.querySelectorAll('.tab-content').forEach(t=>t.classList.remove('active'));const tab=btn.dataset.tab;document.getElementById(`${tab}Tab`).classList.add('active');if(tab==='configs')loadConfigs()})});document.querySelectorAll('.theme-btn').forEach(btn=>{btn.addEventListener('click',()=>{document.body.className='';if(btn.dataset.theme!=='cyber')document.body.classList.add(`theme-${btn.dataset.theme}`)})});document.addEventListener('mousemove',e=>{const glow=document.querySelector('.cursor-glow');glow.style.left=e.clientX+'px';glow.style.top=e.clientY+'px';glow.style.opacity='1'});loadModules();setInterval(loadModules,2000);";
    }
}
