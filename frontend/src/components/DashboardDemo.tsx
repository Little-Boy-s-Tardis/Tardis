import React, { useState, useMemo, useEffect } from 'react';
import { Send, Search, SlidersHorizontal, Sparkles, MessageSquare, AlertCircle, BarChart2, ShieldAlert } from 'lucide-react';
import './DashboardDemo.css';

interface Announcement {
  id: string;
  sender: string;
  platform: 'DISCORD' | 'WHATSAPP';
  timestamp: string;
  originalMessage: string;
  aiSummary: string[];
  tags: string[];
  importance: 'HIGH' | 'MEDIUM' | 'LOW';
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const WS_URL = import.meta.env.VITE_WS_URL || (API_BASE_URL.startsWith('https') 
  ? `wss://${API_BASE_URL.replace(/^https?:\/\//, '')}/ws`
  : `ws://${API_BASE_URL.replace(/^https?:\/\//, '')}/ws`);

const INITIAL_ANNOUNCEMENTS: Announcement[] = [
  {
    id: '1',
    sender: 'Judge Minh (Trưởng Ban)',
    platform: 'DISCORD',
    timestamp: '10 phút trước',
    originalMessage: '@everyone Lưu ý quan trọng: Tôi thấy nhiều đội chưa hoàn thiện file README.md giới thiệu dự án. Ban giám khảo quyết định gia hạn thời gian nộp bài thi thêm 24 tiếng. Hạn chót mới là 22:00 ngày mai 18/06. Sau thời gian này, cổng nộp bài sẽ tự động đóng và không chấp nhận bất kỳ lý do kỹ thuật nào. Hãy kiểm tra kỹ docker-compose của các bạn trước khi push.',
    aiSummary: [
      'Hạn chót mới: Gia hạn thêm 24 tiếng, hạn cuối là 22:00 ngày mai 18/06.',
      'Yêu cầu bắt buộc: Hoàn thiện đầy đủ file README.md giới thiệu dự án.',
      'Kỹ thuật: Kiểm tra kỹ file docker-compose.yml trước khi push bài thi.'
    ],
    tags: ['deadline', 'readme', 'docker'],
    importance: 'HIGH'
  },
  {
    id: '2',
    sender: 'Judge David (Tech Lead)',
    platform: 'WHATSAPP',
    timestamp: '35 phút trước',
    originalMessage: 'Hey teams, remember that our automated test scripts will scan your APIs on the server. Your Spring Boot controllers must use prefix /api/v1 and return valid JSON structures for all resource requests. Ensure that you have configured CORS correctly otherwise the frontend demo won\'t load.',
    aiSummary: [
      'Endpoint Prefix: Các API Spring Boot phải sử dụng tiền tố /api/v1.',
      'JSON Format: Dữ liệu trả về bắt buộc phải đúng định dạng JSON chuẩn.',
      'Cấu hình CORS: Phải bật CORS để tránh việc frontend demo không thể load API.'
    ],
    tags: ['springboot', 'cors', 'api'],
    importance: 'MEDIUM'
  },
  {
    id: '3',
    sender: 'Judge Jessica (Design Coach)',
    platform: 'DISCORD',
    timestamp: '2 giờ trước',
    originalMessage: 'We will be holding a brief Q&A session today at 15:00 UTC on Discord voice channel. If you have questions about the submission criteria, or how the judges will evaluate the UI/UX design, drop by! It is optional but recommended.',
    aiSummary: [
      'Q&A Session: Diễn ra vào lúc 15:00 UTC hôm nay tại kênh Voice Discord.',
      'Nội dung: Giải đáp về tiêu chí nộp bài và tiêu chí đánh giá UI/UX.',
      'Tham gia: Tự nguyện, nhưng khuyến khích các đội tham dự.'
    ],
    tags: ['qa', 'design', 'discord-voice'],
    importance: 'LOW'
  }
];

export function DashboardDemo() {
  const [announcements, setAnnouncements] = useState<Announcement[]>(INITIAL_ANNOUNCEMENTS);
  const [search, setSearch] = useState('');
  const [platformFilter, setPlatformFilter] = useState<'ALL' | 'DISCORD' | 'WHATSAPP'>('ALL');
  const [importanceFilter, setImportanceFilter] = useState<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');
  
  // Simulator form state
  const [simSender, setSimSender] = useState('');
  const [simPlatform, setSimPlatform] = useState<'DISCORD' | 'WHATSAPP'>('DISCORD');
  const [simImportance, setSimImportance] = useState<'HIGH' | 'MEDIUM' | 'LOW'>('MEDIUM');
  const [simMessage, setSimMessage] = useState('');
  
  // Simulation status pipeline
  const [simStep, setSimStep] = useState<number>(0); // 0: idle, 1: webhook, 2: rabbitmq, 3: ai summarizer, 4: broadcasting
  const [expandedCards, setExpandedCards] = useState<Record<string, boolean>>({});

  const toggleExpand = (id: string) => {
    setExpandedCards(prev => ({ ...prev, [id]: !prev[id] }));
  };

  useEffect(() => {
    // 1. Fetch historical announcements
    fetch(`${API_BASE_URL}/api/v1/announcements`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          setAnnouncements(data);
        }
      })
      .catch(err => console.warn("Backend offline. Operating in simulation mode with mock data.", err));

    // 2. STOMP over WebSocket connection
    let ws: WebSocket;
    let reconnectTimeout: any;

    const connectWebSocket = () => {
      console.log("Connecting to WebSocket stomp endpoint...");
      ws = new WebSocket(WS_URL);

      ws.onopen = () => {
        console.log("WebSocket open. Connecting STOMP...");
        ws.send("CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000");
      };

      ws.onmessage = (event) => {
        const msg = event.data;
        if (msg.startsWith("CONNECTED")) {
          console.log("STOMP session connected. Subscribing to /topic/announcements...");
          ws.send("SUBSCRIBE\nid:sub-0\ndestination:/topic/announcements\n\n\u0000");
        } else if (msg.startsWith("MESSAGE")) {
          const bodyStartIndex = msg.indexOf("\n\n") + 2;
          const bodyEndIndex = msg.lastIndexOf("\u0000");
          if (bodyStartIndex > 1 && bodyEndIndex > -1) {
            const jsonBody = msg.substring(bodyStartIndex, bodyEndIndex).trim();
            try {
              const newAnnouncement = JSON.parse(jsonBody);
              console.log("Live message received:", newAnnouncement);
              setAnnouncements(prev => {
                if (prev.some(item => item.id === newAnnouncement.id)) {
                  return prev;
                }
                return [newAnnouncement, ...prev];
              });
            } catch (e) {
              console.error("JSON parsing failed for WebSocket frame:", e);
            }
          }
        }
      };

      ws.onclose = () => {
        console.warn("WebSocket disconnect. Retrying reconnect in 5s...");
        reconnectTimeout = setTimeout(connectWebSocket, 5000);
      };

      ws.onerror = (err) => {
        console.error("WebSocket exception:", err);
        ws.close();
      };
    };

    connectWebSocket();

    return () => {
      if (ws) ws.close();
      clearTimeout(reconnectTimeout);
    };
  }, []);

  const filteredAnnouncements = useMemo(() => {
    return announcements.filter(item => {
      const matchesSearch = 
        item.sender.toLowerCase().includes(search.toLowerCase()) ||
        (item.originalMessage && item.originalMessage.toLowerCase().includes(search.toLowerCase())) ||
        item.tags.some(tag => tag.toLowerCase().includes(search.toLowerCase()));
      
      const matchesPlatform = platformFilter === 'ALL' || item.platform === platformFilter;
      const matchesImportance = importanceFilter === 'ALL' || item.importance === importanceFilter;
      
      return matchesSearch && matchesPlatform && matchesImportance;
    });
  }, [announcements, search, platformFilter, importanceFilter]);

  const stats = useMemo(() => {
    const total = announcements.length;
    const high = announcements.filter(a => a.importance === 'HIGH').length;
    const discordCount = announcements.filter(a => a.platform === 'DISCORD').length;
    const whatsappCount = announcements.filter(a => a.platform === 'WHATSAPP').length;
    
    const originalWordCount = announcements.reduce((sum, a) => sum + (a.originalMessage ? a.originalMessage.split(' ').length : 0), 0);
    const summaryWordCount = announcements.reduce((sum, a) => sum + (a.aiSummary ? a.aiSummary.join(' ').split(' ').length : 0), 0);
    const tokensSaved = Math.max(0, originalWordCount - summaryWordCount) * 4;

    return { total, high, discordCount, whatsappCount, tokensSaved };
  }, [announcements]);

  const handleSimulate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!simSender.trim() || !simMessage.trim()) return;

    setSimStep(1); // Webhook Step 1

    const payload = {
      sender: simSender,
      content: simMessage,
      conversationId: 'global-contest'
    };

    const endpoint = simPlatform === 'DISCORD' 
      ? `${API_BASE_URL}/api/v1/webhooks/discord` 
      : `${API_BASE_URL}/api/v1/webhooks/whatsapp`;

    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Webhook-Token': 'antigravity-secret-verify-token'
      },
      body: JSON.stringify(payload)
    })
    .then(res => {
      if (res.ok) {
        // Step 2: RabbitMQ queued (simulated delay transition)
        setSimStep(2);
        
        setTimeout(() => {
          setSimStep(3); // AI Processing (Qwen)
          
          setTimeout(() => {
            setSimStep(4); // WebSocket Broadcasting
            
            setTimeout(() => {
              setSimStep(0);
              setSimSender('');
              setSimMessage('');
            }, 1000);
          }, 1500);
        }, 1000);
      } else {
        alert("Gửi webhook thất bại! Vui lòng kiểm tra trạng thái Backend (Spring Boot).");
        setSimStep(0);
      }
    })
    .catch(err => {
      console.warn("Backend offline. Performing local fallback simulation...", err);
      // Fallback local animation if backend is not running
      setSimStep(2);
      setTimeout(() => {
        setSimStep(3);
        setTimeout(() => {
          setSimStep(4);
          setTimeout(() => {
            const lines = simMessage.split(/[.!?]+/).map(s => s.trim()).filter(s => s.length > 5);
            const generatedSummary: string[] = [];
            
            if (lines.length > 0) {
              generatedSummary.push(`Nội dung chính: ${lines[0]}`);
            }
            if (lines.length > 1) {
              generatedSummary.push(`Chi tiết: ${lines[1]}`);
            }
            if (simMessage.toLowerCase().includes('hạn') || simMessage.toLowerCase().includes('deadline') || simMessage.toLowerCase().includes('giờ')) {
              generatedSummary.push(`Lưu ý về thời gian hoặc mốc deadline quy định.`);
            }

            const tags = ['simulated'];
            if (simMessage.toLowerCase().includes('deadline') || simMessage.toLowerCase().includes('hạn')) tags.push('deadline');
            if (simMessage.toLowerCase().includes('code') || simMessage.toLowerCase().includes('api')) tags.push('technical');

            const newAnnouncement: Announcement = {
              id: Date.now().toString(),
              sender: simSender,
              platform: simPlatform,
              timestamp: 'Vừa xong',
              originalMessage: simMessage,
              aiSummary: generatedSummary,
              tags: tags,
              importance: simImportance
            };

            setAnnouncements(prev => [newAnnouncement, ...prev]);
            setSimSender('');
            setSimMessage('');
            setSimStep(0);
          }, 600);
        }, 800);
      }, 700);
    });
  };

  return (
    <section className="dashboard-section section-gap">
      <div className="container">
        
        {/* Section Header */}
        <div className="dashboard-intro text-center">
          <span className="badge-live-pulse">REAL-TIME SIMULATION</span>
          <h2>Trang Quản Lý Thông Báo BGK</h2>
          <p className="text-body text-muted">
            Trải nghiệm khả năng tóm tắt thời gian thực. Sử dụng form giả lập bên dưới để gửi tin nhắn từ Discord/WhatsApp của Ban Giám Khảo và xem hệ thống hoạt động.
          </p>
        </div>

        {/* Dashboard Stats */}
        <div className="stats-board-grid">
          <div className="card-default stat-board-card">
            <div className="stat-card-icon discord-bg"><MessageSquare size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.discordCount}</h4>
              <p className="text-small text-muted">Tin từ Discord</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon whatsapp-bg"><MessageSquare size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.whatsappCount}</h4>
              <p className="text-small text-muted">Tin từ WhatsApp</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon alert-bg"><ShieldAlert size={20} /></div>
            <div className="stat-card-data">
              <h4>{stats.high}</h4>
              <p className="text-small text-muted">Khẩn Cấp (High)</p>
            </div>
          </div>
          <div className="card-default stat-board-card">
            <div className="stat-card-icon token-bg"><Sparkles size={20} /></div>
            <div className="stat-card-data">
              <h4>~{stats.tokensSaved}</h4>
              <p className="text-small text-muted">Từ tiết kiệm được (Words)</p>
            </div>
          </div>
        </div>

        <div className="dashboard-workspace-grid">
          
          {/* Left Panel: Filters & Live Feed */}
          <div className="feed-panel">
            <div className="feed-controls-box doodle-border">
              
              {/* Search Bar */}
              <div className="search-wrapper">
                <Search className="search-icon" size={18} />
                <input 
                  type="text" 
                  placeholder="Tìm theo người gửi, nội dung hoặc tag..." 
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="search-input"
                />
              </div>

              {/* Filtering Options */}
              <div className="filter-options-row">
                <div className="filter-group">
                  <span className="filter-label"><SlidersHorizontal size={14} /> Nền tảng:</span>
                  <div className="filter-buttons">
                    <button 
                      className={`filter-btn ${platformFilter === 'ALL' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('ALL')}
                    >
                      Tất cả
                    </button>
                    <button 
                      className={`filter-btn ${platformFilter === 'DISCORD' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('DISCORD')}
                    >
                      Discord
                    </button>
                    <button 
                      className={`filter-btn ${platformFilter === 'WHATSAPP' ? 'active' : ''}`}
                      onClick={() => setPlatformFilter('WHATSAPP')}
                    >
                      WhatsApp
                    </button>
                  </div>
                </div>

                <div className="filter-group">
                  <span className="filter-label"><AlertCircle size={14} /> Độ quan trọng:</span>
                  <div className="filter-buttons">
                    <button 
                      className={`filter-btn ${importanceFilter === 'ALL' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('ALL')}
                    >
                      Tất cả
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'HIGH' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('HIGH')}
                    >
                      Khẩn cấp
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'MEDIUM' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('MEDIUM')}
                    >
                      Thường
                    </button>
                    <button 
                      className={`filter-btn ${importanceFilter === 'LOW' ? 'active' : ''}`}
                      onClick={() => setImportanceFilter('LOW')}
                    >
                      Thấp
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Announcement Feed */}
            <div className="announcements-feed-list">
              {filteredAnnouncements.length === 0 ? (
                <div className="card-default empty-state-card text-center">
                  <p className="text-body text-muted">Không tìm thấy thông báo nào phù hợp bộ lọc.</p>
                </div>
              ) : (
                filteredAnnouncements.map((item) => (
                  <div 
                    key={item.id} 
                    className={`card-default announcement-card border-importance-${item.importance.toLowerCase()}`}
                  >
                    <div className="announcement-card-header">
                      <div className="sender-meta">
                        <span className={`platform-pill platform-color-${item.platform.toLowerCase()}`}>
                          {item.platform}
                        </span>
                        <h4 className="sender-title">{item.sender}</h4>
                      </div>
                      <div className="time-meta">
                        <span className="text-small text-muted">{item.timestamp}</span>
                        <span className={`importance-tag level-${item.importance.toLowerCase()}`}>
                          {item.importance === 'HIGH' ? 'Khẩn cấp' : item.importance === 'MEDIUM' ? 'Bình thường' : 'Thấp'}
                        </span>
                      </div>
                    </div>

                    {/* AI SUMMARY BOX */}
                    <div className="ai-summary-highlight doodle-border">
                      <div className="summary-title">
                        <Sparkles size={16} className="spark-icon" />
                        <span>AI Tóm Tắt Ý Chính:</span>
                      </div>
                      <ul className="summary-list">
                        {item.aiSummary.map((bullet, idx) => (
                          <li key={idx} dangerouslySetInnerHTML={{ __html: bullet }} />
                        ))}
                      </ul>
                    </div>

                    {/* Original Message */}
                    <div className="original-message-section">
                      <button 
                        className="toggle-original-btn"
                        onClick={() => toggleExpand(item.id)}
                      >
                        {expandedCards[item.id] ? ' ẩn tin nhắn gốc ▲' : ' xem tin nhắn gốc ▼'}
                      </button>
                      
                      {expandedCards[item.id] && (
                        <div className="original-msg-content doodle-border">
                          {item.originalMessage}
                        </div>
                      )}
                    </div>

                    <div className="announcement-card-footer">
                      <div className="tags-container">
                        {item.tags.map((tag) => (
                          <span key={tag} className="tag-pill">#{tag}</span>
                        ))}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Right Panel: Announcement Simulator Form */}
          <div className="simulator-panel">
            <div className="card-default simulator-card">
              <div className="sim-header">
                <h3>Bộ Giả Lập Webhook</h3>
                <p className="text-small text-muted">Gửi tin nhắn raw giả lập từ các kênh chat của Ban giám khảo.</p>
              </div>

              {simStep > 0 && (
                <div className="pipeline-loading-box doodle-border">
                  <div className="pipeline-steps">
                    <div className={`step-item ${simStep >= 1 ? 'active' : ''} ${simStep === 1 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 1 ? '✓' : '1'}</span>
                      <span className="step-txt">Nhận Webhook</span>
                    </div>
                    <div className={`step-item ${simStep >= 2 ? 'active' : ''} ${simStep === 2 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 2 ? '✓' : '2'}</span>
                      <span className="step-txt">RabbitMQ Queue</span>
                    </div>
                    <div className={`step-item ${simStep >= 3 ? 'active' : ''} ${simStep === 3 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 3 ? '✓' : '3'}</span>
                      <span className="step-txt">AI Summarizer</span>
                    </div>
                    <div className={`step-item ${simStep >= 4 ? 'active' : ''} ${simStep === 4 ? 'loading' : ''}`}>
                      <span className="step-num">{simStep > 4 ? '✓' : '4'}</span>
                      <span className="step-txt">Broadcast Realtime</span>
                    </div>
                  </div>
                  <div className="progress-bar-wrapper">
                    <div className="progress-bar-fill" style={{ width: `${(simStep / 4) * 100}%` }}></div>
                  </div>
                  <p className="pipeline-status-text">
                    {simStep === 1 && "Đang gửi HTTP POST tới Spring Boot Webhook API..."}
                    {simStep === 2 && "Spring Boot đang đẩy message vào RabbitMQ..."}
                    {simStep === 3 && "RabbitMQ Consumer kích hoạt, đang gọi LLM để tóm tắt..."}
                    {simStep === 4 && "Đã lưu DB, đang push websocket lên dashboard React..."}
                  </p>
                </div>
              )}

              <form onSubmit={handleSimulate} className="sim-form">
                <div className="form-group-flex">
                  <div className="form-field">
                    <label className="field-label">Kênh gửi (Platform)</label>
                    <select 
                      value={simPlatform}
                      onChange={(e) => setSimPlatform(e.target.value as 'DISCORD' | 'WHATSAPP')}
                      className="sim-select"
                      disabled={simStep > 0}
                    >
                      <option value="DISCORD">Discord Webhook</option>
                      <option value="WHATSAPP">WhatsApp Cloud API</option>
                    </select>
                  </div>
                  <div className="form-field">
                    <label className="field-label">Mức độ khẩn</label>
                    <select 
                      value={simImportance}
                      onChange={(e) => setSimImportance(e.target.value as 'HIGH' | 'MEDIUM' | 'LOW')}
                      className="sim-select"
                      disabled={simStep > 0}
                    >
                      <option value="HIGH">HIGH (Khẩn cấp)</option>
                      <option value="MEDIUM">MEDIUM (Bình thường)</option>
                      <option value="LOW">LOW (Thấp)</option>
                    </select>
                  </div>
                </div>

                <div className="form-field">
                  <label className="field-label">Tên Giám Khảo (Sender)</label>
                  <input 
                    type="text" 
                    placeholder="VD: Judge David, BGK Minh..." 
                    value={simSender}
                    onChange={(e) => setSimSender(e.target.value)}
                    className="sim-input"
                    disabled={simStep > 0}
                    required
                  />
                </div>

                <div className="form-field">
                  <label className="field-label">Nội dung tin nhắn gốc (Original Long Message)</label>
                  <textarea 
                    placeholder="Nhập tin nhắn thông báo dài từ giám khảo..." 
                    value={simMessage}
                    onChange={(e) => setSimMessage(e.target.value)}
                    className="sim-textarea"
                    rows={5}
                    disabled={simStep > 0}
                    required
                  />
                </div>

                <button 
                  type="submit" 
                  className="btn btn-sm btn-primary sim-submit-btn" 
                  disabled={simStep > 0 || !simSender.trim() || !simMessage.trim()}
                >
                  <Send size={16} /> Gửi webhook & tóm tắt
                </button>
              </form>
            </div>

            {/* Tech explanation info box */}
            <div className="card-default info-explain-card" style={{ marginTop: '16px' }}>
              <div className="info-header" style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
                <BarChart2 size={18} style={{ color: 'var(--c-warning)' }} />
                <h4 style={{ margin: 0, fontSize: '14px' }}>Nguyên Lý Kiến Trúc</h4>
              </div>
              <p className="text-small" style={{ margin: 0, lineHeight: 1.5, fontSize: '12px' }}>
                Trong ứng dụng thực tế, Spring Boot nhận webhook từ Discord/WhatsApp, lưu vào hàng đợi RabbitMQ, sau đó trả về <code>200 OK</code> trong <strong>&lt; 50ms</strong>. 
                Tiếp đó, background thread tiêu thụ hàng đợi để gửi message sang AI Engine, lưu dữ liệu cấu trúc vào Postgres, rồi kích hoạt client browser cập nhật UI thông qua WebSocket connection.
              </p>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
